package com.flow.flowboard.api.repository;

import com.flow.flowboard.api.entity.Secret;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Secret entity
 * Provides data access methods following Spring Data JPA conventions
 */
@Repository
public interface SecretRepository extends JpaRepository<Secret, UUID> {

    /**
     * Find secret by name (case-sensitive)
     *
     * @param name secret name
     * @return Optional containing the secret if found
     */
    Optional<Secret> findByNameAndActiveTrue(String name);

    /**
     * Find secret by ID (only active records)
     *
     * @param id secret ID
     * @return Optional containing the secret if found and active
     */
    Optional<Secret> findByIdAndActiveTrue(UUID id);

    /**
     * List all active secrets with pagination
     *
     * @param pageable pagination parameters
     * @return page of active secrets
     */
    Page<Secret> findByActiveTrue(Pageable pageable);

    /**
     * Search secrets by name or description (case-insensitive)
     *
     * @param searchTerm search term
     * @param pageable pagination parameters
     * @return page of matching secrets
     */
    @Query("SELECT s FROM Secret s WHERE s.active = true AND " +
            "(LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Secret> searchActiveSecrets(@Param("search") String searchTerm, Pageable pageable);

    /**
     * Check if a secret name already exists (case-insensitive)
     * Useful for validation before creation
     *
     * @param name secret name
     * @return true if name exists
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Secret s " +
            "WHERE s.active = true AND LOWER(s.name) = LOWER(:name)")
    boolean existsByNameIgnoreCaseAndActiveTrue(@Param("name") String name);

    /**
     * Count active secrets
     *
     * @return total count of active secrets
     */
    long countByActiveTrue();
}
