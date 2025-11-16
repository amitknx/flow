package com.flow.flowboard.common.dto.api;

import com.flow.flowboard.common.dto.auth.ApiAuthenticationDto;
import com.flow.flowboard.common.dto.credential.RetryConfigDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO for creating a new API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiCreateRequest {

    /**
     * API name (e.g., "GitHub API", "Stripe API")
     */
    @NotBlank(message = "API name is required")
    @Size(min = 1, max = 200, message = "API name must be between 1 and 200 characters ")
    private String name;

    /**
     * API version (e.g., "v3", "v1", "2023-10")
     */
    @Size(max = 50, message = "Version must not exceed 50 characters")
    private String version;

    /**
     * Base URL for all endpoints
     */
    @NotBlank(message = "Base URL is required")
    @Size(max = 500, message = "Base URL must not exceed 500 characters")
    @Pattern(regexp = "^https?://.*", message = "Base URL must be a valid HTTP or HTTPS URL")
    private String baseUrl;

    /**
     * Description
     */
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    /**
     * Authentication configuration
     * Supports: NoAuth, Basic, Bearer, API Key, OAuth2, Custom
     */
    @Valid
    private ApiAuthenticationDto authentication;

    /**
     * Tags for categorization
     */
    private List<String> tags;

    /**
     * Default timeout for all endpoints in this API (milliseconds)
     */
    @Min(value = 1000, message = "Timeout must be at least 1000ms")
    @Max(value = 300000, message = "Timeout cannot exceed 300000ms")
    @Builder.Default
    private Integer timeoutMs = 30000;

    /**
     * Default retry configuration for all endpoints
     */
    @Valid
    private RetryConfigDto retryConfig;

    /**
     * Endpoints to create with this API (optional - can add endpoints later)
     */
    @Valid
    private List<ApiEndpointDto> endpoints;
}
