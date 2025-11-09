-- Remove retry_config and timeout_ms from api_credentials (belongs to API behavior, not auth)
ALTER TABLE api_credentials DROP COLUMN IF EXISTS retry_config;
ALTER TABLE api_credentials DROP COLUMN IF EXISTS timeout_ms;
ALTER TABLE api_credentials DROP CONSTRAINT IF EXISTS api_credentials_timeout_check;

-- Add default retry_config and timeout_ms to apis table (API-level defaults)
ALTER TABLE apis ADD COLUMN IF NOT EXISTS timeout_ms INTEGER DEFAULT 30000;
ALTER TABLE apis ADD COLUMN IF NOT EXISTS retry_config JSONB;

-- Add constraint for API-level timeout
ALTER TABLE apis ADD CONSTRAINT apis_timeout_check CHECK (
    timeout_ms IS NULL OR (timeout_ms >= 1000 AND timeout_ms <= 300000)
);

-- Update comments
COMMENT ON COLUMN apis.timeout_ms IS 'Default timeout for all endpoints in this API (can be overridden per endpoint)';
COMMENT ON COLUMN apis.retry_config IS 'Default retry configuration for all endpoints (can be overridden per endpoint)';
COMMENT ON COLUMN api_endpoints.timeout_ms IS 'Endpoint-specific timeout override (overrides API default)';
COMMENT ON COLUMN api_endpoints.retry_config IS 'Endpoint-specific retry config override (overrides API default)';
