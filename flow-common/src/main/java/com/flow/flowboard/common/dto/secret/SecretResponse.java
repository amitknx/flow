package com.flow.flowboard.common.dto.secret;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Response DTO for Secret
 * Sensitive secret values are masked for security
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecretResponse {

    private UUID id;
    private String name;
    private String description;
    private List<String> tags;

    /**
     * Masked secret data (e.g., "apiKey": "sk-***xyz")
     * Actual values are not exposed in responses for security
     */
    private Map<String, Object> secretDataMasked;

    private Boolean active;

    // Audit fields
    private Instant createdAt;
    private String createdBy;
    private Instant modifiedAt;
    private String modifiedBy;
}
