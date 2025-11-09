package com.flow.flowboard.api.service;

import com.flow.flowboard.api.entity.Secret;
import com.flow.flowboard.api.exception.SecretNotFoundException;
import com.flow.flowboard.api.exception.DuplicateSecretNameException;
import com.flow.flowboard.api.repository.SecretRepository;
import com.flow.flowboard.api.util.EncryptionUtil;
import com.flow.flowboard.common.dto.secret.SecretCreateRequest;
import com.flow.flowboard.common.dto.secret.SecretResponse;
import com.flow.flowboard.common.dto.secret.SecretUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing secrets
 * Handles CRUD operations, validation, and data masking for security
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecretService {

    private final SecretRepository secretRepository;

    @Transactional
    public SecretResponse createSecret(SecretCreateRequest request) {
        // Validate unique name
        if (secretRepository.existsByNameIgnoreCaseAndActiveTrue(request.getName())) {
            throw new DuplicateSecretNameException(request.getName());
        }

        // Create entity
        Secret secret = Secret.builder()
                .name(request.getName())
                .description(request.getDescription())
                .secretData(request.getSecrets())
                .tags(request.getTags())
                .build();

        Secret saved = secretRepository.save(secret);

        log.info("Created secret: {}", saved.getName());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public SecretResponse getSecretById(UUID id) {
        Secret secret = secretRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new SecretNotFoundException(id));

        return toResponse(secret);
    }

    @Transactional(readOnly = true)
    public SecretResponse getSecretByName(String name) {
        Secret secret = secretRepository.findByNameAndActiveTrue(name)
                .orElseThrow(() -> new SecretNotFoundException(name));

        return toResponse(secret);
    }

    @Transactional(readOnly = true)
    public Page<SecretResponse> getAllSecrets(Pageable pageable) {
        return secretRepository.findByActiveTrue(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SecretResponse> searchSecrets(String searchTerm, Pageable pageable) {
        return secretRepository.searchActiveSecrets(searchTerm, pageable)
                .map(this::toResponse);
    }

    @Transactional
    public SecretResponse updateSecret(UUID id, SecretUpdateRequest request) {
        Secret secret = secretRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new SecretNotFoundException(id));

        // Update fields selectively
        if (request.getDescription() != null) {
            secret.setDescription(request.getDescription());
        }
        if (request.getTags() != null) {
            secret.setTags(request.getTags());
        }
        if (request.getSecretData() != null) {
            secret.setSecretData(request.getSecretData());
        }
        if (request.getActive() != null) {
            secret.setActive(request.getActive());
        }

        Secret updated = secretRepository.save(secret);

        log.info("Updated secret: {}", updated.getName());

        return toResponse(updated);
    }

    @Transactional
    public void deleteSecret(UUID id) {
        Secret secret = secretRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new SecretNotFoundException(id));

        // Soft delete
        secret.setActive(false);
        secretRepository.save(secret);

        log.info("Deleted secret: {}", secret.getName());
    }

    /**
     * Get decrypted secret data for internal use (e.g., workflow execution)
     * SECURITY: Never expose this in API responses
     */
    @Transactional(readOnly = true)
    public Map<String, String> getDecryptedSecretData(String name) {
        Secret secret = secretRepository.findByNameAndActiveTrue(name)
                .orElseThrow(() -> new SecretNotFoundException(name));

        // secretData is automatically decrypted by JPA converter
        Map<String, String> secretData = secret.getSecretData();
        return secretData != null ? secretData : new HashMap<>();
    }

    /**
     * Convert entity to response DTO with masked sensitive data
     */
    private SecretResponse toResponse(Secret secret) {
        return SecretResponse.builder()
                .id(secret.getId())
                .name(secret.getName())
                .description(secret.getDescription())
                .tags(secret.getTags())
                .secretDataMasked(maskSecretData(secret))
                .active(secret.getActive())
                .createdAt(secret.getCreatedAt())
                .createdBy(secret.getCreatedBy())
                .modifiedAt(secret.getModifiedAt())
                .modifiedBy(secret.getModifiedBy())
                .build();
    }

    /**
     * Mask sensitive secret fields for API responses
     */
    private Map<String, Object> maskSecretData(Secret secret) {
        if (secret.getSecretData() == null) {
            return null;
        }

        Map<String, String> decrypted = secret.getSecretData();
        Map<String, Object> masked = new HashMap<>();

        for (Map.Entry<String, String> entry : decrypted.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            // Mask sensitive values based on key name
            if (value != null && isSensitiveKey(key)) {
                masked.put(key, EncryptionUtil.mask(value));
            } else {
                // Keep non-sensitive values as-is
                masked.put(key, value);
            }
        }

        return masked;
    }

    /**
     * Determine if a key contains sensitive data
     */
    private boolean isSensitiveKey(String key) {
        String lowerKey = key.toLowerCase();
        return lowerKey.contains("key") ||
                lowerKey.contains("token") ||
                lowerKey.contains("secret") ||
                lowerKey.contains("password") ||
                lowerKey.contains("credentials");
    }
}
