package com.flow.flowboard.api.engine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flow.flowboard.api.engine.queue.ExecutionTask;
import com.flow.flowboard.api.engine.queue.TaskQueue;
import com.flow.flowboard.api.entity.StepExecution;
import com.flow.flowboard.api.entity.WorkflowExecution;
import com.flow.flowboard.api.repository.StepExecutionRepository;
import com.flow.flowboard.api.repository.WorkflowExecutionRepository;
import com.flow.flowboard.common.domain.StepStatus;
import com.flow.flowboard.common.domain.NodeType;
import com.flow.flowboard.common.dto.workflow.EdgeDto;
import com.flow.flowboard.common.dto.workflow.NodeDto;
import com.flow.flowboard.common.dto.workflow.WorkflowDefinitionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowOrchestrator {

    private final WorkflowExecutionRepository executionRepository;
    private final StepExecutionRepository stepExecutionRepository;
    private final TaskQueue taskQueue;
    private final ObjectMapper objectMapper;

    @Transactional
    public void startExecution(UUID executionId) {
        WorkflowExecution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new RuntimeException("Execution not found: " + executionId));

        WorkflowDefinitionDto definition = parseDefinition(execution.getDefinitionSnapshotJson());
        List<StepExecution> steps = stepExecutionRepository
                .findByWorkflowExecution_IdAndActiveTrueOrderByCreatedAt(executionId);
        Map<String, StepExecution> stepMap = steps.stream()
                .collect(Collectors.toMap(StepExecution::getNodeIdRef, s -> s));

        // Find start nodes (nodes with no incoming edges)
        Set<String> targetNodes = definition.getEdges().stream()
                .map(EdgeDto::getTarget)
                .collect(Collectors.toSet());

        List<NodeDto> startNodes = definition.getNodes().stream()
                .filter(node -> !targetNodes.contains(node.getId()))
                .toList();

        for (NodeDto node : startNodes) {
            queueNode(node, stepMap.get(node.getId()), executionId);
        }
    }

    @Transactional
    public void handleStepCompletion(UUID stepId, Map<String, Object> output) {
        StepExecution step = stepExecutionRepository.findById(stepId)
                .orElseThrow(() -> new RuntimeException("Step not found: " + stepId));

        step.setStatus(StepStatus.SUCCESS);

        // Save output payload
        if (output != null && !output.isEmpty()) {
            try {
                String outputJson = objectMapper.writeValueAsString(output);
                step.setOutputPayload(outputJson);
            } catch (Exception e) {
                log.error("Failed to serialize output for step {}", stepId, e);
            }
        }

        stepExecutionRepository.save(step);

        WorkflowExecution execution = step.getWorkflowExecution();
        WorkflowDefinitionDto definition = parseDefinition(execution.getDefinitionSnapshotJson());

        List<StepExecution> allSteps = stepExecutionRepository
                .findByWorkflowExecution_IdAndActiveTrueOrderByCreatedAt(execution.getId());
        Map<String, StepExecution> stepMap = allSteps.stream()
                .collect(Collectors.toMap(StepExecution::getNodeIdRef, s -> s));

        // Find children
        List<String> childrenIds = definition.getEdges().stream()
                .filter(e -> e.getSource().equals(step.getNodeIdRef()))
                .map(EdgeDto::getTarget)
                .toList();

        for (String childId : childrenIds) {
            if (canRun(childId, definition, stepMap)) {
                NodeDto childNode = definition.getNodes().stream()
                        .filter(n -> n.getId().equals(childId))
                        .findFirst()
                        .orElseThrow();
                queueNode(childNode, stepMap.get(childId), execution.getId());
            }
        }

        // Check if workflow is complete
        checkAndCompleteWorkflow(execution, allSteps);
    }

    private void checkAndCompleteWorkflow(WorkflowExecution execution, List<StepExecution> allSteps) {
        // Check if all steps are in a terminal state (SUCCESS or FAILED)
        boolean allComplete = allSteps.stream()
                .allMatch(s -> s.getStatus() == StepStatus.SUCCESS || s.getStatus() == StepStatus.FAILED);

        if (allComplete) {
            // Check if any step failed
            boolean anyFailed = allSteps.stream()
                    .anyMatch(s -> s.getStatus() == StepStatus.FAILED);

            if (anyFailed) {
                execution.setStatus(com.flow.flowboard.common.domain.ExecutionStatus.FAILED);
                log.info("Workflow execution {} completed with FAILED status", execution.getId());
            } else {
                execution.setStatus(com.flow.flowboard.common.domain.ExecutionStatus.SUCCESS);
                log.info("Workflow execution {} completed with SUCCESS status", execution.getId());
            }

            execution.setEndedAt(java.time.Instant.now());
            executionRepository.save(execution);
        }
    }

    @Transactional
    public void handleStepFailure(UUID stepId, String error) {
        StepExecution step = stepExecutionRepository.findById(stepId)
                .orElseThrow(() -> new RuntimeException("Step not found: " + stepId));
        step.setStatus(StepStatus.FAILED);
        step.setErrorMessage(error);
        stepExecutionRepository.save(step);
        // TODO: Handle failure propagation (e.g. cancel workflow or skip children)
    }

    private boolean canRun(String nodeId, WorkflowDefinitionDto definition, Map<String, StepExecution> stepMap) {
        // Find all parents
        List<String> parentIds = definition.getEdges().stream()
                .filter(e -> e.getTarget().equals(nodeId))
                .map(EdgeDto::getSource)
                .toList();

        for (String parentId : parentIds) {
            StepExecution parentStep = stepMap.get(parentId);
            if (parentStep == null || parentStep.getStatus() != StepStatus.SUCCESS) {
                return false;
            }
        }
        return true;
    }

    private void queueNode(NodeDto node, StepExecution step, UUID executionId) {
        if (step == null)
            return;

        step.setStatus(StepStatus.QUEUED);
        stepExecutionRepository.save(step);

        Map<String, Object> context = new HashMap<>();
        if (node.getData() != null) {
            context.putAll(node.getData());
        }

        NodeType nodeType;
        try {
            nodeType = NodeType.valueOf(node.getType());
        } catch (IllegalArgumentException e) {
            log.error("Invalid node type: {}", node.getType());
            return;
        }

        ExecutionTask task = new ExecutionTask(executionId, step.getId(), node.getId(), nodeType, context);

        // Check for delay
        if (NodeType.DELAY == nodeType) {
            long delay = 1000;
            if (node.getData() != null && node.getData().containsKey("duration")) {
                try {
                    delay = Long.parseLong(node.getData().get("duration").toString());
                } catch (NumberFormatException e) {
                    // ignore
                }
            }
            taskQueue.pushDelayed(task, delay).subscribe();
        } else {
            taskQueue.push(task).subscribe();
        }
    }

    private WorkflowDefinitionDto parseDefinition(String json) {
        try {
            return objectMapper.readValue(json, WorkflowDefinitionDto.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
