/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import ch.gryphus.chainvault.config.TraceIdFilter;
import ch.gryphus.chainvault.model.dto.Migration;
import ch.gryphus.chainvault.model.dto.MigrationDetail;
import ch.gryphus.chainvault.model.dto.MigrationStats;
import ch.gryphus.chainvault.model.entity.MigrationEvent;
import ch.gryphus.chainvault.workflow.service.AuditEventService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

/**
 * The type Migration controller test.
 */
@WebMvcTest(MigrationController.class)
class MigrationControllerTest {

    @Autowired private MockMvcTester mockMvcTester;

    @MockitoBean private AuditEventService mockAuditEventService;
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

    /**
     * Test get migrations.
     */
    @Test
    void testGetMigrations() {
        // Setup
        // Configure AuditEventService.getMigrations(...).
        final Migration migration = new Migration();
        migration.setId("TEST-123");
        migration.setDocId("DOC-TEST-1234");
        migration.setTitle("Test Title");
        migration.setStatus("SUCCESS");
        migration.setCreatedAt(Instant.now());
        final List<Migration> migrations = List.of(migration);
        when(mockAuditEventService.getMigrations(0)).thenReturn(migrations);

        // Run the test and verify the results
        var result =
                mockMvcTester.perform(
                        get("/api/migrations")
                                .param("limit", "0")
                                .accept(MediaType.APPLICATION_JSON));

        String expectedResult =
                """
                [{"docId":"DOC-TEST-1234", "failureReason":null, "id":"TEST-123",
                "ocrAttempted":null, "ocrPageCount":null, "ocrSuccess":null, "ocrTotalTextLength":null,
                "processInstanceKey":null, "status":"SUCCESS", "title":"Test Title", "traceId":null, "updatedAt":null}]
                """;
        assertThat(result)
                .hasStatus(HttpStatus.OK)
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .isLenientlyEqualTo(expectedResult);
    }

    /**
     * Test get migrations audit event service returns no items.
     */
    @Test
    void testGetMigrations_AuditEventServiceReturnsNoItems() {
        // Setup
        when(mockAuditEventService.getMigrations(0)).thenReturn(Collections.emptyList());

        // Run the test and verify the results
        var result =
                mockMvcTester.perform(get("/api/migrations").accept(MediaType.APPLICATION_JSON));

        assertThat(result)
                .hasStatus(HttpStatus.OK)
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .isStrictlyEqualTo("[]");
    }

    /**
     * Test get stats.
     */
    @Test
    void testGetStats() {
        // Setup
        // Configure AuditEventService.getStats(...).
        final MigrationStats migrationStats = new MigrationStats();
        migrationStats.setTotal(10L);
        migrationStats.setPending(0);
        migrationStats.setRunning(5);
        migrationStats.setSuccess(5);
        migrationStats.setFailed(0);
        when(mockAuditEventService.getStats()).thenReturn(migrationStats);

        // Run the test and verify the results
        var result =
                mockMvcTester.perform(
                        get("/api/migrations/stats").accept(MediaType.APPLICATION_JSON));
        String expectedResult =
                "{\"failed\":0,\"last24h\":0,\"pending\":0,\"running\":5,\"success\":5,\"total\":10}";
        assertThat(result).hasStatus(HttpStatus.OK).hasBodyTextEqualTo(expectedResult);
    }

    /**
     * Test get detail.
     */
    @Test
    void testGetDetail() {
        // Setup
        // Configure AuditEventService.getDetail(...).
        final MigrationDetail detail = new MigrationDetail();
        detail.setEvents(List.of(MigrationEvent.builder().build()));
        detail.setOcrTextPreview("ocrTextPreview");
        detail.setChainZipUrl("chainZipUrl");
        detail.setPdfUrl("pdfUrl");
        when(mockAuditEventService.getDetail("id")).thenReturn(detail);

        // Run the test and verify the results
        var result =
                mockMvcTester.perform(
                        get("/api/migrations/{id}/detail", "id")
                                .accept(MediaType.APPLICATION_JSON));

        assertThat(result)
                .hasStatus(HttpStatus.OK)
                .hasContentType(MediaType.APPLICATION_JSON)
                .bodyJson()
                .hasPath("events");
    }
}
