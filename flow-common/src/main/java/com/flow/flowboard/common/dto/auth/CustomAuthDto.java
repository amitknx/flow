package com.flow.flowboard.common.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flow.flowboard.common.domain.ApiAuthType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Custom Authentication
 * Allows users to define custom headers and query parameters
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CustomAuthDto extends ApiAuthenticationDto {

    /**
     * Custom headers to add to requests
     * Keys are header names, values can be direct or secret references
     * Example: {"X-Custom-Auth": "{{secret.my_api.auth_header}}", "X-App-Id": "12345"}
     */
    private Map<String, String> headers;

    /**
     * Custom query parameters to add to requests
     * Keys are parameter names, values can be direct or secret references
     * Example: {"auth_token": "{{secret.my_api.token}}", "app_id": "12345"}
     */
    private Map<String, String> queryParams;

    @Override
    public ApiAuthType getType() {
        return ApiAuthType.CUSTOM;
    }
}
