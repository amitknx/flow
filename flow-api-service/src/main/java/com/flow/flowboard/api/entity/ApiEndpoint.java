package com.flow.flowboard.api.entity;

import com.flow.flowboard.common.domain.HttpMethod;
import com.flow.flowboard.common.domain.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/**
 * Entity representing an API endpoint within an API
 * Each endpoint has its own method, path, parameters, and configuration
 *
 * Examples:
 * - API: "GitHub API v3", Endpoint: "Get User" (GET /users/{{username}})
 * - API: "Stripe API v1", Endpoint: "Create Payment" (POST /v1/payments)
 */
@Entity
@Table(name = "api_endpoints",
        indexes = {
            @Index(name = "idx_api_endpoints_api_id", columnList = "api_id"),
            @Index(name = "idx_api_endpoints_name", columnList = "name"),
            @Index(name = "idx_api_endpoints_active", columnList = "active"),
            @Index(name = "idx_api_endpoints_http_method", columnList = "http_method"),
            @Index(name = "idx_api_endpoints_created_at", columnList = "created_at")
        },
        uniqueConstraints = {
            @UniqueConstraint(name = "api_endpoints_name_api_unique", columnNames = {"api_id", "name"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiEndpoint extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Parent API this endpoint belongs to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_id", nullable = false, foreignKey = @ForeignKey(name = "api_endpoints_api_fk"))
    private Api api;

    /**
     * Endpoint name (e.g., "Get User", "Create Payment", "List Repositories")
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * Description of what this endpoint does
     */
    @Column(name = "description", length = 2000)
    private String description;

    /**
     * HTTP method (GET, POST, PUT, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "http_method", nullable = false, length = 20)
    private HttpMethod httpMethod;

    /**
     * Endpoint path (appended to API base URL)
     * Supports variable substitution: /users/{{userId}}/repos
     */
    @Column(name = "endpoint_path", nullable = false, length = 1000)
    private String endpointPath;

    /**
     * Path parameter definitions as JSONB
     * Example: {"userId": {"type": "string", "required": true, "description": "User ID"}}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "path_params", columnDefinition = "jsonb")
    private String pathParams;

    /**
     * Query parameter definitions as JSONB
     * Example: {"page": {"type": "integer", "default": 1}, "limit": {"default": 10}}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "query_params", columnDefinition = "jsonb")
    private String queryParams;

    /**
     * Additional HTTP headers specific to this endpoint (as JSONB)
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "headers", columnDefinition = "jsonb")
    private String headers;

    /**
     * Request body template as JSONB (for POST/PUT/PATCH)
     * Supports variable substitution
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_body_template", columnDefinition = "jsonb")
    private String requestBodyTemplate;

    /**
     * Content type for request body
     */
    @Column(name = "content_type", length = 100)
    private String contentType;

    /**
     * JSONPath expressions to extract fields from response
     * Stored as JSONB: {"userId": "$.data.id", "userName": "$.data.name"}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_mapping", columnDefinition = "jsonb")
    private String responseMapping;

    /**
     * Endpoint-specific timeout in milliseconds (overrides API/credential default)
     */
    @Column(name = "timeout_ms")
    private Integer timeoutMs;

    /**
     * Endpoint-specific retry configuration as JSONB
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "retry_config", columnDefinition = "jsonb")
    private String retryConfig;

    @Override
    public String toString() {
        return "ApiEndpoint{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", httpMethod=" + httpMethod +
                ", endpointPath='" + endpointPath + '\'' +
                ", active=" + getActive() +
                '}';
    }
}
