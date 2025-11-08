package com.flow.flowboard.api.repository;

import com.flow.flowboard.api.entity.WorkflowExecution;
import com.flow.flowboard.common.domain.ExecutionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
