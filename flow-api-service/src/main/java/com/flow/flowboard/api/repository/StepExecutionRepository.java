package com.flow.flowboard.api.repository;

import com.flow.flowboard.api.entity.StepExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StepExecutionRepository extends JpaRepository<StepExecution, UUID> {

    List<StepExecution> findByWorkflowExecution_IdAndActiveTrueOrderByCreatedAt(UUID workflowExecutionId);

    @Query("SELECT COUNT(se) FROM StepExecution se WHERE se.workflowExecution.id = :executionId AND se.active = true")
    long countByWorkflowExecutionId(@Param("executionId") UUID executionId);

    @Query("SELECT COUNT(se) FROM StepExecution se WHERE se.workflowExecution.id = :executionId AND se.active = true AND se.status = 'SUCCESS'")
    long countCompletedByWorkflowExecutionId(@Param("executionId") UUID executionId);
}
