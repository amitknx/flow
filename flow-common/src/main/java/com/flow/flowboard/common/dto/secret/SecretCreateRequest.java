package com.flow.flowboard.common.dto.secret;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for creating a new secret
 * Secrets are simple key-value stores for sensitive data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecretCreateRequest {

    /**
     * Unique name identifier for the secret (used in API references)
     * Example: "github_prod", "stripe_test"
     */
    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 200, message = "Name must be between 1 and 200 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Name can only contain alphanumeric characters, hyphens, and underscores")
    private String name;

    /**
     * Human-readable description of the secret
     */
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    /**
     * Tags for categorization
     * Example: ["production", "github", "oauth"]
     */
    private List<String> tags;

    /**
     * Secret data as key-value pairs
     * Example: {"api_key": "sk-123", "client_id": "abc", "client_secret": "xyz"}
     * Will be encrypted before storage
     */
    @NotNull(message = "Secret data is required")
    @Size(min = 1, message = "Secret data must contain at least one key-value pair")
    private Map<String, String> secrets;
}
