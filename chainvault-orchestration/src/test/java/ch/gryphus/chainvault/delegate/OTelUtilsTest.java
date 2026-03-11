/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.delegate;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OTelUtilsTest {

    private OpenTelemetry otel;

    @BeforeEach
    void setUp() {
        otel = setupRealSdk();
    }

    @Test
    void verifySdkIsReal() {
        // This should NOT be a Noop
        assertThat(otel.toString()).isNotEqualTo("DefaultOpenTelemetry");
    }

    @Test
    void testExtractContextIsValid() {
        Span parentSpan = otel.getTracer("test").spanBuilder("root").startSpan();
        SpanContext expectedContext = parentSpan.getSpanContext();

        String traceParent =
                String.format(
                        "00-%s-%s-01", expectedContext.getTraceId(), expectedContext.getSpanId());

        // 3. Act: Use the utility to extract the context
        Context extractedContext = OTelUtils.extractContext(otel, traceParent);
        SpanContext actualContext = Span.fromContext(extractedContext).getSpanContext();

        // 4. Assert: The IDs must match exactly
        assertThat(expectedContext.getTraceId()).isEqualTo(actualContext.getTraceId());
        assertThat(expectedContext.getSpanId()).isEqualTo(actualContext.getSpanId());
        parentSpan.end();
    }

    private static OpenTelemetry setupRealSdk() {
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder().build();

        return OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .build();
    }
}
