package com.flow.flowboard.common.dto.execution;

import com.flow.flowboard.common.domain.StepStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepExecutionResponse {

    private UUID id;
    private UUID workflowExecutionId;
    private String nodeIdRef;
    private String stepName;
    private StepStatus status;
    private Object inputPayload;
    private Object outputPayload;
    private String errorMessage;
    private Instant startedAt;
    private Instant endedAt;
    private Long durationMs;
}
