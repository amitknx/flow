package com.flow.flowboard.common.dto.secret;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for updating an existing secret
 * All fields are optional - only provided fields will be updated
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecretUpdateRequest {

    /**
     * Updated description
     */
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    /**
     * Updated tags
     */
    private List<String> tags;

    /**
     * Updated secret data (key-value pairs)
     * Will be encrypted before storage
     */
    private Map<String, String> secretData;

    /**
     * Soft delete flag
     */
    private Boolean active;
}
