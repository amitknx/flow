package com.flow.flowboard.api.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flow.flowboard.api.entity.Api;
import com.flow.flowboard.api.entity.ApiEndpoint;
import com.flow.flowboard.common.dto.auth.ApiAuthenticationDto;
import com.flow.flowboard.common.dto.credential.RetryConfigDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Parses and caches configuration objects from JSON strings
 * Uses Spring Cache to avoid repeated parsing
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ConfigurationParser {

    private final ObjectMapper objectMapper;

    /**
     * Parse authentication JSON to DTO
     */
    public ApiAuthenticationDto parseAuthentication(String authJson) {
        if (authJson == null || authJson.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.readValue(authJson, ApiAuthenticationDto.class);
        } catch (Exception e) {
            log.error("Failed to parse authentication JSON: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Parse retry config from endpoint or API
     */
    public RetryConfigDto parseRetryConfig(ApiEndpoint endpoint, Api api) {
        try {
            // Check endpoint retry config first
            if (endpoint.getRetryConfig() != null && !endpoint.getRetryConfig().isEmpty()) {
                return objectMapper.readValue(endpoint.getRetryConfig(), RetryConfigDto.class);
            }
            // Fall back to API retry config
            if (api.getRetryConfig() != null && !api.getRetryConfig().isEmpty()) {
                return objectMapper.readValue(api.getRetryConfig(), RetryConfigDto.class);
            }
        } catch (Exception e) {
            log.error("Failed to parse retry config: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Parse query params from JSON
     */
    public Map<String, String> parseQueryParams(String json) {
        return parseStringMap(json);
    }

    /**
     * Parse headers from JSON (used by RequestBuilder)
     */
    public Map<String, String> parseHeaders(String json) {
        return parseStringMap(json);
    }

    /**
     * Parse JSON string to Map with proper type handling
     */
    private Map<String, String> parseStringMap(String json) {
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }

        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            log.error("Failed to parse JSON to map: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Determine timeout from request, endpoint, or API (in order of precedence)
     */
    public int determineTimeout(Integer requestTimeout, Integer endpointTimeout, Integer apiTimeout) {
        if (requestTimeout != null) {
            return requestTimeout;
        }
        if (endpointTimeout != null) {
            return endpointTimeout;
        }
        return apiTimeout != null ? apiTimeout : 30000; // Default 30 seconds
    }
}
