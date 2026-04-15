/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import ch.gryphus.chainvault.config.TraceIdFilter;
import ch.gryphus.chainvault.model.dto.MigrationDetail;
import ch.gryphus.chainvault.model.dto.MigrationPage;
import ch.gryphus.chainvault.model.dto.MigrationStats;
import ch.gryphus.chainvault.model.entity.MigrationEvent;
import ch.gryphus.chainvault.workflow.service.AuditEventService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.json.JsonMapper;

@MockitoSettings(strictness = Strictness.LENIENT)
@WebMvcTest(MigrationController.class)
class MigrationControllerTest {

    @Autowired private MockMvcTester mvc;

    @MockitoBean private AuditEventService auditEventService;
    @MockitoBean private JsonMapper jsonMapper;
    @MockitoBean private TraceIdFilter traceIdFilter;

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

    @Test
    void getMigrations_ShouldReturnOk_WhenValid() {
        // Given
        String mockResponse = "{\"data\": []}";
        when(auditEventService.getMigrations(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(new MigrationPage(Collections.emptyList(), 1));
        when(jsonMapper.writeValueAsString(any())).thenReturn(mockResponse);

        // When/Then
        assertThat(mvc.get().uri("/api/migrations").param("limit", "10").param("page", "0"))
                .hasStatusOk()
                .hasBodyTextEqualTo(mockResponse);
    }

    @Test
    void getMigrations_ShouldReturnBadRequest_OnInvalidLimit() {
        // Given
        String errorResponse = "{\"error\": \"limit must be greater than 0\"}";
        when(jsonMapper.writeValueAsString(any())).thenReturn(errorResponse);

        // When/Then
        assertThat(mvc.get().uri("/api/migrations").param("limit", "0"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasBodyTextEqualTo(errorResponse);
    }

    @Test
    void getMigrations_ShouldReturnBadRequest_OnInvalidPage() {
        // Given
        String errorResponse = "{\"error\": \"page must be greater than or equal to 0\"}";
        when(jsonMapper.writeValueAsString(any())).thenReturn(errorResponse);

        // When/Then
        assertThat(mvc.get().uri("/api/migrations").param("page", "-1"))
                .hasStatus(HttpStatus.BAD_REQUEST)
                .hasBodyTextEqualTo(errorResponse);
    }

    @Test
    void getStats_ShouldReturnStats() {
        // Given
        String statsJson = "{\"total\": 5}";
        MigrationStats migrationStats = new MigrationStats();
        migrationStats.setTotal(5);
        when(auditEventService.getStats()).thenReturn(migrationStats);
        when(jsonMapper.writeValueAsString(any())).thenReturn(statsJson);

        // When/Then
        assertThat(mvc.get().uri("/api/migrations/stats"))
                .hasStatusOk()
                .hasBodyTextEqualTo(statsJson);
    }

    @Test
    void getDetail_ShouldReturnSuccess() {
        // Given
        String detailJson = "{\"id\": \"123\"}";
        final MigrationDetail detail = new MigrationDetail();
        detail.setEvents(List.of(MigrationEvent.builder().build()));
        detail.setId("123");
        detail.setOcrTextPreview("ocrTextPreview");
        detail.setChainZipUrl("chainZipUrl");
        detail.setPdfUrl("pdfUrl");
        when(auditEventService.getDetail(anyString())).thenReturn(detail);
        when(jsonMapper.writeValueAsString(any())).thenReturn(detailJson);

        // When/Then
        assertThat(mvc.get().uri("/api/migrations/{id}/detail", "123"))
                .hasStatusOk()
                .hasBodyTextEqualTo(detailJson);
    }
}
