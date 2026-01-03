package com.flow.flowboard.api.executor;

import com.flow.flowboard.common.dto.auth.ApiAuthenticationDto;
import com.flow.flowboard.common.dto.auth.BearerAuthDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Authentication strategy for Bearer Token Authentication
 */
@Component
@Slf4j
public class BearerAuthStrategy implements AuthenticationStrategy {

    @Override
    public WebClient.RequestHeadersSpec<?> apply(
            WebClient.RequestHeadersSpec<?> requestBuilder,
            ApiAuthenticationDto authentication) {

        BearerAuthDto bearerAuth = (BearerAuthDto) authentication;
        String token = bearerAuth.getToken();

        log.debug("Applying Bearer token authentication");

        return requestBuilder.header("Authorization", "Bearer " + token);
    }

    @Override
    public boolean supports(ApiAuthenticationDto authentication) {
        return authentication instanceof BearerAuthDto;
    }

    @Override
    public void contributeHeaders(java.util.Map<String, String> headers, ApiAuthenticationDto authentication) {
        BearerAuthDto bearerAuth = (BearerAuthDto) authentication;
        String token = bearerAuth.getToken();
        headers.put("Authorization", "Bearer " + token);
    }
}
