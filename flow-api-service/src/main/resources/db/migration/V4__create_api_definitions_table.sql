-- Create API definitions table for storing reusable API request configurations
CREATE TABLE api_definitions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    credential_id UUID,
    http_method VARCHAR(20) NOT NULL,
    endpoint_path VARCHAR(1000) NOT NULL,
    query_params JSONB,
    headers JSONB,
    body_template JSONB,
    content_type VARCHAR(100),
    response_mapping JSONB,
    timeout_override_ms INTEGER,
    tags JSONB,
    active BOOLEAN DEFAULT true NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by VARCHAR(100) NOT NULL DEFAULT 'system',

    -- Constraints
    CONSTRAINT api_definitions_name_unique UNIQUE (name),
    CONSTRAINT api_definitions_credential_fk FOREIGN KEY (credential_id)
        REFERENCES api_credentials(id) ON DELETE SET NULL,
    CONSTRAINT api_definitions_http_method_check CHECK (
        http_method IN ('GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'HEAD', 'OPTIONS')
    ),
    CONSTRAINT api_definitions_timeout_check CHECK (
        timeout_override_ms IS NULL OR
        (timeout_override_ms >= 1000 AND timeout_override_ms <= 300000)
    )
);

-- Create indexes for performance
CREATE INDEX idx_api_definitions_name ON api_definitions(name) WHERE active = true;
CREATE INDEX idx_api_definitions_active ON api_definitions(active);
CREATE INDEX idx_api_definitions_created_at ON api_definitions(created_at DESC);
CREATE INDEX idx_api_definitions_credential_id ON api_definitions(credential_id);
CREATE INDEX idx_api_definitions_http_method ON api_definitions(http_method);
CREATE INDEX idx_api_definitions_tags ON api_definitions USING GIN(tags);

-- Add comments for documentation
COMMENT ON TABLE api_definitions IS 'Stores reusable API request definitions that can be referenced in workflows';
COMMENT ON COLUMN api_definitions.credential_id IS 'Optional reference to API credential for authentication';
COMMENT ON COLUMN api_definitions.endpoint_path IS 'API endpoint path (can contain variables like {{userId}})';
COMMENT ON COLUMN api_definitions.query_params IS 'Query parameters as key-value pairs (supports variables)';
COMMENT ON COLUMN api_definitions.headers IS 'Additional headers to include in request';
COMMENT ON COLUMN api_definitions.body_template IS 'Request body template (supports variable substitution)';
COMMENT ON COLUMN api_definitions.response_mapping IS 'JSONPath expressions to extract specific fields from response';
COMMENT ON COLUMN api_definitions.timeout_override_ms IS 'Override timeout from credential (if specified)';
COMMENT ON COLUMN api_definitions.tags IS 'Array of tags for categorization and search';
