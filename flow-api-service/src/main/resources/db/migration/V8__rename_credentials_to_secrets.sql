-- V8: Rename api_credentials to secrets
-- Secrets store reusable authentication credentials that can be referenced by APIs

-- Rename the table
ALTER TABLE api_credentials RENAME TO secrets;

-- Rename indexes
ALTER INDEX IF EXISTS idx_api_credentials_name RENAME TO idx_secrets_name;
ALTER INDEX IF EXISTS idx_api_credentials_active RENAME TO idx_secrets_active;
ALTER INDEX IF EXISTS idx_api_credentials_created_at RENAME TO idx_secrets_created_at;

-- Rename unique constraint
ALTER TABLE secrets DROP CONSTRAINT IF EXISTS api_credentials_name_unique;
ALTER TABLE secrets ADD CONSTRAINT secrets_name_unique UNIQUE (name);

-- Update table comment
COMMENT ON TABLE secrets IS 'Stores encrypted secrets for external service integrations';
COMMENT ON COLUMN secrets.secret_data IS 'Encrypted key-value pairs. Example: {"api_key": "sk-123", "client_id": "abc"}';
COMMENT ON COLUMN secrets.tags IS 'Tags for categorization. Example: ["production", "github"]';
