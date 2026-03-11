/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.config.Constants;
import ch.gryphus.chainvault.entity.MigrationAudit;
import ch.gryphus.chainvault.service.AuditEventService;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.*;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

/**
 * The type Abstract tracing delegate.
 */
public abstract class AbstractTracingDelegate implements JavaDelegate {

    protected abstract AuditEventService getAuditEventService();

    protected abstract String getTaskType();

    protected abstract String getErrorCode();

    @Override
    public void execute(DelegateExecution execution) {
        String traceParent = (String) execution.getVariable("traceParent");
        Context parentContext = extractContext(traceParent);

        // Start the span as a child
        Span span =
                GlobalOpenTelemetry.getTracer("chainvault-tracer")
                        .spanBuilder(getTaskType())
                        .setParent(parentContext)
                        .startSpan();

        try (Scope scope = span.makeCurrent()) {
            executeManagedStep(execution, span);
        } catch (Exception e) {
            span.recordException(e);
            getAuditEventService()
                    .handleException(
                            e,
                            span,
                            execution.getProcessInstanceId(),
                            getErrorCode(),
                            getTaskType());
        } finally {
            span.end();
        }
    }

    private void executeManagedStep(DelegateExecution execution, Span span) throws Exception {
        String docId = (String) execution.getVariable(Constants.BPMN_PROC_VAR_DOC_ID);
        span.setAttribute(Constants.SPAN_ATTR_DOCUMENT_ID, docId);

        getAuditEventService()
                .updateAuditEventStart(
                        execution.getProcessInstanceId(), docId, getTaskType(), span);

        // Run the specific task logic
        doExecute(execution, span, docId);

        span.addEvent(
                getTaskType() + ".success",
                Attributes.of(AttributeKey.stringKey(Constants.SPAN_ATTR_DOCUMENT_ID), docId));
        getAuditEventService()
                .updateAuditEventEnd(
                        execution.getProcessInstanceId(),
                        MigrationAudit.MigrationStatus.SUCCESS,
                        null,
                        null,
                        getTaskType(),
                        getTaskType() + " completed",
                        execution.getTransientVariables());
    }

    protected abstract void doExecute(DelegateExecution execution, Span span, String docId)
            throws IOException, NoSuchAlgorithmException;

    private static Context extractContext(String traceParent) {
        if (traceParent == null) return Context.root();
        return GlobalOpenTelemetry.getPropagators()
                .getTextMapPropagator()
                .extract(
                        Context.current(),
                        Collections.singletonMap("traceparent", traceParent),
                        MapGetter.INSTANCE);
    }

    // Simple helper for the extractor
    private static class MapGetter implements TextMapGetter<Map<String, String>> {
        /**
         * The Instance.
         */
        static final MapGetter INSTANCE = new MapGetter();

        public String get(Map<String, String> carrier, String s) {
            return Objects.requireNonNull(carrier).get(s);
        }

        public Iterable<String> keys(Map<String, String> carrier) {
            return carrier.keySet();
        }
    }
}
