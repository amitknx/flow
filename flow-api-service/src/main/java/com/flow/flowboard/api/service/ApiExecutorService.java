package com.flow.flowboard.api.service;

import com.flow.flowboard.api.entity.Api;
import com.flow.flowboard.api.entity.ApiEndpoint;
import com.flow.flowboard.api.exception.ApiEndpointNotFoundException;
import com.flow.flowboard.api.repository.ApiEndpointRepository;
import com.flow.flowboard.api.util.*;
import com.flow.flowboard.common.dto.auth.ApiAuthenticationDto;
import com.flow.flowboard.common.dto.credential.RetryConfigDto;
import com.flow.flowboard.common.dto.execution.ExecuteEndpointRequest;
import com.flow.flowboard.common.dto.execution.ExecuteEndpointResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Orchestrates API endpoint execution
 * Delegates to specialized components for modular, maintainable code
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApiExecutorService {

        private final ApiEndpointRepository endpointRepository;
        private final WebClient webClient;
        private final UrlBuilder urlBuilder;
        private final RequestBuilder requestBuilder;
        private final ResponseHandler responseHandler;
        private final RetryHandler retryHandler;
        private final ConfigurationParser configParser;

        /**
         * Execute an API endpoint reactively
         *
         * @param endpointId Endpoint ID
         * @param request    Execution request with parameters
         * @return Execution response (Mono for reactive processing)
         */
        public Mono<ExecuteEndpointResponse> executeEndpoint(UUID endpointId, ExecuteEndpointRequest request) {
                log.info("Executing endpoint: {}", endpointId);
                Instant startTime = Instant.now();

                // Fetch endpoint (wrap blocking call in reactive context)
                return Mono.fromCallable(() -> fetchEndpoint(endpointId))
                                .subscribeOn(Schedulers.boundedElastic())
                                .flatMap(endpoint -> executeRequest(endpoint, request, startTime));
        }

        /**
         * Fetch endpoint and validate it's active
         * This is a blocking operation wrapped for reactive context
         * Uses JOIN FETCH to eagerly load the Api entity to avoid
         * LazyInitializationException
         */
        private ApiEndpoint fetchEndpoint(UUID endpointId) {
                ApiEndpoint endpoint = endpointRepository.findByIdWithApi(endpointId)
                                .orElseThrow(() -> new ApiEndpointNotFoundException(endpointId));

                Api api = endpoint.getApi();

                if (!endpoint.getActive() || !api.getActive()) {
                        throw new IllegalStateException("Endpoint or API is not active");
                }

                return endpoint;
        }

        /**
         * Execute the API request with all configuration
         */
        private Mono<ExecuteEndpointResponse> executeRequest(
                        ApiEndpoint endpoint,
                        ExecuteEndpointRequest request,
                        Instant startTime) {

                Api api = endpoint.getApi();

                // Parse authentication with caching
                ApiAuthenticationDto authentication = request.getOverrideAuthentication() != null
                                ? request.getOverrideAuthentication()
                                : configParser.parseAuthentication(api.getAuthentication());

                // Parse query params from endpoint
                Map<String, String> endpointQueryParams = configParser.parseQueryParams(endpoint.getQueryParams());

                // Build URI with proper encoding
                URI uri = urlBuilder.buildUri(api, endpoint.getEndpointPath(), endpointQueryParams, request,
                                authentication);
                String requestUrl = uri.toString();

                // Build request with headers, body, and authentication
                WebClient.RequestHeadersSpec<?> requestSpec = requestBuilder.buildRequest(
                                webClient,
                                uri,
                                endpoint,
                                request,
                                authentication);

                // Capture request details for response
                Map<String, String> requestHeaders = requestBuilder.buildHeaders(endpoint, request, authentication);
                Object requestBody = request.getRequestBody();

                // Determine timeout
                int timeoutMs = configParser.determineTimeout(
                                request.getTimeoutMs(),
                                endpoint.getTimeoutMs(),
                                api.getTimeoutMs());

                Mono<ExecuteEndpointResponse> responseMono = requestSpec
                                .exchangeToMono(response -> response.bodyToMono(String.class)
                                                .defaultIfEmpty("")
                                                .map(body -> responseHandler.handleResponse(
                                                                getHeaders(response.headers().asHttpHeaders()),
                                                                Objects.requireNonNull(HttpStatus.resolve(
                                                                                response.statusCode().value())),
                                                                startTime, requestUrl, endpoint, body,
                                                                requestHeaders, requestBody)))
                                .timeout(Duration.ofMillis(timeoutMs));

                // Apply retry logic if configured
                RetryConfigDto retryConfig = configParser.parseRetryConfig(endpoint, api);
                Retry retrySpec = retryHandler.buildRetrySpec(retryConfig);
                if (retrySpec != null) {
                        responseMono = responseMono.retryWhen(retrySpec);
                }

                // Handle errors
                return responseMono
                                .onErrorResume(error -> responseHandler.handleError(
                                                error, startTime, requestUrl, endpoint,
                                                requestHeaders, requestBody));
        }

        private Map<String, String> getHeaders(HttpHeaders httpHeaders) {
                Map<String, String> responseHeaders = new java.util.HashMap<>();
                httpHeaders.forEach((key, values) -> responseHeaders.put(key, String.join(", ", values)));
                return responseHeaders;
        }
}
