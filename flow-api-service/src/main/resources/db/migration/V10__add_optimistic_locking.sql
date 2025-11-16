-- Add version columns for optimistic locking to prevent dirty writes

-- Add version column to apis table
ALTER TABLE apis ADD COLUMN version_num BIGINT DEFAULT 0;

-- Add version column to api_endpoints table
ALTER TABLE api_endpoints ADD COLUMN version_num BIGINT DEFAULT 0;

-- Comment for documentation
COMMENT ON COLUMN apis.version_num IS 'Version number for optimistic locking';
COMMENT ON COLUMN api_endpoints.version_num IS 'Version number for optimistic locking';
