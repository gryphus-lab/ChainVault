/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.entity.MigrationAudit;
import ch.gryphus.chainvault.service.AuditEventService;
import ch.gryphus.chainvault.service.MigrationServiceException;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

/**
 * The type Migration executor.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MigrationExecutor {

    private final AuditEventService auditEventService;

    /**
     * Execute step.
     *
     * @param execution the execution
     * @param taskType the task type
     * @param errorCode the error code
     * @param task the task
     */
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

    /**
     * The interface Migration task.
     */
    @FunctionalInterface
    public interface MigrationTask {
        /**
         * Run.
         *
         * @param span the span
         * @param docId the doc id
         * @throws IOException the io exception
         * @throws MigrationServiceException the migration service exception
         */
        void run(Span span, String docId) throws IOException, MigrationServiceException;
    }
}
