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
 * Main audit record for each document migration attempt.
 * Tracks lifecycle, status, correlation to Flowable process, and key outputs/errors.
 */
@Entity
@Table(
        name = "migration_audit",
        indexes = {
            @Index(
                    name = "idx_migration_audit_process_instance_key",
                    columnList = "process_instance_key"),
            @Index(name = "idx_migration_audit_document_id", columnList = "document_id"),
            @Index(name = "idx_migration_audit_status", columnList = "status"),
            @Index(name = "idx_migration_audit_created_at", columnList = "created_at DESC"),
            @Index(
                    name = "idx_migration_audit_source_status_created",
                    columnList = "source_system, status, created_at DESC"),
            @Index(name = "idx_migration_audit_trace_id", columnList = "trace_id")
        },
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_migration_unique",
                    columnNames = {"process_instance_key", "document_id"})
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"inputPayloadHash", "eventData"})
public class MigrationAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ───────────────────────────────────────────────
    // Flowable / Process Correlation
    // ───────────────────────────────────────────────

    @Column(name = "process_instance_key", nullable = false)
    private Long processInstanceKey;

    @Column(name = "process_definition_key", nullable = false)
    private Long processDefinitionKey;

    @Column(name = "bpmn_process_id", nullable = false, length = 100)
    private String bpmnProcessId;

    // Optional: business key if set in process variables
    @Column(name = "business_key", length = 255)
    private String businessKey;

    // ───────────────────────────────────────────────
    // Business Identifiers
    // ───────────────────────────────────────────────

    @Column(name = "document_id", nullable = false, length = 120)
    private String documentId;

    @Column(name = "document_external_id", length = 120)
    private String documentExternalId; // e.g. original ID in legacy system

    @Column(name = "source_system", length = 80)
    private String sourceSystem; // e.g. "legacy-archive", "sharepoint", "email"

    @Column(name = "target_system", length = 80)
    private String targetSystem = "finma-archive"; // default value

    // ───────────────────────────────────────────────
    // Status & Lifecycle
    // ───────────────────────────────────────────────

    @Column(name = "status", nullable = false, length = 40)
    @Enumerated(EnumType.STRING)
    private MigrationStatus status;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "error_code", length = 80)
    private String errorCode;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 1;

    // ───────────────────────────────────────────────
    // Timestamps
    // ───────────────────────────────────────────────

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "last_updated_at", nullable = false)
    private Instant lastUpdatedAt = Instant.now();

    // ───────────────────────────────────────────────
    // Output & Evidence References
    // ───────────────────────────────────────────────

    @Column(name = "input_payload_hash", length = 128)
    private String inputPayloadHash; // e.g. SHA-256 of input metadata/payload

    @Column(name = "output_file_key", length = 255)
    private String outputFileKey; // e.g. S3 key, SFTP path, archive reference

    @Column(name = "chain_of_custody_zip", length = 512)
    private String chainOfCustodyZip; // path/reference to ZIP

    @Column(name = "merged_pdf_hash", length = 128)
    private String mergedPdfHash;

    // ───────────────────────────────────────────────
    // Optional: Tracing & Context
    // ───────────────────────────────────────────────

    @Column(name = "trace_id", length = 64)
    private String traceId; // OpenTelemetry trace ID for correlation

    // Flexible extra context (e.g. variables snapshot, custom metadata)
    @Column(name = "event_data", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> eventData;

    // ───────────────────────────────────────────────
    // Status Enum (matches CHECK constraint in Liquibase)
    // ───────────────────────────────────────────────
    public enum MigrationStatus {
        PENDING,
        RUNNING,
        SUCCESS,
        FAILED,
        CANCELLED,
        RETRYING,
        COMPENSATED
    }

    // Optional: auto-update last_updated_at
    @PreUpdate
    protected void onUpdate() {
        this.lastUpdatedAt = Instant.now();
    }
}
