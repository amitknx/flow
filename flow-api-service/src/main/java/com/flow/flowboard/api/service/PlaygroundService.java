package com.flow.flowboard.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flow.flowboard.common.dto.playground.PlaygroundExecuteRequest;
import com.flow.flowboard.common.dto.playground.PlaygroundExecuteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Service for executing arbitrary HTTP requests from the API Playground
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlaygroundService {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    private static final int DEFAULT_TIMEOUT_MS = 30000;

    /**
     * Execute an arbitrary HTTP request
     *
     * @param request The playground execution request
     * @return Mono containing the execution response
     */
    public Mono<PlaygroundExecuteResponse> execute(PlaygroundExecuteRequest request) {
        log.info("Playground executing {} {}", request.getMethod(), request.getUrl());
        Instant startTime = Instant.now();

        int timeoutMs = request.getTimeoutMs() != null ? request.getTimeoutMs() : DEFAULT_TIMEOUT_MS;
        HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod().toUpperCase());

        WebClient webClient = webClientBuilder.build();

        WebClient.RequestBodySpec requestSpec = webClient
                .method(httpMethod)
                .uri(request.getUrl());

        // Add headers
        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            request.getHeaders().forEach(requestSpec::header);
        }

        // Add body for methods that support it
        WebClient.RequestHeadersSpec<?> finalSpec;
        if (request.getBody() != null && hasRequestBody(httpMethod)) {
            requestSpec.contentType(MediaType.APPLICATION_JSON);
            finalSpec = requestSpec.bodyValue(request.getBody());
        } else {
            finalSpec = requestSpec;
        }

        return finalSpec
                .exchangeToMono(response -> response.bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .map(body -> buildSuccessResponse(
                                response.statusCode().value(),
                                Objects.requireNonNull(HttpStatus.resolve(response.statusCode().value())),
                                response.headers().asHttpHeaders(),
                                body,
                                startTime)))
                .timeout(Duration.ofMillis(timeoutMs))
                .onErrorResume(error -> Mono.just(buildErrorResponse(error, startTime)));
    }

    private boolean hasRequestBody(HttpMethod method) {
        return method == HttpMethod.POST ||
                method == HttpMethod.PUT ||
                method == HttpMethod.PATCH;
    }

    private PlaygroundExecuteResponse buildSuccessResponse(
            int statusCode,
            HttpStatus httpStatus,
            HttpHeaders httpHeaders,
            String body,
            Instant startTime) {

        long durationMs = Duration.between(startTime, Instant.now()).toMillis();

        // Convert headers to Map<String, String>
        Map<String, String> headers = new HashMap<>();
        httpHeaders.forEach((key, values) -> headers.put(key, String.join(", ", values)));

        // Try to parse body as JSON
        Object parsedBody = parseResponseBody(body);

        return PlaygroundExecuteResponse.builder()
                .status(statusCode)
                .statusText(httpStatus.getReasonPhrase())
                .headers(headers)
                .body(parsedBody)
                .durationMs(durationMs)
                .build();
    }

    private PlaygroundExecuteResponse buildErrorResponse(Throwable error, Instant startTime) {
        long durationMs = Duration.between(startTime, Instant.now()).toMillis();
        log.error("Playground request failed: {}", error.getMessage());

        String errorMessage = error.getMessage();
        if (error instanceof java.util.concurrent.TimeoutException) {
            errorMessage = "Request timed out";
        }

        return PlaygroundExecuteResponse.builder()
                .status(0)
                .statusText("Error")
                .durationMs(durationMs)
                .error(errorMessage)
                .build();
    }

    private Object parseResponseBody(String body) {
        if (body == null || body.isEmpty()) {
            return null;
        }

        try {
            // Try to parse as JSON object or array
            if (body.trim().startsWith("{") || body.trim().startsWith("[")) {
                return objectMapper.readValue(body, Object.class);
            }
        } catch (JsonProcessingException e) {
            log.debug("Could not parse response body as JSON: {}", e.getMessage());
        }

        // Return as string if not valid JSON
        return body;
    }
}
