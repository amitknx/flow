package com.flow.flowboard.common.dto.api;

import com.flow.flowboard.common.domain.HttpMethod;
import com.flow.flowboard.common.dto.credential.RetryConfigDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for API endpoint
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiEndpointResponse {

    private UUID id;
    private UUID apiId;
    private String apiName;  // Denormalized for convenience
    private String apiVersion;  // Denormalized
    private String name;
    private String description;
    private HttpMethod httpMethod;
    private String endpointPath;
    private Map<String, Object> pathParams;
    private Map<String, Object> queryParams;
    private Map<String, String> headers;
    private Object requestBodyTemplate;
    private String contentType;
    private Map<String, String> responseMapping;
    private Integer timeoutMs;
    private RetryConfigDto retryConfig;
    private Boolean active;

    // Audit fields
    private Instant createdAt;
    private String createdBy;
    private Instant modifiedAt;
    private String modifiedBy;
}
