package com.flow.flowboard.api.service.impl;

import com.flow.flowboard.api.repository.WorkflowExecutionRepository;
import com.flow.flowboard.api.repository.WorkflowRepository;
import com.flow.flowboard.api.service.DashboardService;
import com.flow.flowboard.common.domain.ExecutionStatus;
import com.flow.flowboard.common.dto.dashboard.DashboardRecentExecution;
import com.flow.flowboard.common.dto.dashboard.DashboardResponse;
import com.flow.flowboard.common.dto.dashboard.DashboardStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

        private final WorkflowRepository workflowRepository;
        private final WorkflowExecutionRepository workflowExecutionRepository;

        @Override
        @Transactional(readOnly = true)
        public DashboardResponse getDashboardStats() {
                log.debug("Fetching dashboard statistics");

                long totalWorkflows = workflowRepository.countByActiveTrue();

                long activeExecutions = workflowExecutionRepository.countByActiveTrueAndStatusIn(
                                Set.of(ExecutionStatus.PENDING, ExecutionStatus.RUNNING));

                long successfulExecutions = workflowExecutionRepository.countByActiveTrueAndStatusIn(
                                Set.of(ExecutionStatus.SUCCESS));

                long failedExecutions = workflowExecutionRepository.countByActiveTrueAndStatusIn(
                                Set.of(ExecutionStatus.FAILED));

                Double avgExecutionTime = workflowExecutionRepository.getAverageExecutionTimeInMillis();
                Long avgExecutionTimeLong = avgExecutionTime != null ? Math.round(avgExecutionTime) : 0L;

                DashboardStats stats = DashboardStats.builder()
                                .totalWorkflows(totalWorkflows)
                                .activeExecutions(activeExecutions)
                                .successfulExecutions(successfulExecutions)
                                .failedExecutions(failedExecutions)
                                .avgExecutionTime(avgExecutionTimeLong)
                                .build();

                // Fetch top 5 recent executions
                List<DashboardRecentExecution> recentExecutions = workflowExecutionRepository
                                .findRecentExecutions(PageRequest.of(0, 5))
                                .stream()
                                .map(proj -> DashboardRecentExecution.builder()
                                                .id(proj.getId())
                                                .workflowName(proj.getWorkflowName())
                                                .status(proj.getStatus())
                                                .duration(proj.getDuration() != null ? proj.getDuration()
                                                                : null)
                                                .startedAt(proj.getStartedAt())
                                                .build())
                                .toList();

                return DashboardResponse.builder()
                                .stats(stats)
                                .recentExecutions(recentExecutions)
                                .build();
        }
}
