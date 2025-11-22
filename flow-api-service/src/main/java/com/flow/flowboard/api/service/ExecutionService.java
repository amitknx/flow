package com.flow.flowboard.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flow.flowboard.api.entity.StepExecution;
import com.flow.flowboard.api.entity.Workflow;
import com.flow.flowboard.api.entity.WorkflowExecution;
import com.flow.flowboard.api.exception.ExecutionNotFoundException;
import com.flow.flowboard.api.exception.InvalidExecutionStateException;
import com.flow.flowboard.api.exception.WorkflowNotFoundException;
import com.flow.flowboard.api.repository.StepExecutionRepository;
import com.flow.flowboard.api.repository.WorkflowExecutionRepository;
import com.flow.flowboard.api.repository.WorkflowRepository;
import com.flow.flowboard.common.domain.ExecutionStatus;
import com.flow.flowboard.common.domain.StepStatus;
import com.flow.flowboard.common.dto.execution.ExecutionCreateRequest;
import com.flow.flowboard.common.dto.execution.ExecutionResponse;
import com.flow.flowboard.common.dto.execution.ExecutionStatusResponse;
import com.flow.flowboard.common.dto.execution.StepExecutionResponse;
import com.flow.flowboard.common.dto.workflow.NodeDto;
import com.flow.flowboard.common.dto.workflow.WorkflowDefinitionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExecutionService {

    private final WorkflowRepository workflowRepository;
    private final WorkflowExecutionRepository executionRepository;
    private final StepExecutionRepository stepExecutionRepository;
    private final ObjectMapper objectMapper;
    private final com.flow.flowboard.api.engine.service.WorkflowOrchestrator orchestrator;

    @Transactional
    public ExecutionResponse createExecution(ExecutionCreateRequest request) {
        // Validate workflow exists
        Workflow workflow = workflowRepository.findById(request.getWorkflowId())
                .orElseThrow(() -> new WorkflowNotFoundException(request.getWorkflowId()));

        // Take immutable snapshot of workflow definition
        String definitionSnapshot = workflow.getData();

        // Create workflow execution
        WorkflowExecution execution = WorkflowExecution.builder()
                .workflow(workflow)
                .correlationId(request.getCorrelationId())
                .status(ExecutionStatus.PENDING)
                .triggerType(request.getTriggerType())
                .triggerInput(toJsonString(request.getTriggerInput()))
                .definitionSnapshotJson(definitionSnapshot)
                .build();

        WorkflowExecution savedExecution = executionRepository.save(execution);

        // Initialize step executions from workflow definition
        List<StepExecution> stepExecutions = initializeStepExecutions(savedExecution, definitionSnapshot);
        stepExecutionRepository.saveAll(stepExecutions);

        log.info("Created workflow execution {} for workflow {} with trigger type {}",
                savedExecution.getId(), workflow.getId(), request.getTriggerType());

        // Start execution asynchronously
        orchestrator.startExecution(savedExecution.getId());

        return toExecutionResponse(savedExecution, stepExecutions);
    }

    @Transactional(readOnly = true)
    public ExecutionResponse getExecutionById(UUID id) {
        WorkflowExecution execution = executionRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ExecutionNotFoundException(id));

        List<StepExecution> stepExecutions = stepExecutionRepository
                .findByWorkflowExecution_IdAndActiveTrueOrderByCreatedAt(id);

        return toExecutionResponse(execution, stepExecutions);
    }

    @Transactional(readOnly = true)
    public ExecutionStatusResponse getExecutionStatus(UUID id) {
        WorkflowExecution execution = executionRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ExecutionNotFoundException(id));

        long totalSteps = stepExecutionRepository.countByWorkflowExecutionId(id);
        long completedSteps = stepExecutionRepository.countCompletedByWorkflowExecutionId(id);

        int progressPercentage = totalSteps > 0
                ? (int) ((completedSteps * 100) / totalSteps)
                : 0;

        return ExecutionStatusResponse.builder()
                .id(execution.getId())
                .status(execution.getStatus())
                .completedSteps((int) completedSteps)
                .totalSteps((int) totalSteps)
                .progressPercentage(progressPercentage)
                .startedAt(execution.getStartedAt())
                .endedAt(execution.getEndedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<ExecutionResponse> listExecutionsByWorkflow(UUID workflowId, Pageable pageable) {
        return executionRepository.findByWorkflow_IdAndActiveTrue(workflowId, pageable)
                .map(execution -> {
                    List<StepExecution> steps = stepExecutionRepository
                            .findByWorkflowExecution_IdAndActiveTrueOrderByCreatedAt(execution.getId());
                    return toExecutionResponse(execution, steps);
                });
    }

    @Transactional(readOnly = true)
    public Page<ExecutionResponse> getAllExecutions(Pageable pageable) {
        return executionRepository.findByActiveTrue(pageable)
                .map(execution -> {
                    List<StepExecution> steps = stepExecutionRepository
                            .findByWorkflowExecution_IdAndActiveTrueOrderByCreatedAt(execution.getId());
                    return toExecutionResponse(execution, steps);
                });
    }

    @Transactional
    public ExecutionResponse cancelExecution(UUID id) {
        WorkflowExecution execution = executionRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ExecutionNotFoundException(id));

        if (execution.getStatus() == ExecutionStatus.SUCCESS ||
                execution.getStatus() == ExecutionStatus.FAILED ||
                execution.getStatus() == ExecutionStatus.CANCELLED) {
            throw new InvalidExecutionStateException(
                    "Cannot cancel execution in " + execution.getStatus() + " state");
        }

        execution.setStatus(ExecutionStatus.CANCELLED);
        execution.setEndedAt(Instant.now());

        WorkflowExecution updated = executionRepository.save(execution);

        List<StepExecution> stepExecutions = stepExecutionRepository
                .findByWorkflowExecution_IdAndActiveTrueOrderByCreatedAt(id);

        log.info("Cancelled execution {}", id);

        return toExecutionResponse(updated, stepExecutions);
    }

    private List<StepExecution> initializeStepExecutions(WorkflowExecution execution, String workflowData) {
        List<StepExecution> stepExecutions = new ArrayList<>();

        try {
            // Parse workflow data to extract nodes
            WorkflowDefinitionDto workflowDefinition = objectMapper.readValue(workflowData,
                    WorkflowDefinitionDto.class);
            List<NodeDto> nodes = workflowDefinition.getNodes();

            if (nodes != null) {
                for (NodeDto node : nodes) {
                    String nodeId = node.getId();
                    Map<String, Object> nodeData = node.getData();
                    String stepName = nodeData != null && nodeData.containsKey("label")
                            ? (String) nodeData.get("label")
                            : node.getName() != null ? node.getName() : nodeId;

                    StepExecution stepExecution = StepExecution.builder()
                            .workflowExecution(execution)
                            .nodeIdRef(nodeId)
                            .stepName(stepName)
                            .status(StepStatus.PENDING)
                            .build();

                    stepExecutions.add(stepExecution);
                }
            }
        } catch (JsonProcessingException e) {
            log.error("Failed to parse workflow definition for execution {}", execution.getId(), e);
            throw new RuntimeException("Failed to initialize step executions", e);
        }

        return stepExecutions;
    }

    private ExecutionResponse toExecutionResponse(WorkflowExecution execution, List<StepExecution> stepExecutions) {
        return ExecutionResponse.builder()
                .id(execution.getId())
                .workflowId(execution.getWorkflow().getId())
                .correlationId(execution.getCorrelationId())
                .status(execution.getStatus())
                .triggerType(execution.getTriggerType())
                .triggerInput(parseJsonString(execution.getTriggerInput()))
                .definitionSnapshot(parseWorkflowDefinition(execution.getDefinitionSnapshotJson()))
                .startedAt(execution.getStartedAt())
                .endedAt(execution.getEndedAt())
                .active(execution.getActive())
                .createdAt(execution.getCreatedAt())
                .createdBy(execution.getCreatedBy())
                .modifiedAt(execution.getModifiedAt())
                .modifiedBy(execution.getModifiedBy())
                .stepExecutions(stepExecutions.stream()
                        .map(this::toStepExecutionResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    private StepExecutionResponse toStepExecutionResponse(StepExecution step) {
        return StepExecutionResponse.builder()
                .id(step.getId())
                .workflowExecutionId(step.getWorkflowExecution().getId())
                .nodeIdRef(step.getNodeIdRef())
                .stepName(step.getStepName())
                .status(step.getStatus())
                .inputPayload(parseJsonString(step.getInputPayload()))
                .outputPayload(parseJsonString(step.getOutputPayload()))
                .errorMessage(step.getErrorMessage())
                .startedAt(step.getStartedAt())
                .endedAt(step.getEndedAt())
                .durationMs(step.getDurationMs())
                .build();
    }

    private String toJsonString(Object data) {
        if (data == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize data to JSON", e);
        }
    }

    private Object parseJsonString(String json) {
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JSON data", e);
        }
    }

    private WorkflowDefinitionDto parseWorkflowDefinition(String json) {
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, WorkflowDefinitionDto.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse workflow definition JSON", e);
            throw new RuntimeException("Failed to parse workflow definition", e);
        }
    }
}
