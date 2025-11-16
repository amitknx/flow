package com.flow.flowboard.common.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flow.flowboard.common.domain.ApiAuthType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * OAuth 2.0 Authentication (Client Credentials Flow)
 * Automatically obtains and refreshes access tokens
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class OAuth2AuthDto extends ApiAuthenticationDto {

    /**
     * Client ID - can be direct value or secret reference
     * Example reference: "{{secret.github_prod.client_id}}"
     */
    @NotBlank(message = "Client ID is required for OAuth2")
    private String clientId;

    /**
     * Client Secret - can be direct value or secret reference
     * Example reference: "{{secret.github_prod.client_secret}}"
     */
    @NotBlank(message = "Client Secret is required for OAuth2")
    private String clientSecret;

    /**
     * Token endpoint URL
     * Example: "https://github.com/login/oauth/access_token"
     */
    @NotBlank(message = "Token URL is required for OAuth2")
    private String tokenUrl;

    /**
     * OAuth scopes (space-separated)
     * Example: "repo user:email"
     */
    private String scope;

    @Override
    public ApiAuthType getType() {
        return ApiAuthType.OAUTH2;
    }
}
