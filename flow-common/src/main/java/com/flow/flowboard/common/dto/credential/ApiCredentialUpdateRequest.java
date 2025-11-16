package com.flow.flowboard.common.dto.credential;

import com.flow.flowboard.common.domain.AuthType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for updating an existing API credential
 * All fields are optional - only provided fields will be updated
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiCredentialUpdateRequest {

    /**
     * Updated description
     */
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    /**
     * Updated base URL
     */
    @Size(max = 500, message = "Base URL must not exceed 500 characters")
    @Pattern(regexp = "^https?://.*", message = "Base URL must be a valid HTTP or HTTPS URL")
    private String baseUrl;

    /**
     * Updated authentication type
     */
    private AuthType authType;

    /**
     * Updated authentication credentials
     * Will be encrypted before storage
     */
    private Map<String, Object> credentialData;

    /**
     * Updated default headers
     */
    private Map<String, String> defaultHeaders;

    /**
     * Soft delete flag
     */
    private Boolean active;
}
