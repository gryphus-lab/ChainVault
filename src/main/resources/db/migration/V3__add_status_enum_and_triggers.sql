-- Optional: create a more strict status enum type (PostgreSQL)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'migration_status') THEN
CREATE TYPE migration_status AS ENUM (
            'PENDING', 'RUNNING', 'SUCCESS', 'FAILED', 'CANCELLED', 'RETRYING'
        );
END IF;
END $$;

-- You can later change the column type if you want stricter enforcement
-- ALTER TABLE migration_audit ALTER COLUMN status TYPE migration_status USING status::migration_status;

-- Trigger to update last_updated_at and attempt_count on status change
CREATE OR REPLACE FUNCTION update_migration_audit_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.last_updated_at = NOW();

    IF NEW.status = 'RETRYING' AND OLD.status != 'RETRYING' THEN
        NEW.attempt_count = OLD.attempt_count + 1;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_migration_audit_update_timestamp
    BEFORE UPDATE ON migration_audit
    FOR EACH ROW
    EXECUTE FUNCTION update_migration_audit_timestamp();