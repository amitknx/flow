package com.flow.flowboard.common.dto.execution;

import com.flow.flowboard.common.dto.auth.ApiAuthenticationDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for executing an API endpoint
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteEndpointRequest {

    /**
     * Path parameters to substitute in the endpoint path
     * Example: {"userId": "123", "repoId": "456"}
     * For endpoint path: /users/{{userId}}/repos/{{repoId}}
     */
    private Map<String, String> pathParams;

    /**
     * Query parameters to add to the request
     * Example: {"page": "1", "limit": "10"}
     */
    private Map<String, String> queryParams;

    /**
     * Additional headers to add to the request
     * These will be merged with headers from the endpoint definition
     * Example: {"X-Custom-Header": "value"}
     */
    private Map<String, String> headers;

    /**
     * Request body for POST/PUT/PATCH requests
     * Can be any JSON-serializable object
     */
    private Object requestBody;

    /**
     * Optional: Override authentication for this specific execution
     * If not provided, uses authentication from the parent API
     */
    @Valid
    private ApiAuthenticationDto overrideAuthentication;

    /**
     * Optional: Override timeout for this execution (milliseconds)
     * If not provided, uses timeout from endpoint or API
     */
    private Integer timeoutMs;
}
