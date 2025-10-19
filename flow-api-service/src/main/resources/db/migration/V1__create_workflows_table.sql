-- Create workflows table
CREATE TABLE workflows (
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

-- Create index on active flag for filtering active workflows
CREATE INDEX idx_workflows_active ON workflows(active);

-- Create index for full-text search on name and description
CREATE INDEX idx_workflows_search ON workflows USING gin(to_tsvector('english', name || ' ' || COALESCE(description, '')));

-- Create index on created_at for sorting by creation time
CREATE INDEX idx_workflows_created_at ON workflows(created_at DESC);

-- Create GIN index on JSONB data for efficient JSON queries
CREATE INDEX idx_workflows_data ON workflows USING gin(data);

-- Add comment to table
COMMENT ON TABLE workflows IS 'Stores workflow definitions with React Flow data in JSONB format';
COMMENT ON COLUMN workflows.id IS 'UUID v7 primary key (time-ordered)';
COMMENT ON COLUMN workflows.data IS 'React Flow JSON data (nodes, edges, positions)';
COMMENT ON COLUMN workflows.active IS 'Soft delete flag - false means deleted';
