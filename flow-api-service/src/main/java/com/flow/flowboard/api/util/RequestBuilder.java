package com.flow.flowboard.api.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flow.flowboard.api.entity.ApiEndpoint;
import com.flow.flowboard.api.executor.AuthenticationStrategy;
import com.flow.flowboard.common.dto.auth.ApiAuthenticationDto;
import com.flow.flowboard.common.dto.execution.ExecuteEndpointRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds WebClient requests with headers, body, and authentication
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RequestBuilder {

    private final List<AuthenticationStrategy> authenticationStrategies;
    private final ObjectMapper objectMapper;

    /**
     * Build WebClient request with all parameters, headers, and authentication
     */
    public WebClient.RequestHeadersSpec<?> buildRequest(
            WebClient webClient,
            URI uri,
            ApiEndpoint endpoint,
            ExecuteEndpointRequest request,
            ApiAuthenticationDto authentication) {

        // Parse stored headers from endpoint
        Map<String, String> endpointHeaders = parseHeaders(endpoint.getHeaders());

        // Build request based on HTTP method
        WebClient.RequestHeadersSpec<?> requestSpec = createRequestByMethod(webClient, uri, endpoint);

        // Add headers from endpoint definition
        requestSpec = addHeaders(requestSpec, endpointHeaders);

        // Add headers from request (override endpoint headers)
        requestSpec = addHeaders(requestSpec, request.getHeaders());

        // Apply authentication
        requestSpec = applyAuthentication(requestSpec, authentication);

        // Add request body for POST/PUT/PATCH
        requestSpec = addRequestBody(requestSpec, endpoint, request);

        return requestSpec;
    }

    /**
     * Create request based on HTTP method
     */
    private WebClient.RequestHeadersSpec<?> createRequestByMethod(
            WebClient webClient,
            URI uri,
            ApiEndpoint endpoint) {

        return switch (endpoint.getHttpMethod()) {
            case GET -> webClient.get().uri(uri);
            case POST -> webClient.post().uri(uri);
            case PUT -> webClient.put().uri(uri);
            case DELETE -> webClient.delete().uri(uri);
            case PATCH -> webClient.patch().uri(uri);
            case HEAD -> webClient.head().uri(uri);
            case OPTIONS -> webClient.options().uri(uri);
        };
    }

    /**
     * Add headers to request
     */
    private WebClient.RequestHeadersSpec<?> addHeaders(
            WebClient.RequestHeadersSpec<?> requestSpec,
            Map<String, String> headers) {

        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestSpec = requestSpec.header(entry.getKey(), entry.getValue());
            }
        }
        return requestSpec;
    }

    /**
     * Apply authentication using the appropriate strategy
     */
    private WebClient.RequestHeadersSpec<?> applyAuthentication(
            WebClient.RequestHeadersSpec<?> requestSpec,
            ApiAuthenticationDto authentication) {

        if (authentication == null) {
            return requestSpec;
        }

        for (AuthenticationStrategy strategy : authenticationStrategies) {
            if (strategy.supports(authentication)) {
                return strategy.apply(requestSpec, authentication);
            }
        }

        log.warn("No authentication strategy found for type: {}", authentication.getClass().getSimpleName());
        return requestSpec;
    }

    /**
     * Add request body for POST/PUT/PATCH methods
     */
    private WebClient.RequestHeadersSpec<?> addRequestBody(
            WebClient.RequestHeadersSpec<?> requestSpec,
            ApiEndpoint endpoint,
            ExecuteEndpointRequest request) {

        if (request.getRequestBody() != null &&
                (endpoint.getHttpMethod().name().equals("POST") ||
                        endpoint.getHttpMethod().name().equals("PUT") ||
                        endpoint.getHttpMethod().name().equals("PATCH"))) {

            String contentType = endpoint.getContentType() != null
                    ? endpoint.getContentType()
                    : MediaType.APPLICATION_JSON_VALUE;

            requestSpec = ((WebClient.RequestBodySpec) requestSpec)
                    .contentType(MediaType.valueOf(contentType))
                    .bodyValue(request.getRequestBody());
        }

        return requestSpec;
    }

    /**
     * Build map of all headers that will be sent with the request
     */
    public Map<String, String> buildHeaders(
            ApiEndpoint endpoint,
            ExecuteEndpointRequest request,
            ApiAuthenticationDto authentication) {

        Map<String, String> headers = new HashMap<>();

        // 1. Endpoint headers
        Map<String, String> endpointHeaders = parseHeaders(endpoint.getHeaders());
        if (endpointHeaders != null) {
            headers.putAll(endpointHeaders);
        }

        // 2. Request override headers
        if (request.getHeaders() != null) {
            headers.putAll(request.getHeaders());
        }

        // 3. Authentication headers
        if (authentication != null) {
            for (AuthenticationStrategy strategy : authenticationStrategies) {
                if (strategy.supports(authentication)) {
                    strategy.contributeHeaders(headers, authentication);
                    break;
                }
            }
        }

        return headers;
    }

    /**
     * Parse JSON string to Map with proper type handling
     */
    public Map<String, String> parseHeaders(String json) {
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }

        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {
            });
        } catch (Exception e) {
            log.error("Failed to parse headers JSON", e);
            return new HashMap<>();
        }
    }
}
