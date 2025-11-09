-- Drop the previous api_definitions table if it exists
DROP TABLE IF EXISTS api_definitions;

-- Create APIs table (top-level API configuration)
CREATE TABLE apis (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    version VARCHAR(50),
    base_url VARCHAR(500) NOT NULL,
    description VARCHAR(2000),
    credential_id UUID,
    tags JSONB,
    active BOOLEAN DEFAULT true NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by VARCHAR(100) NOT NULL DEFAULT 'system',

    -- Constraints
    CONSTRAINT apis_name_version_unique UNIQUE (name, version),
    CONSTRAINT apis_credential_fk FOREIGN KEY (credential_id)
        REFERENCES api_credentials(id) ON DELETE SET NULL
);

-- Create API endpoints table (individual endpoints within an API)
CREATE TABLE api_endpoints (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    api_id UUID NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    http_method VARCHAR(20) NOT NULL,
    endpoint_path VARCHAR(1000) NOT NULL,
    path_params JSONB,
    query_params JSONB,
    headers JSONB,
    request_body_template JSONB,
    content_type VARCHAR(100),
    response_mapping JSONB,
    timeout_ms INTEGER,
    retry_config JSONB,
    active BOOLEAN DEFAULT true NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by VARCHAR(100) NOT NULL DEFAULT 'system',

    -- Constraints
    CONSTRAINT api_endpoints_api_fk FOREIGN KEY (api_id)
        REFERENCES apis(id) ON DELETE CASCADE,
    CONSTRAINT api_endpoints_name_api_unique UNIQUE (api_id, name),
    CONSTRAINT api_endpoints_http_method_check CHECK (
        http_method IN ('GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'HEAD', 'OPTIONS')
    ),
    CONSTRAINT api_endpoints_timeout_check CHECK (
        timeout_ms IS NULL OR
        (timeout_ms >= 1000 AND timeout_ms <= 300000)
    )
);

-- Create indexes for APIs table
CREATE INDEX idx_apis_name ON apis(name) WHERE active = true;
CREATE INDEX idx_apis_active ON apis(active);
CREATE INDEX idx_apis_created_at ON apis(created_at DESC);
CREATE INDEX idx_apis_credential_id ON apis(credential_id);
CREATE INDEX idx_apis_tags ON apis USING GIN(tags);

-- Create indexes for API endpoints table
CREATE INDEX idx_api_endpoints_api_id ON api_endpoints(api_id) WHERE active = true;
CREATE INDEX idx_api_endpoints_name ON api_endpoints(name) WHERE active = true;
CREATE INDEX idx_api_endpoints_active ON api_endpoints(active);
CREATE INDEX idx_api_endpoints_http_method ON api_endpoints(http_method);
CREATE INDEX idx_api_endpoints_created_at ON api_endpoints(created_at DESC);

-- Add comments for documentation
COMMENT ON TABLE apis IS 'Top-level API configurations (e.g., GitHub API v3, Stripe API v1)';
COMMENT ON COLUMN apis.name IS 'API name (e.g., GitHub API, Stripe API)';
COMMENT ON COLUMN apis.version IS 'API version (e.g., v3, v1, 2023-10)';
COMMENT ON COLUMN apis.base_url IS 'Base URL for all endpoints in this API';
COMMENT ON COLUMN apis.credential_id IS 'Default credential for authentication (can be overridden per endpoint)';

COMMENT ON TABLE api_endpoints IS 'Individual API endpoints within an API';
COMMENT ON COLUMN api_endpoints.api_id IS 'Parent API this endpoint belongs to';
COMMENT ON COLUMN api_endpoints.endpoint_path IS 'Path appended to API base URL (supports variables like {{userId}})';
COMMENT ON COLUMN api_endpoints.path_params IS 'Path parameter definitions with type and description';
COMMENT ON COLUMN api_endpoints.query_params IS 'Query parameter definitions with default values';
COMMENT ON COLUMN api_endpoints.headers IS 'Additional headers specific to this endpoint';
COMMENT ON COLUMN api_endpoints.request_body_template IS 'Request body template (supports variable substitution)';
COMMENT ON COLUMN api_endpoints.response_mapping IS 'JSONPath expressions to extract fields from response';
COMMENT ON COLUMN api_endpoints.timeout_ms IS 'Endpoint-specific timeout override';
COMMENT ON COLUMN api_endpoints.retry_config IS 'Endpoint-specific retry configuration';
