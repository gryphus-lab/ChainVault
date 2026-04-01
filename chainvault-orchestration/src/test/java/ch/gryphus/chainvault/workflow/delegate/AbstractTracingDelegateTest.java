/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.workflow.delegate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import ch.gryphus.chainvault.repository.MigrationAuditRepository;
import ch.gryphus.chainvault.service.SseEmitterService;
import ch.gryphus.chainvault.workflow.service.AuditEventService;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import java.util.List;
import java.util.Map;
import org.flowable.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * The type Abstract tracing delegate test.
 */
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class AbstractTracingDelegateTest {

    @Mock private OpenTelemetry mockOpenTelemetry;
    @Mock private ContextPropagators mockContextPropagators;
    @Mock private TextMapPropagator mockTextMapPropagator;
    @Mock private Context mockContext;
    @Mock private AuditEventService mockAuditService;
    @Mock private SseEmitterService mockSseEmitterService;
    @Mock private DelegateExecution mockExecution;
    @Mock private Tracer mockTracer;
    @Mock private SpanBuilder mockSpanBuilder;
    @Mock private SpanContext mockSpanContext;
    @Mock private Span mockSpan;
    @Mock private MigrationAuditRepository mockAuditRepo;

    private AbstractTracingDelegate abstractTracingDelegateUnderTest;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        abstractTracingDelegateUnderTest =
                new AbstractTracingDelegate(
                        mockOpenTelemetry,
                        mockAuditService,
                        mockSseEmitterService,
                        "taskType",
                        "errorCode") {
                    @Override
                    protected void doExecute(DelegateExecution execution, Span span, String docId) {
                        //
                    }
                };

        when(mockOpenTelemetry.getTracer(anyString())).thenReturn(mockTracer);
        when(mockOpenTelemetry.getPropagators()).thenReturn(mockContextPropagators);
        when(mockContextPropagators.getTextMapPropagator()).thenReturn(mockTextMapPropagator);
        when(mockTextMapPropagator.extract(any(), any(), any())).thenReturn(mockContext);
        when(mockTracer.spanBuilder(anyString())).thenReturn(mockSpanBuilder);
        when(mockSpanBuilder.setParent(any())).thenReturn(mockSpanBuilder);
        when(mockSpanBuilder.startSpan()).thenReturn(mockSpan);
        when(mockSpan.getSpanContext()).thenReturn(mockSpanContext);
        when(mockSpanContext.getTraceId()).thenReturn("traceId");
    }

    /**
     * Test execute does not throw exception for valid execution variables.
     */
    @Test
    void testExecuteDoesNotThrowExceptionForValidExecutionVariables() {
        // Setup
        when(mockExecution.getVariable(anyString())).thenReturn("test");

        // Run and verify
        assertThatNoException()
                .isThrownBy(() -> abstractTracingDelegateUnderTest.execute(mockExecution));
    }

    /**
     * Test execute does not throw exception for null execution variables.
     */
    @Test
    void testExecuteDoesNotThrowExceptionForNullExecutionVariables() {
        // Setup
        when(mockExecution.getVariable(anyString())).thenReturn(null);

        // Run and verify
        assertThatNoException()
                .isThrownBy(() -> abstractTracingDelegateUnderTest.execute(mockExecution));
    }

    /**
     * Test execute throws exception.
     */
    @Test
    void testExecuteThrowsException() {
        // Setup
        when(mockExecution.getVariable(anyString())).thenReturn(Map.of("key", "value"));

        // Run and verify
        assertThatException()
                .isThrownBy(() -> abstractTracingDelegateUnderTest.execute(mockExecution));
    }

    /**
     * Test get transient variable safely returns expected value.
     */
    @Test
    void testGetTransientVariableSafelyReturnsExpectedValue() {
        // Setup
        when(mockExecution.getTransientVariable(anyString())).thenReturn("testValue");
        // Run the test
        String result =
                AbstractTracingDelegate.getTransientVariableSafely(
                        mockExecution, "testVariable", String.class);

        // Verify the results
        assertThat(result).isEqualTo("testValue");
    }

    /**
     * Test get transient variable safely does not throw exception for null value.
     */
    @Test
    void testGetTransientVariableSafelyDoesNotThrowExceptionForNullValue() {
        // Setup
        when(mockExecution.getTransientVariable(anyString())).thenReturn(null);
        // Run the test
        List<?> result =
                AbstractTracingDelegate.getTransientVariableSafely(
                        mockExecution, "testVariable", List.class);

        // Verify the results
        assertThat(result).isNull();
    }

    /**
     * Test get transient variable safely throws exception on class mismatch.
     */
    @Test
    void testGetTransientVariableSafelyThrowsExceptionOnClassMismatch() {
        // Setup
        when(mockExecution.getTransientVariable(anyString())).thenReturn(Map.of("key", "value"));
        // Run the test
        assertThatException()
                .isThrownBy(
                        () ->
                                AbstractTracingDelegate.getTransientVariableSafely(
                                        mockExecution, "testVariable", String.class))
                .isExactlyInstanceOf(IllegalArgumentException.class);
    }
}
