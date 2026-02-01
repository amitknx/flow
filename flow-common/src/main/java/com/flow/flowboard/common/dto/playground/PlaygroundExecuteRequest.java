package com.flow.flowboard.common.dto.playground;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for executing an arbitrary HTTP request from the API Playground
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaygroundExecuteRequest {

    /**
     * HTTP method (GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS)
     */
    @NotBlank(message = "HTTP method is required")
    @Pattern(regexp = "^(GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS)$", message = "Invalid HTTP method")
    private String method;

    /**
     * Full URL to execute the request against
     */
    @NotBlank(message = "URL is required")
    private String url;

    /**
     * Request headers to include
     */
    private Map<String, String> headers;

    /**
     * Request body (for POST, PUT, PATCH requests)
     */
    private Object body;

    /**
     * Timeout in milliseconds (default: 30000)
     */
    @Builder.Default
    private Integer timeoutMs = 30000;
}
