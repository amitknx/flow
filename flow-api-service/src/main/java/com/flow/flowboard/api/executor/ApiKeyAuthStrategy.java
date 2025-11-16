package com.flow.flowboard.api.executor;

import com.flow.flowboard.common.dto.auth.ApiAuthenticationDto;
import com.flow.flowboard.common.dto.auth.ApiKeyAuthDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

/**
 * Authentication strategy for API Key Authentication
 * Supports both header-based and query-parameter-based API keys
 */
@Component
@Slf4j
public class ApiKeyAuthStrategy implements AuthenticationStrategy {

    @Override
    public WebClient.RequestHeadersSpec<?> apply(
            WebClient.RequestHeadersSpec<?> requestBuilder,
            ApiAuthenticationDto authentication) {

        ApiKeyAuthDto apiKeyAuth = (ApiKeyAuthDto) authentication;
        String location = apiKeyAuth.getLocation();
        String keyName = apiKeyAuth.getKeyName();
        String keyValue = apiKeyAuth.getKeyValue();

        if ("header".equalsIgnoreCase(location)) {
            log.debug("Applying API Key authentication in header: {}", keyName);
            return requestBuilder.header(keyName, keyValue);
        } else if ("query".equalsIgnoreCase(location)) {
            log.debug("Applying API Key authentication in query parameter: {}", keyName);
            // Query params are handled in the URI building phase
            // This will be applied in the ApiExecutorService
            return requestBuilder;
        } else {
            log.warn("Unknown API Key location: {}. Defaulting to header.", location);
            return requestBuilder.header(keyName, keyValue);
        }
    }

    @Override
    public boolean supports(ApiAuthenticationDto authentication) {
        return authentication instanceof ApiKeyAuthDto;
    }
}
