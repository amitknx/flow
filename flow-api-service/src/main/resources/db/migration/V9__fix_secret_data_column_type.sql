-- V9: Fix secret_data column type from TEXT to JSONB
-- The column should be JSONB to store encrypted key-value pairs
-- Since there's no data yet, we can safely alter the column type

-- Change secret_data column from TEXT to JSONB
ALTER TABLE secrets 
    ALTER COLUMN secret_data TYPE JSONB 
    USING secret_data::jsonb;

-- Add comment
COMMENT ON COLUMN secrets.secret_data IS 'Encrypted key-value pairs stored as JSONB. Values are encrypted, keys remain unencrypted. Example: {"api_key": "<encrypted>", "client_id": "<encrypted>"}';

