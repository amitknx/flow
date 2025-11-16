package com.flow.flowboard.api.repository;

import com.flow.flowboard.api.entity.ApiEndpoint;
import com.flow.flowboard.common.domain.HttpMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for ApiEndpoint entity
 */
@Repository
public interface ApiEndpointRepository extends JpaRepository<ApiEndpoint, UUID> {

    /**
     * Find endpoint by ID with eagerly loaded API (for execution)
     */
    @Query("SELECT e FROM ApiEndpoint e JOIN FETCH e.api WHERE e.id = :id")
    Optional<ApiEndpoint> findByIdWithApi(@Param("id") UUID id);

    /**
     * Find endpoint by ID (only active records)
     */
    Optional<ApiEndpoint> findByIdAndActiveTrue(UUID id);

    /**
     * Find endpoint by name within an API
     */
    Optional<ApiEndpoint> findByApi_IdAndNameAndActiveTrue(UUID apiId, String name);

    /**
     * List all endpoints for a specific API
     */
    List<ApiEndpoint> findByApi_IdAndActiveTrueOrderByCreatedAt(UUID apiId);

    /**
     * List all endpoints for a specific API with pagination
     */
    Page<ApiEndpoint> findByApi_IdAndActiveTrue(UUID apiId, Pageable pageable);

    /**
     * List all active endpoints with pagination
     */
    Page<ApiEndpoint> findByActiveTrue(Pageable pageable);

    /**
     * Find endpoints by HTTP method
     */
    Page<ApiEndpoint> findByHttpMethodAndActiveTrue(HttpMethod httpMethod, Pageable pageable);

    /**
     * Search endpoints by name or description (case-insensitive)
     */
    @Query("SELECT e FROM ApiEndpoint e WHERE e.active = true AND " +
            "(LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<ApiEndpoint> searchActiveEndpoints(@Param("search") String searchTerm, Pageable pageable);

    /**
     * Search endpoints within a specific API
     */
    @Query("SELECT e FROM ApiEndpoint e WHERE e.api.id = :apiId AND e.active = true AND " +
            "(LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<ApiEndpoint> searchActiveEndpointsByApi(
            @Param("apiId") UUID apiId,
            @Param("search") String searchTerm,
            Pageable pageable
    );

    /**
     * Check if an endpoint name already exists within an API
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM ApiEndpoint e " +
            "WHERE e.api.id = :apiId AND e.active = true AND LOWER(e.name) = LOWER(:name)")
    boolean existsByApiIdAndNameIgnoreCaseAndActiveTrue(
            @Param("apiId") UUID apiId,
            @Param("name") String name
    );

    /**
     * Count endpoints in an API
     */
    long countByApi_IdAndActiveTrue(UUID apiId);

    /**
     * Count all active endpoints
     */
    long countByActiveTrue();
}
