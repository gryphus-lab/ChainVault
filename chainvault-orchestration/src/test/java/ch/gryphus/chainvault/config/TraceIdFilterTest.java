/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.config;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.when;

import io.micrometer.tracing.CurrentTraceContext;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * The type Trace id filter test.
 */
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class TraceIdFilterTest {

    @Mock private Tracer mockTracer;
    @Mock private CurrentTraceContext mockCurrentTraceContext;
    @Mock private TraceContext mockTraceContext;
    @Mock private FilterChain mockFilterChain;

    private TraceIdFilter traceIdFilterUnderTest;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        traceIdFilterUnderTest = new TraceIdFilter(mockTracer);
        when(mockTracer.currentTraceContext()).thenReturn(mockCurrentTraceContext);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    /**
     * Test do filter internal when trace context exists.
     */
    @Test
    void testDoFilterInternal_whenTraceContextExists() {
        // Setup
        when(mockCurrentTraceContext.context()).thenReturn(mockTraceContext);
        when(mockTraceContext.traceId()).thenReturn("traceId");

        // Verify
        assertThatNoException()
                .isThrownBy(
                        () ->
                                traceIdFilterUnderTest.doFilterInternal(
                                        request, response, mockFilterChain));
    }

    /**
     * Test do filter internal when trace context is null.
     */
    @Test
    void testDoFilterInternal_whenTraceContextIsNull() {
        // Setup
        when(mockCurrentTraceContext.context()).thenReturn(null);

        // Verify
        assertThatNoException()
                .isThrownBy(
                        () ->
                                traceIdFilterUnderTest.doFilterInternal(
                                        request, response, mockFilterChain));
    }

    /**
     * Test do filter internal throws null pointer exception when request is null.
     */
    @Test
    void testDoFilterInternalThrowsNullPointerException_whenRequestIsNull() {
        // Setup
        when(mockCurrentTraceContext.context()).thenReturn(mockTraceContext);
        when(mockTraceContext.traceId()).thenReturn("traceId");

        // Run the test
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(
                        () ->
                                traceIdFilterUnderTest.doFilterInternal(
                                        null, response, mockFilterChain));
    }

    /**
     * Test do filter internal throws null pointer exception when response is null.
     */
    @Test
    void testDoFilterInternalThrowsNullPointerException_whenResponseIsNull() {
        // Setup
        when(mockCurrentTraceContext.context()).thenReturn(mockTraceContext);
        when(mockTraceContext.traceId()).thenReturn("traceId");

        // Run the test
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(
                        () ->
                                traceIdFilterUnderTest.doFilterInternal(
                                        request, null, mockFilterChain));
    }

    /**
     * Test do filter internal throws null pointer exception when filter chain is null.
     */
    @Test
    void testDoFilterInternalThrowsNullPointerException_whenFilterChainIsNull() {
        // Setup
        when(mockCurrentTraceContext.context()).thenReturn(mockTraceContext);
        when(mockTraceContext.traceId()).thenReturn("traceId");

        // Run the test
        assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> traceIdFilterUnderTest.doFilterInternal(request, response, null));
    }
}
