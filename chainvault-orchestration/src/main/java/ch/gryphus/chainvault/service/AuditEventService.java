/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.service;

import ch.gryphus.chainvault.entity.MigrationAudit;
import ch.gryphus.chainvault.entity.MigrationEvent;
import ch.gryphus.chainvault.repository.MigrationAuditRepository;
import ch.gryphus.chainvault.repository.MigrationEventRepository;
import io.opentelemetry.api.trace.Span;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditEventService {

    private final MigrationAuditRepository auditRepo;
    private final MigrationEventRepository eventRepo;

    public void updateAuditEventStart(
            String processInstanceId, String docId, String eventTaskType) {

        MigrationAudit audit =
                auditRepo
                        .findByProcessInstanceKey(processInstanceId)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "No audit for " + processInstanceId));

        audit.setProcessInstanceKey(processInstanceId);
        audit.setDocumentId(docId);
        audit.setAttemptCount(audit.getAttemptCount() + 1);
        audit.setStatus(MigrationAudit.MigrationStatus.RUNNING);
        audit.setStartedAt(Instant.now());
        auditRepo.save(audit);

        MigrationEvent event = new MigrationEvent();
        event.setMigrationAuditId(audit.getId());
        event.setEventType(MigrationEvent.MigrationEventType.TASK_STARTED);
        event.setTaskType(eventTaskType);
        event.setCreatedAt(Instant.now());
        eventRepo.save(event);
    }

    public void updateAuditEventEnd(
            String processInstanceId,
            MigrationAudit.MigrationStatus status,
            String errorCode,
            String errorMsg,
            String eventTaskType,
            String eventMsg) {
        MigrationAudit audit =
                auditRepo
                        .findByProcessInstanceKey(processInstanceId)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "No audit for " + processInstanceId));

        audit.setStatus(status);

        if (status == MigrationAudit.MigrationStatus.FAILED) {
            audit.setFailureReason(errorMsg);
            audit.setErrorCode(errorCode);
        }

        audit.setCompletedAt(Instant.now());
        audit.setTraceId(Span.current().getSpanContext().getTraceId());
        auditRepo.save(audit);

        MigrationEvent event = new MigrationEvent();
        event.setMigrationAuditId(audit.getId());

        event.setEventType(
                status == MigrationAudit.MigrationStatus.FAILED
                        ? MigrationEvent.MigrationEventType.TASK_FAILED
                        : MigrationEvent.MigrationEventType.TASK_COMPLETED);

        event.setTaskType(eventTaskType);
        event.setMessage(eventMsg);
        eventRepo.save(event);
    }
}
