/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.config;

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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * The type Trace id filter test.
 */
@ExtendWith(MockitoExtension.class)
class TraceIdFilterTest {

    @Mock private Tracer mockTracer;
    @Mock private CurrentTraceContext mockCurrentTraceContext;
    @Mock private TraceContext mockTraceContext;
    @Mock private FilterChain mockFilterChain;

    private TraceIdFilter traceIdFilterUnderTest;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        traceIdFilterUnderTest = new TraceIdFilter(mockTracer);
        when(mockTracer.currentTraceContext()).thenReturn(mockCurrentTraceContext);
    }

    /**
     * Test do filter internal when trace context exists.
     */
    @Test
    void testDoFilterInternal_whenTraceContextExists() {
        // Setup
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
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
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(mockCurrentTraceContext.context()).thenReturn(null);

        // Verify
        assertThatNoException()
                .isThrownBy(
                        () ->
                                traceIdFilterUnderTest.doFilterInternal(
                                        request, response, mockFilterChain));
    }
}
