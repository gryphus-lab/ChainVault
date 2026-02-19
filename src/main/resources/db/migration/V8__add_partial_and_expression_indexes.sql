-- V8__add_partial_and_expression_indexes.sql
-- Adjusted to avoid non-immutable functions (CURRENT_DATE, NOW(), etc.) in partial index predicates

-- Partial: only index active / problematic records (this one is fine – no date functions)
CREATE INDEX idx_migration_audit_active_problems
    ON migration_audit (status, attempt_count DESC)
    WHERE status IN ('RUNNING', 'RETRYING', 'FAILED');

-- Expression index: normalize document_id (case-insensitive lookup) – this is safe
CREATE INDEX idx_migration_audit_docid_lower
    ON migration_audit (LOWER(document_id));

-- Replacement for "recent uploads" partial index:
--   → Use a normal descending index on completed_at + output_file_key
--   → Application can add WHERE completed_at > ... in queries – Postgres will use the index efficiently
CREATE INDEX idx_migration_audit_recent_uploads_alt
    ON migration_audit (completed_at DESC, output_file_key)
    WHERE output_file_key IS NOT NULL;


-- Replacement for "failed last week" partial index:
--   → Use a normal composite index – very fast for this pattern anyway
CREATE INDEX idx_migration_audit_failed_recent_alt
    ON migration_audit (status, created_at DESC)
    WHERE status = 'FAILED';