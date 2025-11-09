package com.flow.flowboard.api.entity;

import com.flow.flowboard.api.util.EncryptedJsonConverter;
import com.flow.flowboard.common.domain.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing secrets - simple key-value store for sensitive data
 * Secrets can be referenced by APIs for authentication
 *
 * Example: A secret named "github_prod" might contain:
 * {
 *   "api_key": "ghp_xxxxxxxxxxxxx",
 *   "client_id": "Iv1.xxxxxxxxxxxxx",
 *   "client_secret": "xxxxxxxxxxxxx"
 * }
 *
 * Security:
 * - secretData field is encrypted at rest using AES-256-GCM
 * - Sensitive values never exposed in toString() or logs
 * - Soft delete support via active flag from AuditableEntity
 */
@Entity
@Table(name = "secrets",
        indexes = {
            @Index(name = "idx_secrets_name", columnList = "name"),
            @Index(name = "idx_secrets_active", columnList = "active"),
            @Index(name = "idx_secrets_created_at", columnList = "created_at")
        },
        uniqueConstraints = {
            @UniqueConstraint(name = "secrets_name_unique", columnNames = "name")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Secret extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Unique name identifier for the secret (used in API references)
     * Example: "github_prod", "stripe_test", "internal_api"
     */
    @Column(name = "name", nullable = false, length = 200, unique = true)
    private String name;

    /**
     * Human-readable description
     */
    @Column(name = "description", length = 2000)
    private String description;

    /**
     * Tags for categorization, stored as JSONB array
     * Example: ["production", "github", "oauth"]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "jsonb")
    private List<String> tags;

    /**
     * Encrypted secret data stored as JSONB key-value pairs
     * Example: {"api_key": "sk-123", "client_id": "abc", "client_secret": "xyz"}
     *
     * SECURITY: Only values are encrypted, keys remain unencrypted for queryability
     * This field is automatically encrypted/decrypted by EncryptedJsonConverter
     * Never log or expose the decrypted values
     */
    @Convert(converter = EncryptedJsonConverter.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "secret_data", nullable = false, columnDefinition = "jsonb")
    private Map<String, String> secretData;

    /**
     * Override toString to prevent accidental logging of sensitive data
     */
    @Override
    public String toString() {
        return "Secret{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", active=" + getActive() +
                '}';
    }
}
