package com.flow.flowboard.common.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flow.flowboard.common.domain.ApiAuthType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * API Key Authentication
 * API key can be sent in a header or query parameter
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ApiKeyAuthDto extends ApiAuthenticationDto {

    /**
     * Where to send the API key: "header" or "query"
     */
    @NotBlank(message = "Location is required (header or query)")
    private String location; // "header" or "query"

    /**
     * Name of the header or query parameter
     * Example: "X-API-Key", "api_key", "apikey"
     */
    @NotBlank(message = "Key name is required")
    private String keyName;

    /**
     * API key value - can be direct value or secret reference
     * Example direct: "sk-1234567890abcdef"
     * Example reference: "{{secret.stripe_prod.api_key}}"
     */
    @NotBlank(message = "API key value is required")
    private String keyValue;

    @Override
    public ApiAuthType getType() {
        return ApiAuthType.API_KEY;
    }
}
