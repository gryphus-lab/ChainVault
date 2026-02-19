-- ───────────────────────────────────────────────
-- Performance: More targeted indexes on migration_audit
-- ───────────────────────────────────────────────

-- Composite: common filter + sort combination (status + time)
CREATE INDEX idx_migration_audit_status_created
    ON migration_audit (status, created_at DESC);

-- For "recent failures" dashboard / monitoring queries
CREATE INDEX idx_migration_audit_failed_recent
    ON migration_audit (status, created_at DESC)
    WHERE status IN ('FAILED', 'RETRYING');

-- Composite: document + time range lookups (very common pattern)
CREATE INDEX idx_migration_audit_docid_created
    ON migration_audit (document_id, created_at DESC);

-- Covering index for "list by process instance with key columns"
CREATE INDEX idx_migration_audit_process_key_covering ON migration_audit USING btree
    (process_instance_key)
    INCLUDE (document_id, status, created_at, completed_at, attempt_count);

-- For counting attempts per document (debugging duplicates/retries)
CREATE INDEX idx_migration_audit_docid_attempt
    ON migration_audit (document_id, attempt_count);

-- Optional: BRIN index for very large time-based partitions (cheaper than B-tree on huge tables)
-- CREATE INDEX idx_migration_audit_created_brin ON migration_audit USING brin (created_at);