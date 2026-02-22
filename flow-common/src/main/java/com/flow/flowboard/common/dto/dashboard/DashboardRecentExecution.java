package com.flow.flowboard.common.dto.dashboard;

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
public class DashboardRecentExecution {
    private UUID id;
    private String workflowName;
    private ExecutionStatus status;
    private Long duration; // in milliseconds
    private Instant startedAt;
}
