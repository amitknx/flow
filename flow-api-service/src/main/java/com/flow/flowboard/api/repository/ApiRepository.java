package com.flow.flowboard.api.repository;

import com.flow.flowboard.api.entity.Api;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Api entity
 */
@Repository
public interface ApiRepository extends JpaRepository<Api, UUID> {

    /**
     * Find API by name and version
     */
    Optional<Api> findByNameAndVersionAndActiveTrue(String name, String version);

    /**
     * Find API by ID (only active records)
     */
    Optional<Api> findByIdAndActiveTrue(UUID id);

    /**
     * List all active APIs with pagination
     */
    Page<Api> findByActiveTrue(Pageable pageable);

    /**
     * Search APIs by name or description (case-insensitive)
     */
    @Query("SELECT a FROM Api a WHERE a.active = true AND " +
            "(LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(a.version) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Api> searchActiveApis(@Param("search") String searchTerm, Pageable pageable);

    /**
     * Check if an API with name and version already exists
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Api a " +
            "WHERE a.active = true AND LOWER(a.name) = LOWER(:name) " +
            "AND (:version IS NULL OR LOWER(a.version) = LOWER(:version))")
    boolean existsByNameAndVersionIgnoreCaseAndActiveTrue(
            @Param("name") String name,
            @Param("version") String version
    );

    /**
     * Count active APIs
     */
    long countByActiveTrue();

}
