package com.flow.flowboard.common.dto.api;

import com.flow.flowboard.common.domain.HttpMethod;
import com.flow.flowboard.common.dto.credential.RetryConfigDto;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/**
 * DTO for API endpoint (can be used standalone or nested in API create/update)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiEndpointDto {

    /**
     * Endpoint ID (optional for create, required for update)
     */
    private UUID id;

    /**
     * Version number for optimistic locking (required for update)
     */
    private Long versionNum;

    /**
     * Endpoint name (e.g., "Get User", "Create Payment")
     */
    @NotBlank(message = "Endpoint name is required")
    @Size(min = 1, max = 200, message = "Endpoint name must be between 1 and 200 characters")
    private String name;

    /**
     * Description of the endpoint
     */
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    /**
     * HTTP method
     */
    @NotNull(message = "HTTP method is required")
    private HttpMethod httpMethod;

    /**
     * Endpoint path (appended to API base URL)
     * Supports variables: /users/{{userId}}/repos
     */
    @NotBlank(message = "Endpoint path is required")
    @Size(max = 1000, message = "Endpoint path must not exceed 1000 characters")
    private String endpointPath;

    /**
     * Path parameter definitions
     * Example: {"userId": {"type": "string", "required": true, "description": "User ID"}}
     */
    private Map<String, Object> pathParams;

    /**
     * Query parameter definitions
     * Example: {"page": {"type": "integer", "default": 1}, "limit": {"default": 10}}
     */
    private Map<String, Object> queryParams;

    /**
     * Additional headers specific to this endpoint
     */
    private Map<String, String> headers;

    /**
     * Request body template (for POST/PUT/PATCH)
     */
    private Object requestBodyTemplate;

    /**
     * Content type for request body
     */
    private String contentType;

    /**
     * Response mapping (JSONPath expressions)
     */
    private Map<String, String> responseMapping;

    /**
     * Endpoint-specific timeout override (ms)
     */
    @Min(value = 1000, message = "Timeout must be at least 1000ms")
    @Max(value = 300000, message = "Timeout cannot exceed 300000ms")
    private Integer timeoutMs;

    /**
     * Endpoint-specific retry configuration
     */
    private RetryConfigDto retryConfig;
}
