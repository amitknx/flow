package com.flow.flowboard.api.controller;

import com.flow.flowboard.api.service.SecretService;
import com.flow.flowboard.common.dto.secret.SecretCreateRequest;
import com.flow.flowboard.common.dto.secret.SecretResponse;
import com.flow.flowboard.common.dto.secret.SecretUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for secret management
 *
 * Endpoints:
 * - POST   /api/v1/secrets              - Create new secret
 * - GET    /api/v1/secrets              - List all secrets (paginated)
 * - GET    /api/v1/secrets/{id}         - Get secret by ID
 * - GET    /api/v1/secrets/name/{name}  - Get secret by name
 * - PUT    /api/v1/secrets/{id}         - Update secret
 * - DELETE /api/v1/secrets/{id}         - Delete secret (soft delete)
 *
 * Security Notes:
 * - All responses mask sensitive secret data
 * - Only authorized users should have access (requires authentication when implemented)
 * - Audit trail is automatically maintained via AuditableEntity
 */
@RestController
@RequestMapping("/api/v1/secrets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class SecretController {

    private final SecretService secretService;

    /**
     * Create a new secret
     *
     * @param request secret creation request
     * @return created secret with masked sensitive data
     */
    @PostMapping
    public ResponseEntity<SecretResponse> createSecret(
            @Valid @RequestBody SecretCreateRequest request) {
        SecretResponse response = secretService.createSecret(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get secret by ID
     *
     * @param id secret ID
     * @return secret with masked sensitive data
     */
    @GetMapping("/{id}")
    public ResponseEntity<SecretResponse> getSecretById(
            @PathVariable("id") UUID id) {
        SecretResponse response = secretService.getSecretById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get secret by name
     *
     * @param name secret name
     * @return secret with masked sensitive data
     */
    @GetMapping("/name/{name}")
    public ResponseEntity<SecretResponse> getSecretByName(
            @PathVariable("name") String name) {
        SecretResponse response = secretService.getSecretByName(name);
        return ResponseEntity.ok(response);
    }

    /**
     * List all secrets with optional search
     *
     * @param search optional search term for name/description
     * @param pageable pagination parameters
     * @return page of secrets with masked sensitive data
     */
    @GetMapping
    public ResponseEntity<Page<SecretResponse>> getAllSecrets(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable) {

        Page<SecretResponse> response;
        if (search != null && !search.isBlank()) {
            response = secretService.searchSecrets(search, pageable);
        } else {
            response = secretService.getAllSecrets(pageable);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing secret
     *
     * @param id secret ID
     * @param request update request with fields to change
     * @return updated secret with masked sensitive data
     */
    @PutMapping("/{id}")
    public ResponseEntity<SecretResponse> updateSecret(
            @PathVariable("id") UUID id,
            @Valid @RequestBody SecretUpdateRequest request) {
        SecretResponse response = secretService.updateSecret(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a secret (soft delete)
     *
     * @param id secret ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSecret(@PathVariable("id") UUID id) {
        secretService.deleteSecret(id);
        return ResponseEntity.noContent().build();
    }
}
