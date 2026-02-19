-- ───────────────────────────────────────────────
-- Reporting & analytics patterns
-- ───────────────────────────────────────────────

-- "Migrations per source system + status" (overview dashboard)
CREATE INDEX idx_migration_audit_source_status_created
    ON migration_audit (source_system, status, created_at DESC);

-- "Failed migrations by error code" (troubleshooting)
CREATE INDEX idx_migration_audit_errorcode_status
    ON migration_audit (error_code, status)
    WHERE status = 'FAILED';

-- "Average duration per task type" preparation (needs event table join)
CREATE INDEX idx_migration_event_tasktype_audit
    ON migration_event (task_type, migration_audit_id)
    WHERE task_type IS NOT NULL;

-- Speed up joins between audit and events when filtering by date range
CREATE INDEX idx_migration_event_created_audit
    ON migration_event (created_at, migration_audit_id);