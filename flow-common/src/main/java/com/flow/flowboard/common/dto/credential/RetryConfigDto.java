package com.flow.flowboard.common.dto.credential;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Configuration for HTTP request retry behavior
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetryConfigDto {

    /**
     * Maximum number of retry attempts (0-5)
     */
    @Min(value = 0, message = "Max retries must be at least 0")
    @Max(value = 5, message = "Max retries cannot exceed 5")
    @Builder.Default
    private Integer maxRetries = 3;

    /**
     * Initial backoff duration in milliseconds
     */
    @Min(value = 100, message = "Backoff must be at least 100ms")
    @Max(value = 60000, message = "Backoff cannot exceed 60000ms")
    @Builder.Default
    private Integer backoffMs = 1000;

    /**
     * HTTP status codes that should trigger a retry (e.g., 429, 502, 503, 504)
     */
    private List<Integer> retryableStatuses;

    /**
     * Whether to use exponential backoff (doubles backoff after each retry)
     */
    @Builder.Default
    private Boolean exponentialBackoff = true;
}
