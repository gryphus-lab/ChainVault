/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import ch.gryphus.chainvault.config.TraceIdFilter;
import ch.gryphus.chainvault.service.SseEmitterService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * The type Migration sse controller test.
 */
@WebMvcTest(MigrationSseController.class)
class MigrationSseControllerTest {

    @Autowired private MockMvcTester mockMvcTester;
    @MockitoBean private TraceIdFilter traceIdFilter;
    @MockitoBean private SseEmitterService mockSseEmitterService;

    /**
     * Sets .
     *
     * @throws ServletException the servlet exception
     * @throws IOException      the io exception
     */
    @BeforeEach
    void setup() throws ServletException, IOException {
        doAnswer(
                        invocation -> {
                            HttpServletRequest request = invocation.getArgument(0);
                            HttpServletResponse response = invocation.getArgument(1);
                            FilterChain chain = invocation.getArgument(2);
                            chain.doFilter(request, response); // Manually trigger the next step
                            return null;
                        })
                .when(traceIdFilter)
                .doFilter(any(), any(), any());
    }

    /**
     * Should return sse stream with provided client id.
     */
    @Test
    void shouldReturnSseStreamWithProvidedClientId() {
        // given
        String clientId = "test-client";
        SseEmitter emitter = new SseEmitter(0L);

        when(mockSseEmitterService.createEmitter(clientId)).thenReturn(emitter);

        // when
        var result =
                mockMvcTester.perform(get("/api/migrations/events").param("clientId", clientId));

        // then
        assertThat(result).hasStatus(HttpStatus.OK).hasContentType(MediaType.TEXT_EVENT_STREAM);

        verify(mockSseEmitterService).createEmitter(clientId);
    }

    /**
     * Should generate client id when not provided.
     */
    @Test
    void shouldGenerateClientIdWhenNotProvided() {
        // given
        SseEmitter emitter = new SseEmitter(0L);

        when(mockSseEmitterService.createEmitter(anyString())).thenReturn(emitter);

        // when
        var result = mockMvcTester.perform(get("/api/migrations/events"));

        // then
        assertThat(result).hasStatus(HttpStatus.OK).hasContentType(MediaType.TEXT_EVENT_STREAM);

        verify(mockSseEmitterService).createEmitter(anyString());
    }
}
