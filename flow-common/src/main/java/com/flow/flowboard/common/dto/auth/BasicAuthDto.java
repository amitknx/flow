package com.flow.flowboard.common.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flow.flowboard.common.domain.ApiAuthType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Basic Authentication (username + password)
 * Credentials can be direct values or references to secrets
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class BasicAuthDto extends ApiAuthenticationDto {

    /**
     * Username - can be direct value or secret reference
     * Example direct: "admin"
     * Example reference: "{{secret.my_api.username}}"
     */
    @NotBlank(message = "Username is required for Basic authentication")
    private String username;

    /**
     * Password - can be direct value or secret reference
     * Example direct: "password123"
     * Example reference: "{{secret.my_api.password}}"
     */
    @NotBlank(message = "Password is required for Basic authentication")
    private String password;

    @Override
    public ApiAuthType getType() {
        return ApiAuthType.BASIC;
    }
}
