package com.flow.flowboard.common.dto.playground;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for playground HTTP request execution
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaygroundExecuteResponse {

    /**
     * HTTP status code from the response
     */
    private int status;

    /**
     * HTTP status text (e.g., "OK", "Not Found")
     */
    private String statusText;

    /**
     * Response headers
     */
    private Map<String, String> headers;

    /**
     * Response body (parsed as JSON object if possible, otherwise string)
     */
    private Object body;

    /**
     * Duration of the request in milliseconds
     */
    private long durationMs;

    /**
     * Error message if the request failed
     */
    private String error;
}
