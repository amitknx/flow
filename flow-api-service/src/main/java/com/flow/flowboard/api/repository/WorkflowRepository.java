package com.flow.flowboard.api.repository;

import com.flow.flowboard.api.entity.Workflow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WorkflowRepository extends JpaRepository<Workflow, UUID> {

    Page<Workflow> findByActiveTrue(Pageable pageable);

    @Query("SELECT w FROM Workflow w WHERE w.active = true AND " +
           "(LOWER(w.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(w.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Workflow> searchActiveWorkflows(@Param("search") String search, Pageable pageable);
}
