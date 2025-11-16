package com.flow.flowboard.common.dto.execution;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for endpoint execution
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteEndpointResponse {

    /**
     * Execution ID (if saved to database)
     */
    private UUID executionId;

    /**
     * Execution status
     */
    private String status;

    /**
     * HTTP status code from the response
     */
    private int httpStatusCode;

    /**
     * HTTP status text (e.g., "OK", "Not Found")
     */
    private String httpStatusText;

    /**
     * Duration of the request in milliseconds
     */
    private Long durationMs;

    /**
     * Timestamp when request was sent
     */
    private Instant requestTimestamp;

    /**
     * Timestamp when response was received
     */
    private Instant responseTimestamp;

    /**
     * Response headers
     */
    private Map<String, String> responseHeaders;

    /**
     * Response body (parsed as JSON object if possible, otherwise string)
     */
    private Object responseBody;

    /**
     * Error message if execution failed
     */
    private String errorMessage;

    /**
     * Full request URL that was executed
     */
    private String requestUrl;

    /**
     * HTTP method used
     */
    private String httpMethod;
}
