/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

/**
 * The type Abstract tracing delegate.
 */
public abstract class AbstractTracingDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        // 1. Get the trace context string from the process variables
        String traceParent = (String) execution.getVariable("traceParent");

        // 2. Extract into OTel context
        Context parentContext = extractContext(traceParent);

        // 3. Start the span
        Span span =
                GlobalOpenTelemetry.getTracer("chainvault-tracer")
                        .spanBuilder(execution.getCurrentActivityId())
                        .setParent(parentContext)
                        .startSpan();

        try (Scope scope = span.makeCurrent()) {
            doExecute(execution);
        } finally {
            span.end();
        }
    }

    /**
     * Do execute.
     *
     * @param execution the execution
     */
    protected abstract void doExecute(DelegateExecution execution);

    private Context extractContext(String traceParent) {
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
