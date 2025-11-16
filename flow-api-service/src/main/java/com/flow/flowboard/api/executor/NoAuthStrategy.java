package com.flow.flowboard.api.executor;

import com.flow.flowboard.common.dto.auth.ApiAuthenticationDto;
import com.flow.flowboard.common.dto.auth.NoAuthDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Authentication strategy for no authentication
 */
@Component
@Slf4j
public class NoAuthStrategy implements AuthenticationStrategy {

    @Override
    public WebClient.RequestHeadersSpec<?> apply(
            WebClient.RequestHeadersSpec<?> requestBuilder,
            ApiAuthenticationDto authentication) {
        log.debug("No authentication applied");
        return requestBuilder;
    }

    @Override
    public boolean supports(ApiAuthenticationDto authentication) {
        return authentication == null || authentication instanceof NoAuthDto;
    }
}
