package com.flow.flowboard.api.util;

import com.flow.flowboard.api.entity.Api;
import com.flow.flowboard.common.dto.auth.ApiAuthenticationDto;
import com.flow.flowboard.common.dto.auth.ApiKeyAuthDto;
import com.flow.flowboard.common.dto.auth.CustomAuthDto;
import com.flow.flowboard.common.dto.execution.ExecuteEndpointRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

/**
 * Handles URL building with proper encoding and parameter substitution
 */
@Component
@Slf4j
public class UrlBuilder {

    /**
     * Build complete URI with path params and query params
     *
     * @param api API entity
     * @param endpointPath Endpoint path (may contain {{pathParam}} placeholders)
     * @param endpointQueryParams Query params from endpoint definition
     * @param request Execution request with runtime parameters
     * @param authentication Authentication config (may add query params)
     * @return Fully constructed URI with encoding
     */
    public URI buildUri(
            Api api,
            String endpointPath,
            Map<String, String> endpointQueryParams,
            ExecuteEndpointRequest request,
            ApiAuthenticationDto authentication) {

        // Build base URL with path parameter substitution
        String fullPath = buildPathWithSubstitution(api.getBaseUrl(), endpointPath, request);

        // Build URI with query parameters
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(fullPath);

        // Add endpoint query params
        if (endpointQueryParams != null && !endpointQueryParams.isEmpty()) {
            endpointQueryParams.forEach(uriBuilder::queryParam);
        }

        // Add request query params (override endpoint params)
        if (request.getQueryParams() != null && !request.getQueryParams().isEmpty()) {
            request.getQueryParams().forEach(uriBuilder::queryParam);
        }

        // Add authentication query params
        addAuthenticationQueryParams(uriBuilder, authentication);

        return uriBuilder.build().encode().toUri();
    }

    /**
     * Build full path with path parameter substitution
     * Properly encodes path parameters
     */
    private String buildPathWithSubstitution(String baseUrl, String path, ExecuteEndpointRequest request) {
        String cleanBaseUrl = baseUrl.replaceAll("/+$", ""); // Remove trailing slashes

        // Substitute path parameters with URL encoding
        if (request.getPathParams() != null && !request.getPathParams().isEmpty()) {
            for (Map.Entry<String, String> entry : request.getPathParams().entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                // URL encode the path parameter value
                String encodedValue = UriComponentsBuilder.fromPath("/{value}")
                        .buildAndExpand(entry.getValue())
                        .toUriString()
                        .substring(1); // Remove leading /
                path = path.replace(placeholder, encodedValue);
            }
        }

        // Ensure path starts with /
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        return cleanBaseUrl + path;
    }

    /**
     * Add authentication-specific query parameters
     * Does not log sensitive data
     */
    private void addAuthenticationQueryParams(UriComponentsBuilder uriBuilder, ApiAuthenticationDto authentication) {
        if (authentication == null) {
            return;
        }

        // Handle API Key authentication in query params
        if (authentication instanceof ApiKeyAuthDto apiKey) {
            if ("query".equalsIgnoreCase(apiKey.getLocation())) {
                log.debug("Adding API key authentication as query parameter (key name: {})", apiKey.getKeyName());
                uriBuilder.queryParam(apiKey.getKeyName(), apiKey.getKeyValue());
            }
        }
        // Handle Custom authentication query params
        else if (authentication instanceof CustomAuthDto custom) {
            if (custom.getQueryParams() != null && !custom.getQueryParams().isEmpty()) {
                log.debug("Adding {} custom authentication query parameters", custom.getQueryParams().size());
                custom.getQueryParams().forEach(uriBuilder::queryParam);
            }
        }
    }
}
