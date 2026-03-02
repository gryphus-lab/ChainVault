/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Represents a fine-grained event logged during a document migration process.
 * Each event is tied to one MigrationAudit record and captures a specific step or state change.
 */
@Entity
@Table(
        name = "migration_event",
        indexes = {
            @Index(name = "idx_migration_event_audit_id", columnList = "migration_audit_id"),
            @Index(name = "idx_migration_event_created_at", columnList = "created_at DESC"),
            @Index(name = "idx_migration_event_event_type", columnList = "event_type"),
            @Index(name = "idx_migration_audit_trace_id", columnList = "trace_id")
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"eventData"})
public class MigrationEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Foreign key to the parent migration audit record
     */
    @Column(name = "migration_audit_id", nullable = false)
    private Long migrationAuditId;

    /**
     * Type of event (e.g. TASK_STARTED, TASK_COMPLETED, TASK_FAILED, COMPENSATION_EXECUTED)
     */
    @Column(name = "event_type", nullable = false, length = 60)
    @Enumerated(EnumType.STRING)
    private MigrationEventType eventType;

    /**
     * The external task topic or internal activity name (e.g. "extract-hash", "upload-sftp")
     */
    @Column(name = "task_type", length = 120)
    private String taskType;

    /**
     * BPMN activity / element ID (optional)
     */
    @Column(name = "activity_id", length = 100)
    private String activityId;

    /**
     * Human-readable message or short description
     */
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    /**
     * Error code if this is a failure event (e.g. "EXTRACTION_FAILED")
     */
    @Column(name = "error_code", length = 80)
    private String errorCode;

    /**
     * Full exception message or detailed failure reason
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Timestamp when the event occurred
     */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    /**
     * Flexible structured data (variables snapshot, hashes, file paths, etc.)
     * Stored as JSONB in PostgreSQL
     */
    @Column(name = "event_data", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> eventData;

    // Optional: correlation ID for distributed tracing (if using OpenTelemetry)
    @Column(name = "trace_id", length = 64)
    private String traceId;

    // Optional: who/what triggered the event (system, user, worker-id)
    @Column(name = "triggered_by", length = 100)
    private String triggeredBy;

    /**
     * Enum for standardized event types
     */
    public enum MigrationEventType {
        PROCESS_STARTED,
        PROCESS_ENDED,
        TASK_STARTED,
        TASK_COMPLETED,
        TASK_FAILED,
        ERROR_BOUNDARY_TRIGGERED,
        RETRY_ATTEMPTED,
        COMPENSATION_EXECUTED,
        COMPENSATION_FAILED,
        STATUS_UPDATED,
        UPLOADED,
        ZIP_CREATED,
        PDF_MERGED,
        METADATA_GENERATED
    }
}
