-- ───────────────────────────────────────────────
-- Performance: Indexes on migration_event (usually much larger table)
-- ───────────────────────────────────────────────

-- Primary access pattern: events for a specific migration
-- (already have idx_migration_event_audit_id, but make it more efficient)
CREATE INDEX idx_migration_event_audit_created
    ON migration_event (migration_audit_id, created_at ASC);

-- For timeline / sequence reconstruction of a single migration
CREATE INDEX idx_migration_event_audit_event_sequence
    ON migration_event (migration_audit_id, created_at ASC, event_type);

-- Covering index for "last event per migration" queries
CREATE INDEX idx_migration_event_last_per_audit_covering ON migration_event USING btree
    (migration_audit_id, created_at DESC)
    INCLUDE (event_type, message, error_code);

-- Frequent dashboard: count failures / retries by type
CREATE INDEX idx_migration_event_failed_by_type
    ON migration_event (event_type, created_at DESC)
    WHERE event_type IN ('TASK_FAILED', 'ERROR_BOUNDARY');

-- For "recent events" monitoring – descending btree index
CREATE INDEX idx_migration_event_recent_events
    ON migration_event (created_at DESC);

-- Optional: BRIN index – very low overhead, great for time-range scans
CREATE INDEX idx_migration_event_created_brin
    ON migration_event USING brin (created_at);