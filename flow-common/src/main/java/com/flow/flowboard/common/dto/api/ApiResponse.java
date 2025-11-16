package com.flow.flowboard.common.dto.api;

import com.flow.flowboard.common.dto.auth.ApiAuthenticationDto;
import com.flow.flowboard.common.dto.credential.RetryConfigDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {

    private UUID id;
    private Long versionNum;  // Optimistic locking version
    private String name;
    private String version;
    private String baseUrl;
    private String description;
    private ApiAuthenticationDto authentication;  // Authentication configuration
    private List<String> tags;
    private Integer timeoutMs;  // Default timeout for endpoints
    private RetryConfigDto retryConfig;  // Default retry config for endpoints
    private List<ApiEndpointDto> endpoints;  // Full list of endpoints with IDs
    private Integer endpointCount;  // Number of endpoints in this API
    private Boolean active;

    // Audit fields
    private Instant createdAt;
    private String createdBy;
    private Instant modifiedAt;
    private String modifiedBy;
}
