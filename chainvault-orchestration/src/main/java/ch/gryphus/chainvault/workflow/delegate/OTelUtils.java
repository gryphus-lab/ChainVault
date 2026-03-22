/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.workflow.delegate;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import java.util.Collections;
import java.util.Map;

/**
 * The type O tel utils.
 */
final class OTelUtils {
    private OTelUtils() {
        /* This utility class should not be instantiated */
    }

    // Explicitly define the Getter for Map types
    private static final TextMapGetter<Map<String, String>> MAP_GETTER = new MapTextMapGetter();

    /**
     * Extract context context.
     *
     * @param openTelemetry the open telemetry
     * @param traceParent   the trace parent
     * @return the context
     */
    static Context extractContext(OpenTelemetry openTelemetry, String traceParent) {
        if (traceParent == null || traceParent.isEmpty()) {
            return Context.root();
        }

        // Wrap the string in a map that the Getter understands
        Map<String, String> carrier = Collections.singletonMap("traceparent", traceParent);

        return openTelemetry
                .getPropagators()
                .getTextMapPropagator()
                .extract(Context.current(), carrier, MAP_GETTER);
    }

    private static class MapTextMapGetter implements TextMapGetter<Map<String, String>> {
        @Override
        public Iterable<String> keys(Map<String, String> carrier) {
            return carrier.keySet();
        }

        @Override
        public String get(Map<String, String> carrier, String s) {
            return carrier == null ? null : carrier.get(s);
        }
    }
}
