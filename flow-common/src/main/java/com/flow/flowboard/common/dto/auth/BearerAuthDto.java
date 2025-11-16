package com.flow.flowboard.common.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flow.flowboard.common.domain.ApiAuthType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Bearer Token Authentication
 * Token can be a direct value or reference to a secret
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class BearerAuthDto extends ApiAuthenticationDto {

    /**
     * Bearer token - can be direct value or secret reference
     * Example direct: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
     * Example reference: "{{secret.github_prod.token}}"
     */
    @NotBlank(message = "Token is required for Bearer authentication")
    private String token;

    @Override
    public ApiAuthType getType() {
        return ApiAuthType.BEARER;
    }
}
