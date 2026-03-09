/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.config.Constants;
import ch.gryphus.chainvault.entity.MigrationAudit;
import ch.gryphus.chainvault.service.AuditEventService;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
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
     * @param taskType  the task type
     * @param errorCode the error code
     * @param task      the task
     */
    public void executeStep(
            DelegateExecution execution, String taskType, String errorCode, MigrationTask task) {
        Span span = Span.current();
        String docId = (String) execution.getVariable(Constants.BPMN_PROC_VAR_DOC_ID);
        String piKey = execution.getProcessInstanceId();

        span.setAttribute(Constants.SPAN_ATTR_DOCUMENT_ID, docId);
        log.info("{} started for docId: {}", taskType, docId);
        auditEventService.updateAuditEventStart(piKey, docId, taskType, span);

        try {
            // Run the unique logic
            task.run(span, docId);

            span.addEvent(
                    "%s.success".formatted(taskType),
                    Attributes.of(AttributeKey.stringKey(Constants.SPAN_ATTR_DOCUMENT_ID), docId));

            auditEventService.updateAuditEventEnd(
                    piKey,
                    MigrationAudit.MigrationStatus.SUCCESS,
                    null,
                    null,
                    taskType,
                    "%s completed successfully".formatted(taskType));

            log.info("{} completed for docId: {}", taskType, docId);
        } catch (Exception e) {
            log.error("{} encountered an error while executing step", taskType, e);
            auditEventService.handleException(e, span, piKey, errorCode, taskType);
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
         * @param span  the span
         * @param docId the doc id
         * @throws IOException              the io exception
         * @throws NoSuchAlgorithmException the no such algorithm exception
         */
        void run(Span span, String docId) throws IOException, NoSuchAlgorithmException;
    }
}
