package com.flow.flowboard.api.executor;

import com.flow.flowboard.common.dto.auth.ApiAuthenticationDto;
import com.flow.flowboard.common.dto.auth.BasicAuthDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Authentication strategy for HTTP Basic Authentication
 */
@Component
@Slf4j
public class BasicAuthStrategy implements AuthenticationStrategy {

    @Override
    public WebClient.RequestHeadersSpec<?> apply(
            WebClient.RequestHeadersSpec<?> requestBuilder,
            ApiAuthenticationDto authentication) {

        BasicAuthDto basicAuth = (BasicAuthDto) authentication;
        String username = basicAuth.getUsername();
        String password = basicAuth.getPassword();

        // Create Basic auth header value
        String credentials = username + ":" + password;
        String encodedCredentials = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        String authHeader = "Basic " + encodedCredentials;

        log.debug("Applying Basic authentication for user: {}", username);

        return requestBuilder.header("Authorization", authHeader);
    }

    @Override
    public boolean supports(ApiAuthenticationDto authentication) {
        return authentication instanceof BasicAuthDto;
    }
}
