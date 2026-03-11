/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import ch.gryphus.chainvault.service.MigrationService;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
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
        String traceParent = (String) execution.getVariable("traceParent");

        // Reconstruct the context from the string
        Context parentContext = extractContextFromTraceParent(traceParent);

        Span childSpan =
                tracer.spanBuilder("Task: Async Init").setParent(parentContext).startSpan();

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
        } finally {
            childSpan.end();
        }
    }

    private static Context extractContextFromTraceParent(String traceParent) {
        // Inject the header into a temporary map
        Map<String, String> carrier = Collections.singletonMap("traceparent", traceParent);

        // Extract the context using OTel's built-in propagator
        return GlobalOpenTelemetry.getPropagators()
                .getTextMapPropagator()
                .extract(Context.current(), carrier, MapGetter.INSTANCE);
    }

    // Simple helper for the extractor
    private static class MapGetter implements TextMapGetter<Map<String, String>> {
        static final MapGetter INSTANCE = new MapGetter();

        public String get(Map<String, String> carrier, String s) {
            return Objects.requireNonNull(carrier).get(s);
        }

        public Iterable<String> keys(Map<String, String> carrier) {
            return carrier.keySet();
        }
    }
}
