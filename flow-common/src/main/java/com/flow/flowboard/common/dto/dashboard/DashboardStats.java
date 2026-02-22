package com.flow.flowboard.common.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private long totalWorkflows;
    private long activeExecutions;
    private long successfulExecutions;
    private long failedExecutions;
    private Long avgExecutionTime; // in milliseconds
}
