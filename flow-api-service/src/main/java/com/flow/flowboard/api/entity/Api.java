package com.flow.flowboard.api.entity;

import com.flow.flowboard.common.domain.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a top-level API configuration
 * An API can have multiple endpoints (one-to-many relationship)
 *
 * Examples: "GitHub API v3", "Stripe API v1", "Internal User Service v2"
 */
@Entity
@Table(name = "apis",
        indexes = {
            @Index(name = "idx_apis_name", columnList = "name"),
            @Index(name = "idx_apis_active", columnList = "active"),
            @Index(name = "idx_apis_created_at", columnList = "created_at")
        },
        uniqueConstraints = {
            @UniqueConstraint(name = "apis_name_version_unique", columnNames = {"name", "version"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Api extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Version field for optimistic locking to prevent dirty writes
     */
    @Version
    @Column(name = "version_num")
    private Long versionNum;

    /**
     * API name (e.g., "GitHub API", "Stripe API")
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * API version (e.g., "v3", "v1", "2023-10")
     */
    @Column(name = "version", length = 50)
    private String version;

    /**
     * Base URL for all endpoints in this API
     * Example: https://api.github.com, https://api.stripe.com
     */
    @Column(name = "base_url", nullable = false, length = 500)
    private String baseUrl;

    /**
     * Description of the API
     */
    @Column(name = "description", length = 2000)
    private String description;

    /**
     * Authentication configuration stored as JSONB
     * Contains auth type and references to secret values
     *
     * Example for API Key auth:
     * {
     *   "type": "API_KEY",
     *   "config": {
     *     "headerName": "X-API-Key",
     *     "valueRef": "secret.github_prod.api_key"
     *   }
     * }
     *
     * Example for OAuth2:
     * {
     *   "type": "OAUTH2",
     *   "config": {
     *     "clientIdRef": "secret.github_prod.client_id",
     *     "clientSecretRef": "secret.github_prod.client_secret",
     *     "tokenUrl": "https://github.com/login/oauth/access_token"
     *   }
     * }
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "authentication", columnDefinition = "jsonb")
    private String authentication;

    /**
     * Tags for categorization, stored as JSONB array
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags", columnDefinition = "jsonb")
    private List<String> tags;

    /**
     * Default timeout for all endpoints in this API (milliseconds)
     * Can be overridden per endpoint
     */
    @Column(name = "timeout_ms")
    @Builder.Default
    private Integer timeoutMs = 30000;

    /**
     * Default retry configuration for all endpoints
     * Can be overridden per endpoint
     * Stored as JSONB
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "retry_config", columnDefinition = "jsonb")
    private String retryConfig;

    /**
     * Endpoints belonging to this API
     * Cascade delete: when API is deleted, all endpoints are deleted
     */
    @OneToMany(mappedBy = "api", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ApiEndpoint> endpoints = new ArrayList<>();

    /**
     * Helper method to add an endpoint
     */
    public void addEndpoint(ApiEndpoint endpoint) {
        endpoints.add(endpoint);
        endpoint.setApi(this);
    }

    /**
     * Helper method to remove an endpoint
     */
    public void removeEndpoint(ApiEndpoint endpoint) {
        endpoints.remove(endpoint);
        endpoint.setApi(null);
    }

    @Override
    public String toString() {
        return "Api{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", baseUrl='" + baseUrl + '\'' +
                ", active=" + getActive() +
                '}';
    }
}
