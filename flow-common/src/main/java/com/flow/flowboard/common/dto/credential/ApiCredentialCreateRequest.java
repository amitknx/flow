package com.flow.flowboard.common.dto.credential;

import com.flow.flowboard.common.domain.AuthType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for creating a new API credential
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiCredentialCreateRequest {

    /**
     * Unique name identifier for the credential (used in workflow references)
     */
    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 200, message = "Name must be between 1 and 200 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Name can only contain alphanumeric characters, hyphens, and underscores")
    private String name;

    /**
     * Human-readable description of the API credential
     */
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    /**
     * Base URL for the API (e.g., https://api.example.com)
     */
    @NotBlank(message = "Base URL is required")
    @Size(max = 500, message = "Base URL must not exceed 500 characters")
    @Pattern(regexp = "^https?://.*", message = "Base URL must be a valid HTTP or HTTPS URL")
    private String baseUrl;

    /**
     * Authentication type for this credential
     */
    @NotNull(message = "Auth type is required")
    private AuthType authType;

    /**
     * Authentication credentials (structure depends on authType)
     * Will be encrypted before storage
     */
    @NotNull(message = "Credential data is required")
    private Map<String, Object> credentialData;

    /**
     * Default HTTP headers to include in all requests
     * Common auth-related headers like Accept, User-Agent, etc.
     */
    private Map<String, String> defaultHeaders;
}
