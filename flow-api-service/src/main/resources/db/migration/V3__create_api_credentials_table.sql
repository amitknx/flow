-- Create API credentials table for storing external API configurations
CREATE TABLE api_credentials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    base_url VARCHAR(500) NOT NULL,
    auth_type VARCHAR(50) NOT NULL,
    credential_data JSONB NOT NULL,
    default_headers JSONB,
    timeout_ms INTEGER DEFAULT 30000 NOT NULL,
    retry_config JSONB,
    active BOOLEAN DEFAULT true NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_by VARCHAR(100) NOT NULL DEFAULT 'system',

    -- Constraints
    CONSTRAINT api_credentials_name_unique UNIQUE (name),
    CONSTRAINT api_credentials_timeout_check CHECK (timeout_ms >= 1000 AND timeout_ms <= 300000),
    CONSTRAINT api_credentials_auth_type_check CHECK (auth_type IN ('NONE', 'API_KEY', 'BEARER_TOKEN', 'BASIC_AUTH', 'OAUTH2'))
);

-- Create indexes for performance
CREATE INDEX idx_api_credentials_name ON api_credentials(name) WHERE active = true;
CREATE INDEX idx_api_credentials_active ON api_credentials(active);
CREATE INDEX idx_api_credentials_created_at ON api_credentials(created_at DESC);
CREATE INDEX idx_api_credentials_auth_type ON api_credentials(auth_type);

-- Add comment for documentation
COMMENT ON TABLE api_credentials IS 'Stores encrypted API credentials for external service integrations';
COMMENT ON COLUMN api_credentials.credential_data IS 'Encrypted JSON containing sensitive authentication data';
COMMENT ON COLUMN api_credentials.default_headers IS 'Default HTTP headers to be included in all requests';
COMMENT ON COLUMN api_credentials.retry_config IS 'Retry configuration: {maxRetries, backoffMs, retryableStatuses}';
