-- Convenient reporting view
CREATE VIEW migration_summary AS
SELECT
    ma.id,
    ma.document_id,
    ma.status,
    ma.attempt_count,
    ma.created_at,
    ma.completed_at,
    AGE(COALESCE(ma.completed_at, NOW()), ma.created_at) AS duration,
    ma.failure_reason,
    ma.error_code,
    COUNT(me.id) AS event_count,
    MAX(me.created_at) FILTER (WHERE me.event_type = 'UPLOADED') AS uploaded_at,
    MAX(me.created_at) FILTER (WHERE me.event_type = 'PDF_MERGED') AS merged_at
FROM migration_audit ma
         LEFT JOIN migration_event me ON me.migration_audit_id = ma.id
GROUP BY ma.id, ma.created_at
ORDER BY ma.created_at DESC;