-- V3: Create project history table for versioning and rollback
-- Author: Bernard Uriza Orozco
-- Date: 2025-10-27

CREATE TABLE IF NOT EXISTS project_history (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    version_number INTEGER NOT NULL,
    snapshot_data JSONB NOT NULL,
    change_type VARCHAR(50) NOT NULL,
    changed_fields TEXT[],
    changed_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_project_history_project FOREIGN KEY (project_id) REFERENCES portfolio_projects(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_project_history_project_id ON project_history(project_id);
CREATE INDEX idx_project_history_created_at ON project_history(created_at DESC);
CREATE INDEX idx_project_history_version ON project_history(project_id, version_number);
CREATE INDEX idx_project_history_snapshot_gin ON project_history USING GIN (snapshot_data);

-- Create unique constraint to prevent duplicate versions
CREATE UNIQUE INDEX idx_project_history_unique_version ON project_history(project_id, version_number);

-- Add comment to table
COMMENT ON TABLE project_history IS 'Stores historical versions of portfolio projects for rollback and audit';
COMMENT ON COLUMN project_history.snapshot_data IS 'Complete JSON snapshot of the project state at this version';
COMMENT ON COLUMN project_history.change_type IS 'Type of change: CREATE, UPDATE, DELETE, ROLLBACK, SYNC, MANUAL';
COMMENT ON COLUMN project_history.changed_fields IS 'Array of field names that changed in this version';
COMMENT ON COLUMN project_history.changed_by IS 'User or system component that made the change';
