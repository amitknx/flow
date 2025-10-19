-- =============================================
-- Flowboard - Workflows Table Schema
-- =============================================

-- Create workflows table
CREATE TABLE IF NOT EXISTS workflows (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(2000),
    data JSONB NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100),
    modified_at TIMESTAMP NOT NULL,
    modified_by VARCHAR(100)
);

-- =============================================
-- Indexes for Performance
-- =============================================

-- Index on active flag for filtering
CREATE INDEX IF NOT EXISTS idx_workflows_active
ON workflows(active);

-- Index for full-text search on name and description
CREATE INDEX IF NOT EXISTS idx_workflows_search
ON workflows USING gin(to_tsvector('english', name || ' ' || COALESCE(description, '')));

-- Index on created_at for sorting by creation time (descending)
CREATE INDEX IF NOT EXISTS idx_workflows_created_at
ON workflows(created_at DESC);

-- GIN index on JSONB data for efficient JSON queries
CREATE INDEX IF NOT EXISTS idx_workflows_data
ON workflows USING gin(data);

-- Composite index for common query patterns (active + created_at)
CREATE INDEX IF NOT EXISTS idx_workflows_active_created
ON workflows(active, created_at DESC);

-- =============================================
-- Comments
-- =============================================

COMMENT ON TABLE workflows IS 'Stores workflow definitions with React Flow data in JSONB format';
COMMENT ON COLUMN workflows.id IS 'UUID v7 primary key (time-ordered for better index performance)';
COMMENT ON COLUMN workflows.name IS 'Workflow name (1-200 characters)';
COMMENT ON COLUMN workflows.description IS 'Optional workflow description (up to 2000 characters)';
COMMENT ON COLUMN workflows.data IS 'React Flow JSON data (nodes, edges, positions, globals)';
COMMENT ON COLUMN workflows.active IS 'Soft delete flag - false means deleted';
COMMENT ON COLUMN workflows.created_at IS 'Timestamp when workflow was created';
COMMENT ON COLUMN workflows.created_by IS 'User who created the workflow';
COMMENT ON COLUMN workflows.modified_at IS 'Timestamp of last modification';
COMMENT ON COLUMN workflows.modified_by IS 'User who last modified the workflow';

-- =============================================
-- Sample Data (Optional)
-- =============================================

-- Uncomment to insert sample workflow
/*
INSERT INTO workflows (
    id,
    name,
    description,
    data,
    active,
    created_at,
    created_by,
    modified_at,
    modified_by
) VALUES (
    gen_random_uuid(),
    'Sample Order Processing',
    'Example workflow for processing orders',
    '{"nodes": [{"id": "start", "type": "START", "position": {"x": 100, "y": 100}}], "edges": []}',
    true,
    NOW(),
    'system',
    NOW(),
    'system'
);
*/
