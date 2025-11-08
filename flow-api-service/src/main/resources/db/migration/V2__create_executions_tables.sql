-- Create workflow_executions table
CREATE TABLE workflow_executions (
    id UUID PRIMARY KEY,
    workflow_id UUID NOT NULL,
    correlation_id VARCHAR(255),
    status VARCHAR(20) NOT NULL,
    trigger_type VARCHAR(20) NOT NULL,
    trigger_input JSONB,
    definition_snapshot_json JSONB NOT NULL,
    started_at TIMESTAMP,
    ended_at TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100),
    modified_at TIMESTAMP NOT NULL,
    modified_by VARCHAR(100),
    CONSTRAINT fk_workflow_executions_workflow FOREIGN KEY (workflow_id) REFERENCES workflows(id)
);

-- Create step_executions table
CREATE TABLE step_executions (
    id UUID PRIMARY KEY,
    workflow_execution_id UUID NOT NULL,
    node_id_ref VARCHAR(100) NOT NULL,
    step_name VARCHAR(200),
    status VARCHAR(20) NOT NULL,
    input_payload JSONB,
    output_payload JSONB,
    error_message TEXT,
    started_at TIMESTAMP,
    ended_at TIMESTAMP,
    duration_ms BIGINT,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100),
    modified_at TIMESTAMP NOT NULL,
    modified_by VARCHAR(100),
    CONSTRAINT fk_step_executions_workflow_execution FOREIGN KEY (workflow_execution_id) REFERENCES workflow_executions(id) ON DELETE CASCADE
);

-- Create indexes for workflow_executions
CREATE INDEX idx_workflow_executions_workflow_id ON workflow_executions(workflow_id);
CREATE INDEX idx_workflow_executions_correlation_id ON workflow_executions(correlation_id);
CREATE INDEX idx_workflow_executions_status ON workflow_executions(status);
CREATE INDEX idx_workflow_executions_active ON workflow_executions(active);
CREATE INDEX idx_workflow_executions_created_at ON workflow_executions(created_at DESC);
CREATE INDEX idx_workflow_executions_started_at ON workflow_executions(started_at DESC);

-- Create GIN indexes on JSONB columns for workflow_executions
CREATE INDEX idx_workflow_executions_trigger_input ON workflow_executions USING gin(trigger_input);
CREATE INDEX idx_workflow_executions_definition_snapshot ON workflow_executions USING gin(definition_snapshot_json);

-- Create indexes for step_executions
CREATE INDEX idx_step_executions_workflow_execution_id ON step_executions(workflow_execution_id);
CREATE INDEX idx_step_executions_node_id_ref ON step_executions(node_id_ref);
CREATE INDEX idx_step_executions_status ON step_executions(status);
CREATE INDEX idx_step_executions_active ON step_executions(active);
CREATE INDEX idx_step_executions_created_at ON step_executions(created_at DESC);

-- Create GIN indexes on JSONB columns for step_executions
CREATE INDEX idx_step_executions_input_payload ON step_executions USING gin(input_payload);
CREATE INDEX idx_step_executions_output_payload ON step_executions USING gin(output_payload);

-- Composite index for common query pattern: get steps for execution ordered by creation
CREATE INDEX idx_step_executions_exec_created ON step_executions(workflow_execution_id, created_at);

-- Add comments to tables
COMMENT ON TABLE workflow_executions IS 'Stores workflow execution instances with status tracking and immutable definition snapshots';
COMMENT ON COLUMN workflow_executions.id IS 'UUID v7 primary key (time-ordered)';
COMMENT ON COLUMN workflow_executions.workflow_id IS 'Reference to the workflow definition';
COMMENT ON COLUMN workflow_executions.correlation_id IS 'Optional identifier to link related executions across systems';
COMMENT ON COLUMN workflow_executions.status IS 'Execution status: PENDING, RUNNING, SUCCESS, FAILED, CANCELLED';
COMMENT ON COLUMN workflow_executions.trigger_type IS 'How execution was triggered: MANUAL, API, SCHEDULED';
COMMENT ON COLUMN workflow_executions.trigger_input IS 'Input parameters provided when execution was triggered';
COMMENT ON COLUMN workflow_executions.definition_snapshot_json IS 'Immutable snapshot of workflow definition at execution time';
COMMENT ON COLUMN workflow_executions.active IS 'Soft delete flag - false means deleted';

COMMENT ON TABLE step_executions IS 'Stores individual step executions within a workflow execution';
COMMENT ON COLUMN step_executions.id IS 'UUID v7 primary key (time-ordered)';
COMMENT ON COLUMN step_executions.workflow_execution_id IS 'Reference to parent workflow execution';
COMMENT ON COLUMN step_executions.node_id_ref IS 'Reference to node ID in workflow definition';
COMMENT ON COLUMN step_executions.step_name IS 'Human-readable step name';
COMMENT ON COLUMN step_executions.status IS 'Step status: PENDING, RUNNING, SUCCESS, FAILED, SKIPPED';
COMMENT ON COLUMN step_executions.input_payload IS 'Input data for this step';
COMMENT ON COLUMN step_executions.output_payload IS 'Output/result data from this step';
COMMENT ON COLUMN step_executions.error_message IS 'Error details if step failed';
COMMENT ON COLUMN step_executions.duration_ms IS 'Step execution duration in milliseconds';
COMMENT ON COLUMN step_executions.active IS 'Soft delete flag - false means deleted';
