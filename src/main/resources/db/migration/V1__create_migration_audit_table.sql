-- Main table for tracking each document migration attempt
CREATE TABLE migration_audit (
    id                      BIGSERIAL           PRIMARY KEY,

    -- Camunda correlation
    process_instance_key    BIGINT              NOT NULL,
    process_definition_key  BIGINT              NOT NULL,
    bpmn_process_id         VARCHAR(100)        NOT NULL,

    -- Business identifiers
    document_id             VARCHAR(120)        NOT NULL,
    document_external_id    VARCHAR(120),                           -- e.g. original archive reference
    source_system           VARCHAR(80),                            -- e.g. 'legacy-archive', 'sharepoint', etc.
    target_system           VARCHAR(80)         DEFAULT 'finma-archive',

    -- Status & lifecycle
    status                  VARCHAR(40)         NOT NULL            -- PENDING, RUNNING, SUCCESS, FAILED, CANCELLED, RETRYING
        CHECK (status IN ('PENDING', 'RUNNING', 'SUCCESS', 'FAILED', 'CANCELLED', 'RETRYING')),
    failure_reason          TEXT,
    error_code              VARCHAR(80),                            -- e.g. EXTRACTION_FAILED, UPLOAD_FAILED
    attempt_count           INTEGER             DEFAULT 1           NOT NULL,

    -- Timestamps
    created_at              TIMESTAMPTZ         DEFAULT NOW()       NOT NULL,
    started_at              TIMESTAMPTZ,
    completed_at            TIMESTAMPTZ,
    last_updated_at         TIMESTAMPTZ         DEFAULT NOW()       NOT NULL,

    -- Optional context / payload references
    input_payload_hash      VARCHAR(128),                           -- SHA-256 of input metadata/payload
    output_file_key         VARCHAR(255),                           -- e.g. S3 key, SFTP path, or archive reference
    chain_of_custody_zip    VARCHAR(512),                           -- path or reference to ZIP
    merged_pdf_hash         VARCHAR(128),

    CONSTRAINT uk_migration_unique UNIQUE (process_instance_key, document_id)
);

CREATE INDEX idx_migration_audit_process_instance_key
    ON migration_audit (process_instance_key);

CREATE INDEX idx_migration_audit_document_id
    ON migration_audit (document_id);

CREATE INDEX idx_migration_audit_status
    ON migration_audit (status);

CREATE INDEX idx_migration_audit_created_at
    ON migration_audit (created_at DESC);