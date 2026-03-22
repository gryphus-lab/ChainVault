/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.workflow.delegate;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The type O tel utils test.
 */
class OTelUtilsTest {

    private OpenTelemetry otel;
    private Span parentSpan;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        otel = setupRealSdk();
        parentSpan = otel.getTracer("test").spanBuilder("root").startSpan();
    }

    /**
     * Tear down.
     */
    @AfterEach
    void tearDown() {
        parentSpan.end();
    }

    /**
     * Verify sdk is real.
     */
    @Test
    void verifySdkIsReal() {
        // This should NOT be a Noop
        assertThat(otel.toString()).isNotEqualTo("DefaultOpenTelemetry");
    }

    /**
     * Tes when trace parent is valid format.
     */
    @Test
    void tesWhenTraceParentIsValidFormat() {
        SpanContext expectedContext = parentSpan.getSpanContext();

        String traceParent =
                String.format(
                        "00-%s-%s-01", expectedContext.getTraceId(), expectedContext.getSpanId());

        Context extractedContext = OTelUtils.extractContext(otel, traceParent);
        SpanContext actualContext = Span.fromContext(extractedContext).getSpanContext();

        assertThat(actualContext.getTraceId()).isEqualTo(expectedContext.getTraceId());
        assertThat(actualContext.getSpanId()).isEqualTo(expectedContext.getSpanId());
    }

    /**
     * Test with null or empty trace parent.
     */
    @Test
    void testWithNullOrEmptyTraceParent() {
        String traceParent = ""; // empty value
        Context extractedContext = OTelUtils.extractContext(otel, traceParent);
        SpanContext actualContext = Span.fromContext(extractedContext).getSpanContext();

        assertThat(actualContext.getTraceId()).isNotNull();
        assertThat(actualContext.getSpanId()).isNotNull();

        extractedContext = OTelUtils.extractContext(otel, null); // null value
        actualContext = Span.fromContext(extractedContext).getSpanContext();

        assertThat(actualContext.getTraceId()).isNotNull();
        assertThat(actualContext.getSpanId()).isNotNull();
    }

    private static OpenTelemetry setupRealSdk() {
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder().build();

        return OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .build();
    }
}
