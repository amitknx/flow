package com.flow.flowboard.api.executor;

import com.flow.flowboard.common.dto.auth.ApiAuthenticationDto;
import com.flow.flowboard.common.dto.auth.CustomAuthDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * Authentication strategy for Custom Authentication
 * Allows adding custom headers and query parameters
 */
@Component
@Slf4j
public class CustomAuthStrategy implements AuthenticationStrategy {

    @Override
    public WebClient.RequestHeadersSpec<?> apply(
            WebClient.RequestHeadersSpec<?> requestBuilder,
            ApiAuthenticationDto authentication) {

        CustomAuthDto customAuth = (CustomAuthDto) authentication;

        // Apply custom headers
        if (customAuth.getHeaders() != null && !customAuth.getHeaders().isEmpty()) {
            log.debug("Applying {} custom headers", customAuth.getHeaders().size());
            for (Map.Entry<String, String> header : customAuth.getHeaders().entrySet()) {
                requestBuilder = requestBuilder.header(header.getKey(), header.getValue());
            }
        }

        // Query parameters will be handled in URI building phase
        if (customAuth.getQueryParams() != null && !customAuth.getQueryParams().isEmpty()) {
            log.debug("Custom auth includes {} query parameters", customAuth.getQueryParams().size());
        }

        return requestBuilder;
    }

    @Override
    public boolean supports(ApiAuthenticationDto authentication) {
        return authentication instanceof CustomAuthDto;
    }
}
