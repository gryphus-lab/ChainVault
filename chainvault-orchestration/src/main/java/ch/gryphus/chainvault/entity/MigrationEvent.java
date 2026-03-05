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
        schema = "chainvault",
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

    @Column(name = "migration_audit_id", nullable = false)
    private Long migrationAuditId;

    @Column(name = "event_type", nullable = false, length = 60)
    @Enumerated(EnumType.STRING)
    private MigrationEventType eventType;

    @Column(name = "task_type", length = 120)
    private String taskType;

    @Column(name = "activity_id", length = 100)
    private String activityId;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "error_code", length = 80)
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "event_data", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> eventData;

    // Optional: correlation ID for distributed tracing (if using OpenTelemetry)
    @Column(name = "trace_id", length = 64)
    private String traceId;

    /**
     * The enum Migration event type.
     */
    public enum MigrationEventType {
        /**
         * Process started migration event type.
         */
        PROCESS_STARTED,
        /**
         * Process ended migration event type.
         */
        PROCESS_ENDED,
        /**
         * Task started migration event type.
         */
        TASK_STARTED,
        /**
         * Task completed migration event type.
         */
        TASK_COMPLETED,
        /**
         * Task failed migration event type.
         */
        TASK_FAILED,
        /**
         * Error boundary triggered migration event type.
         */
        ERROR_BOUNDARY_TRIGGERED,
        /**
         * Retry attempted migration event type.
         */
        RETRY_ATTEMPTED,
        /**
         * Compensation executed migration event type.
         */
        COMPENSATION_EXECUTED,
        /**
         * Compensation failed migration event type.
         */
        COMPENSATION_FAILED,
        /**
         * Status updated migration event type.
         */
        STATUS_UPDATED,
        /**
         * Uploaded migration event type.
         */
        UPLOADED,
        /**
         * Zip created migration event type.
         */
        ZIP_CREATED,
        /**
         * Pdf merged migration event type.
         */
        PDF_MERGED,
        /**
         * Metadata generated migration event type.
         */
        METADATA_GENERATED
    }
}
