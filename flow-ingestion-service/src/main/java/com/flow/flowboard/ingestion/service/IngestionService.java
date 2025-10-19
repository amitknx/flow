/*
 ************************************************************************
 * flow CONFIDENTIAL ___________________
 * Copyright 2024 flow Systems Incorporated All Rights Reserved.
 * NOTICE: All information contained herein is, and remains the property of flow Systems
 * Incorporated and its suppliers, if any. The intellectual and technical concepts contained herein
 * are proprietary to flow Systems Incorporated and its suppliers and are protected by all
 * applicable intellectual property laws, including trade secret and copyright laws. Dissemination
 * of this information or reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from flow Systems Incorporated.
 *************************************************************************
 */
package com.flow.flowboard.ingestion.service;

import com.flow.flowboard.common.dto.IngestionEventDto;
import com.flow.flowboard.ingestion.domain.StepExecution;
import com.flow.flowboard.ingestion.domain.WorkflowExecution;
import com.flow.flowboard.ingestion.repository.StepExecutionRepository;
import com.flow.flowboard.ingestion.repository.WorkflowExecutionRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists ingestion events to runtime tables (MVP, no MQ yet).
 */
@Service
@RequiredArgsConstructor
public class IngestionService {

    private final WorkflowExecutionRepository workflowExecutionRepository;
    private final StepExecutionRepository stepExecutionRepository;

    @Transactional
    public void ingest(IngestionEventDto event) {
        UUID workflowId = UUID.fromString(event.getWorkflowId());
        Optional<WorkflowExecution> existing = workflowExecutionRepository
                .findByWorkflowIdAndCorrelationId(workflowId, event.getCorrelationId());

        WorkflowExecution execution = existing.orElseGet(() -> {
            WorkflowExecution e = WorkflowExecution.builder()
                    .workflowId(workflowId)
                    .correlationId(event.getCorrelationId())
                    .status(WorkflowExecution.Status.RUNNING)
                    .triggerInput(null)
                    .definitionSnapshotJson(null)
                    .startedAt(Instant.now())
                    .build();
            return workflowExecutionRepository.save(e);
        });

        IngestionEventDto.Step step = event.getStep();
        StepExecution.Status status = mapStatus(step.getPhase());
        StepExecution stepExec = StepExecution.builder()
                .workflowExecutionId(execution.getId())
                .nodeIdRef(step.getId())
                .status(status)
                .inputPayload(event.getInput() != null ? event.getInput().toString() : null)
                .outputPayload(event.getOutput() != null ? event.getOutput().toString() : null)
                .startedAt(status == StepExecution.Status.RUNNING ? Instant.now() : null)
                .endedAt((status == StepExecution.Status.SUCCESS || status == StepExecution.Status.FAILED) ? Instant.now() : null)
                .build();
        if (stepExec.getStartedAt() != null && stepExec.getEndedAt() != null) {
            stepExec.setDurationMs(Duration.between(stepExec.getStartedAt(), stepExec.getEndedAt()).toMillis());
        }
        stepExecutionRepository.save(stepExec);
    }

    private StepExecution.Status mapStatus(String phase) {
        if (phase == null) return StepExecution.Status.RUNNING;
        return switch (phase) {
            case "START" -> StepExecution.Status.RUNNING;
            case "COMPLETE" -> StepExecution.Status.SUCCESS;
            case "FAIL" -> StepExecution.Status.FAILED;
            default -> StepExecution.Status.RUNNING;
        };
    }
}
