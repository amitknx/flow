package com.flow.flowboard.api.repository;

import com.flow.flowboard.api.entity.WorkflowExecution;
import com.flow.flowboard.common.domain.ExecutionStatus;
import com.flow.flowboard.common.dto.dashboard.DashboardRecentExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkflowExecutionRepository extends JpaRepository<WorkflowExecution, UUID> {

    Page<WorkflowExecution> findByActiveTrue(Pageable pageable);

    Optional<WorkflowExecution> findByIdAndActiveTrue(UUID id);

    Page<WorkflowExecution> findByWorkflow_IdAndActiveTrue(UUID workflowId, Pageable pageable);

    Page<WorkflowExecution> findByCorrelationIdAndActiveTrue(String correlationId, Pageable pageable);

    @Query("SELECT we FROM WorkflowExecution we WHERE we.active = true AND we.status = :status")
    Page<WorkflowExecution> findByStatusAndActiveTrue(@Param("status") ExecutionStatus status, Pageable pageable);

    long countByActiveTrueAndStatusIn(Collection<ExecutionStatus> statuses);

    @Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (ended_at - started_at))) * 1000 " +
            "FROM workflow_executions " +
            "WHERE active = true AND status IN ('SUCCESS', 'FAILED') AND started_at IS NOT NULL AND ended_at IS NOT NULL", nativeQuery = true)
    Double getAverageExecutionTimeInMillis();

    @Query("SELECT new com.flow.flowboard.common.dto.dashboard.DashboardRecentExecution(" +
            "we.id, w.name, we.status, " +
            "CAST((CAST(EXTRACT(EPOCH FROM (we.endedAt)) AS double) - CAST(EXTRACT(EPOCH FROM (we.startedAt)) AS double)) * 1000 AS Long), " +
            "we.startedAt) " +
            "FROM WorkflowExecution we JOIN we.workflow w " +
            "WHERE we.active = true " +
            "ORDER BY we.startedAt DESC")
    List<DashboardRecentExecution> findRecentExecutions(
            Pageable pageable);
}
