package com.flow.flowboard.common.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.flow.flowboard.common.domain.ApiAuthType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * No authentication required
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class NoAuthDto extends ApiAuthenticationDto {
    @Override
    public ApiAuthType getType() {
        return ApiAuthType.NONE;
    }
}
