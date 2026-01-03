package com.flow.flowboard.api.executor;

import com.flow.flowboard.common.dto.auth.ApiAuthenticationDto;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Strategy interface for applying different authentication types to WebClient
 * requests
 */
public interface AuthenticationStrategy {

    /**
     * Apply authentication to the WebClient request builder
     *
     * @param requestBuilder WebClient request headers spec
     * @param authentication Authentication configuration
     * @return Modified request headers spec with authentication applied
     */
    WebClient.RequestHeadersSpec<?> apply(
            WebClient.RequestHeadersSpec<?> requestBuilder,
            ApiAuthenticationDto authentication);

    /**
     * Check if this strategy supports the given authentication type
     *
     * @param authentication Authentication configuration
     * @return true if this strategy can handle the authentication type
     */
    boolean supports(ApiAuthenticationDto authentication);

    /**
     * Contribute headers to the request map
     *
     * @param headers        Map to add headers to
     * @param authentication Authentication configuration
     */
    default void contributeHeaders(java.util.Map<String, String> headers, ApiAuthenticationDto authentication) {
        // Default implementation does nothing
    }
}
