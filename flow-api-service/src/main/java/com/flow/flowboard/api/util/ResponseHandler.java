package com.flow.flowboard.api.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flow.flowboard.api.entity.ApiEndpoint;
import com.flow.flowboard.common.dto.execution.ExecuteEndpointResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Handles response and error processing for API execution
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ResponseHandler {

    private final ObjectMapper objectMapper;

    /**
     * Handle successful response
     */
    public ExecuteEndpointResponse handleResponse(
            Map<String, String> responseHeaders,
            HttpStatus status,
            Instant startTime,
            String requestUrl,
            ApiEndpoint endpoint,
            String body) {

        Instant endTime = Instant.now();
        long durationMs = Duration.between(startTime, endTime).toMillis();
//        org.springframework.http.HttpStatus status =
//                (org.springframework.http.HttpStatus) response.statusCode();

        return ExecuteEndpointResponse.builder()
                .status(status.is2xxSuccessful() ? "SUCCESS" : "FAILED")
                .httpStatusCode(status.value())
                .httpStatusText(status.getReasonPhrase())
                .requestTimestamp(startTime)
                .responseTimestamp(endTime)
                .durationMs(durationMs)
                .requestUrl(requestUrl)
                .httpMethod(endpoint.getHttpMethod().name())
                .responseHeaders(responseHeaders)
                .responseBody(parseResponseBody(body))
                .build();
    }

    /**
     * Handle error response
     * Unwraps RetryExhaustedException and maps errors to appropriate HTTP status codes
     */
    public Mono<ExecuteEndpointResponse> handleError(
            Throwable error,
            Instant startTime,
            String requestUrl,
            ApiEndpoint endpoint) {

        Instant endTime = Instant.now();
        long durationMs = Duration.between(startTime, endTime).toMillis();

        // Unwrap RetryExhaustedException to get the actual cause
        Throwable actualError = unwrapError(error);

        log.error("Error executing endpoint {}: {}", endpoint.getId(), actualError.getMessage(), actualError);

        // Build error response with mapped status code
        ErrorInfo errorInfo = mapErrorToHttpStatus(actualError);

        ExecuteEndpointResponse.ExecuteEndpointResponseBuilder responseBuilder = ExecuteEndpointResponse.builder()
                .status("ERROR")
                .requestTimestamp(startTime)
                .responseTimestamp(endTime)
                .durationMs(durationMs)
                .requestUrl(requestUrl)
                .httpMethod(endpoint.getHttpMethod().name())
                .httpStatusCode(errorInfo.httpStatus.value())
                .httpStatusText(errorInfo.httpStatus.getReasonPhrase())
                .errorMessage(errorInfo.errorMessage);

        // Add response body if it's a WebClient error with response
        if (actualError instanceof WebClientResponseException webClientEx) {
            String responseBody = webClientEx.getResponseBodyAsString();
            if (!responseBody.isEmpty()) {
                responseBuilder.responseBody(parseResponseBody(responseBody));
            }
        }

        return Mono.just(responseBuilder.build());
    }

    /**
     * Unwrap RetryExhaustedException to get the actual underlying error
     */
    private Throwable unwrapError(Throwable error) {
        // Check if it's a RetryExhaustedException
        if (Exceptions.isRetryExhausted(error)) {
            // Get the actual cause
            Throwable cause = error.getCause();
            if (cause != null) {
                return unwrapError(cause); // Recursively unwrap nested causes
            }
        }
        return error;
    }

    /**
     * Map exception to HTTP status code and error message
     */
    private ErrorInfo mapErrorToHttpStatus(Throwable error) {
        // WebClient HTTP errors - use actual status from API
        if (error instanceof WebClientResponseException webClientEx) {
            HttpStatus status = HttpStatus.valueOf(webClientEx.getStatusCode().value());
            return new ErrorInfo(status, error.getMessage());
        }

        // Timeout errors
        if (error instanceof TimeoutException) {
            return new ErrorInfo(HttpStatus.REQUEST_TIMEOUT, "Request Timeout: " + error.getMessage());
        }

        // Connection errors (connection refused, reset, etc.)
        if (error instanceof java.net.ConnectException) {
            return new ErrorInfo(HttpStatus.SERVICE_UNAVAILABLE, "Connection Failed: " + error.getMessage());
        }

        // DNS/Host resolution errors
        if (error instanceof java.net.UnknownHostException) {
            return new ErrorInfo(HttpStatus.BAD_GATEWAY, "Unknown Host: " + error.getMessage());
        }

        // Socket timeout
        if (error instanceof java.net.SocketTimeoutException) {
            return new ErrorInfo(HttpStatus.GATEWAY_TIMEOUT, "Socket Timeout: " + error.getMessage());
        }

        // SSL/TLS errors
        if (error instanceof javax.net.ssl.SSLException) {
            return new ErrorInfo(HttpStatus.BAD_GATEWAY, "SSL Error: " + error.getMessage());
        }

        // Generic network/IO errors
        if (error instanceof java.io.IOException) {
            return new ErrorInfo(HttpStatus.BAD_GATEWAY, "Network Error: " + error.getMessage());
        }

        // Default: Internal Server Error
        return new ErrorInfo(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Request Failed: " + (error.getMessage() != null ? error.getMessage() : error.getClass().getSimpleName())
        );
    }

    /**
     * Internal class to hold error information
     */
    private record ErrorInfo(HttpStatus httpStatus, String errorMessage) {
    }

    /**
     * Extract headers from client response
     */
    private Map<String, String> extractHeaders(ClientResponse clientResponse) {
        Map<String, String> headers = new HashMap<>();
        clientResponse.headers().asHttpHeaders().forEach((key, values) ->
                headers.put(key, String.join(", ", values)));
        return headers;
    }

    /**
     * Build success response
     */
    private ExecuteEndpointResponse buildSuccessResponse(
            String body,
            HttpStatus status,
            Instant startTime,
            Instant endTime,
            String requestUrl,
            ApiEndpoint endpoint,
            Map<String, String> responseHeaders) {

        long durationMs = Duration.between(startTime, endTime).toMillis();
        Object parsedBody = parseResponseBody(body);

        return ExecuteEndpointResponse.builder()
                .status(status.is2xxSuccessful() ? "SUCCESS" : "FAILED")
                .httpStatusCode(status.value())
                .httpStatusText(status.getReasonPhrase())
                .requestTimestamp(startTime)
                .responseTimestamp(endTime)
                .durationMs(durationMs)
                .requestUrl(requestUrl)
                .httpMethod(endpoint.getHttpMethod().name())
                .responseHeaders(responseHeaders)
                .responseBody(parsedBody)
                .build();
    }

    /**
     * Parse response body to Object (JSON if possible, otherwise string)
     */
    public Object parseResponseBody(String body) {
        if (body == null || body.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.readValue(body, Object.class);
        } catch (Exception e) {
            // Return as string if not valid JSON
            return body;
        }
    }
}
