package com.flow.flowboard.common.dto.execution;

import com.flow.flowboard.common.domain.ExecutionStatus;
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
public class ExecutionStatusResponse {

    private UUID id;
    private ExecutionStatus status;
    private Integer completedSteps;
    private Integer totalSteps;
    private Integer progressPercentage;
    private Instant startedAt;
    private Instant endedAt;
}
