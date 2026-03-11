/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.service.MigrationService;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * The type Init variables service.
 */
@Slf4j
@Component("asyncInitVars")
@RequiredArgsConstructor
public class AsyncInitVariablesDelegate implements JavaDelegate {

    private final MigrationService migrationService;
    private final MigrationExecutor executor;
    private final Tracer tracer;

    @Override
    public void execute(DelegateExecution execution) {
        Context parentContext = (Context) execution.getVariable("parentContext");
        if (parentContext == null) {
            parentContext = Context.current();
        }

        Span childSpan =
                tracer.spanBuilder("Flowable: %s".formatted(execution.getCurrentActivityId()))
                        .setParent(parentContext)
                        .setSpanKind(SpanKind.INTERNAL)
                        .startSpan();

        try (Scope scope = childSpan.makeCurrent()) {
            executor.executeStep(
                    execution,
                    "async-init-vars",
                    "ASYNC-INIT_FAILED",
                    (span, docId, map) ->
                            log.info(
                                    "async-init-vars executed for docId {}, and scope: {}",
                                    docId,
                                    scope));
        }
    }
}
