package com.flow.flowboard.api.util;

import com.flow.flowboard.common.dto.credential.RetryConfigDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Handles retry logic configuration for API requests
 */
@Component
@Slf4j
public class RetryHandler {

    /**
     * Build retry specification based on retry config
     */
    public Retry buildRetrySpec(RetryConfigDto retryConfig) {
        if (retryConfig == null || retryConfig.getMaxRetries() == null || retryConfig.getMaxRetries() <= 0) {
            return null;
        }

        long backoffMs = retryConfig.getBackoffMs() != null ? retryConfig.getBackoffMs() : 1000;
        int maxRetries = retryConfig.getMaxRetries();

        if (Boolean.TRUE.equals(retryConfig.getExponentialBackoff())) {
            return Retry.backoff(maxRetries, Duration.ofMillis(backoffMs))
                    .filter(throwable -> isRetryableError(throwable, retryConfig))
                    .doBeforeRetry(retrySignal ->
                            log.info("Retrying request with exponential backoff (attempt {}/{})",
                                    retrySignal.totalRetries() + 1, maxRetries));
        } else {
            return Retry.fixedDelay(maxRetries, Duration.ofMillis(backoffMs))
                    .filter(throwable -> isRetryableError(throwable, retryConfig))
                    .doBeforeRetry(retrySignal ->
                            log.info("Retrying request with fixed delay (attempt {}/{})",
                                    retrySignal.totalRetries() + 1, maxRetries));
        }
    }

    /**
     * Determine if error is retryable based on configuration
     */
    private boolean isRetryableError(Throwable throwable, RetryConfigDto retryConfig) {
        if (throwable instanceof WebClientResponseException webClientEx) {
            int statusCode = webClientEx.getStatusCode().value();

            // Check if status code is in configured retryable statuses
            if (retryConfig.getRetryableStatuses() != null && !retryConfig.getRetryableStatuses().isEmpty()) {
                return retryConfig.getRetryableStatuses().contains(statusCode);
            }

            // Default retryable statuses: 408 (Request Timeout), 429 (Too Many Requests), 5xx (Server Errors)
            return statusCode == 408 || statusCode == 429 || (statusCode >= 500 && statusCode < 600);
        }

        // Retry on network errors (connection timeout, connection refused, etc.)
        return true;
    }
}
