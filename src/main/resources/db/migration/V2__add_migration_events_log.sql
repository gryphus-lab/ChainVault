-- Fine-grained event log for each migration (optional but very useful for debugging & audit)
CREATE TABLE migration_event (
                                 id                      BIGSERIAL           PRIMARY KEY,
                                 migration_audit_id      BIGINT              NOT NULL
                                     REFERENCES migration_audit(id) ON DELETE CASCADE,

                                 event_type              VARCHAR(60)         NOT NULL            -- e.g. STARTED, TASK_COMPLETED, TASK_FAILED, RETRY, UPLOADED, ERROR_BOUNDARY
                                     CHECK (event_type IN (
                                                           'STARTED', 'TASK_STARTED', 'TASK_COMPLETED', 'TASK_FAILED',
                                                           'ERROR_BOUNDARY', 'RETRY', 'CANCELLED', 'SUCCESS', 'UPLOADED',
                                                           'ZIP_CREATED', 'PDF_MERGED', 'METADATA_GENERATED'
                                         )),

                                 task_type               VARCHAR(120),                           -- zeebe task type e.g. extract-and-hash, upload-sftp
                                 task_element_id         VARCHAR(100),                           -- BPMN element ID
                                 activity_id             VARCHAR(100),

                                 message                 TEXT,
                                 error_code              VARCHAR(80),
                                 error_message           TEXT,

                                 created_at              TIMESTAMPTZ         DEFAULT NOW()       NOT NULL,
                                 event_data              JSONB                                       -- flexible structured data (variables snapshot, hashes, etc.)
);

CREATE INDEX idx_migration_event_audit_id
    ON migration_event (migration_audit_id);

CREATE INDEX idx_migration_event_created_at
    ON migration_event (created_at DESC);

CREATE INDEX idx_migration_event_event_type
    ON migration_event (event_type);