-- V7: Refactor api_credentials to simple key-value store and move authentication to APIs
-- Credentials now only store key-value pairs (name, description, tags, secret_data)
-- APIs now have authentication configuration that can reference secret values

-- Update api_credentials table - remove auth-related columns
ALTER TABLE api_credentials DROP CONSTRAINT IF EXISTS api_credentials_auth_type_check;
ALTER TABLE api_credentials DROP COLUMN IF EXISTS base_url;
ALTER TABLE api_credentials DROP COLUMN IF EXISTS auth_type;
ALTER TABLE api_credentials DROP COLUMN IF EXISTS default_headers;

-- Rename credential_data to secret_data for clarity
ALTER TABLE api_credentials RENAME COLUMN credential_data TO secret_data;

-- Add tags column to api_credentials
ALTER TABLE api_credentials ADD COLUMN IF NOT EXISTS tags JSONB;

-- Drop the auth_type index since we removed that column
DROP INDEX IF EXISTS idx_api_credentials_auth_type;

-- Update APIs table - remove credential_id foreign key and add authentication column
ALTER TABLE apis DROP CONSTRAINT IF EXISTS apis_credential_fk;
ALTER TABLE apis DROP COLUMN IF EXISTS credential_id;

-- Drop the credential_id index
DROP INDEX IF EXISTS idx_apis_credential_id;

-- Add authentication column to apis table
ALTER TABLE apis ADD COLUMN IF NOT EXISTS authentication JSONB;

-- Add comment explaining the authentication structure
COMMENT ON COLUMN apis.authentication IS 'Authentication configuration as JSONB. Example: {"type": "API_KEY", "config": {"headerName": "X-API-Key", "valueRef": "secret.github_prod.api_key"}}';
COMMENT ON COLUMN api_credentials.secret_data IS 'Encrypted key-value pairs. Example: {"api_key": "sk-123", "client_id": "abc"}';
COMMENT ON COLUMN api_credentials.tags IS 'Tags for categorization. Example: ["production", "github"]';
