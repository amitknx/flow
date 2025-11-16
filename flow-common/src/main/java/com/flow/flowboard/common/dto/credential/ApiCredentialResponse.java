package com.flow.flowboard.common.dto.credential;

import com.flow.flowboard.common.domain.AuthType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for API credential
 * Sensitive credential data is masked for security
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiCredentialResponse {

    private UUID id;
    private String name;
    private String description;
    private String baseUrl;
    private AuthType authType;

    /**
     * Masked credential data (e.g., "apiKey": "sk-***xyz")
     * Actual values are not exposed in responses for security
     */
    private Map<String, Object> credentialDataMasked;

    private Map<String, String> defaultHeaders;
    private Boolean active;

    // Audit fields
    private Instant createdAt;
    private String createdBy;
    private Instant modifiedAt;
    private String modifiedBy;
}
