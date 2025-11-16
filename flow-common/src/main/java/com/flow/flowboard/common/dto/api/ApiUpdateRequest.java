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

/**
 * Request DTO for updating an existing API
 * All fields are optional - only provided fields will be updated
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiUpdateRequest {

    /**
     * Version number for optimistic locking (REQUIRED for updates to prevent dirty writes)
     */
    @NotNull(message = "Version number is required for updates")
    private Long versionNum;

    /**
     * Updated API name
     */
    @Size(min = 1, max = 200, message = "API name must be between 1 and 200 characters")
    private String name;

    /**
     * Updated API version string (e.g., "v1", "v2")
     */
    @Size(max = 50, message = "Version must not exceed 50 characters")
    private String version;

    /**
     * Updated base URL
     */
    @Size(max = 500, message = "Base URL must not exceed 500 characters")
    @Pattern(regexp = "^https?://.*", message = "Base URL must be a valid HTTP or HTTPS URL")
    private String baseUrl;

    /**
     * Updated description
     */
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    /**
     * Updated authentication configuration
     */
    @Valid
    private ApiAuthenticationDto authentication;

    /**
     * Updated tags
     */
    private List<String> tags;

    /**
     * Updated default timeout
     */
    @Min(value = 1000, message = "Timeout must be at least 1000ms")
    @Max(value = 300000, message = "Timeout cannot exceed 300000ms")
    private Integer timeoutMs;

    /**
     * Updated default retry configuration
     */
    @Valid
    private RetryConfigDto retryConfig;

    /**
     * Updated endpoints (replaces all existing endpoints)
     */
    @Valid
    private List<ApiEndpointDto> endpoints;

    /**
     * Soft delete flag
     */
    private Boolean active;
}
