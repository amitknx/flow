package com.flow.flowboard.api.service;

import com.flow.flowboard.common.dto.dashboard.DashboardResponse;

public interface DashboardService {

    /**
     * Gets the dashboard statistics and recent executions.
     *
     * @return DashboardResponse containing stats and recent executions
     */
    DashboardResponse getDashboardStats();
}
