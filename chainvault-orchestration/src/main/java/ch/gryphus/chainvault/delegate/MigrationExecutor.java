/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.entity.MigrationAudit;
import ch.gryphus.chainvault.service.AuditEventService;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MigrationExecutor {

    private final AuditEventService auditEventService;

    public void executeStep(
            DelegateExecution execution, String taskType, String errorCode, MigrationTask task) {
        Span span = Span.current();
        String docId = (String) execution.getVariable("docId");
        String piKey = execution.getProcessInstanceId();

        span.setAttribute("document.id", docId);
        log.info("{} started for docId: {}", taskType, docId);
        auditEventService.updateAuditEventStart(piKey, docId, taskType);

        try {
            // Run the unique logic
            task.run(span, docId);

            // Duplicated Success Logic
            span.addEvent(
                    "%s.success".formatted(taskType),
                    Attributes.of(AttributeKey.stringKey("document.id"), docId));

            auditEventService.updateAuditEventEnd(
                    piKey,
                    MigrationAudit.MigrationStatus.SUCCESS,
                    null,
                    null,
                    taskType,
                    taskType + " completed successfully");

        } catch (Exception e) {
            auditEventService.handleException(e, span, piKey, errorCode, taskType);
        } finally {
            log.info("{} completed for docId: {}", taskType, docId);
        }
    }

    @FunctionalInterface
    public interface MigrationTask {
        void run(Span span, String docId) throws Exception;
    }
}
