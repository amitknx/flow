package com.flow.flowboard.common.dto.auth;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.flow.flowboard.common.domain.ApiAuthType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Base class for API authentication configuration
 * Uses Jackson polymorphic serialization to support different auth types
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = NoAuthDto.class, name = "NONE"),
    @JsonSubTypes.Type(value = BasicAuthDto.class, name = "BASIC"),
    @JsonSubTypes.Type(value = BearerAuthDto.class, name = "BEARER"),
    @JsonSubTypes.Type(value = ApiKeyAuthDto.class, name = "API_KEY"),
    @JsonSubTypes.Type(value = OAuth2AuthDto.class, name = "OAUTH2"),
    @JsonSubTypes.Type(value = CustomAuthDto.class, name = "CUSTOM")
})
public abstract class ApiAuthenticationDto {
    /**
     * Authentication type
     */
    private ApiAuthType type;
}
