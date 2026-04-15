/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.workflow.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.model.dto.Migration;
import ch.gryphus.chainvault.model.dto.MigrationDetail;
import ch.gryphus.chainvault.model.dto.MigrationPage;
import ch.gryphus.chainvault.model.dto.MigrationStats;
import ch.gryphus.chainvault.model.entity.MigrationAudit;
import ch.gryphus.chainvault.model.entity.MigrationEvent;
import ch.gryphus.chainvault.repository.MigrationAuditRepository;
import ch.gryphus.chainvault.repository.MigrationEventRepository;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.*;
import org.flowable.engine.delegate.BpmnError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class AuditEventServiceTest {

    @Mock private MigrationAuditRepository auditRepo;
    @Mock private MigrationEventRepository eventRepo;
    @Mock private Span span;
    @Mock private SpanContext spanContext;

    @InjectMocks private AuditEventService auditEventService;

    private MigrationAudit testAudit;
    private static final String PI_KEY = "proc-123";
    private static final String TRACE_ID = "trace-888";

    @BeforeEach
    void setUp() {
        testAudit = new MigrationAudit();
        testAudit.setId(1L);
        testAudit.setProcessInstanceKey(PI_KEY);
        testAudit.setAttemptCount(0);

        lenient().when(span.getSpanContext()).thenReturn(spanContext);
        lenient().when(spanContext.getTraceId()).thenReturn(TRACE_ID);
    }

    @Test
    @DisplayName("updateAuditEventStart: Should increment attempts and set RUNNING status")
    void updateAuditEventStartSuccess() {
        when(auditRepo.findByProcessInstanceKey(PI_KEY)).thenReturn(Optional.of(testAudit));

        auditEventService.updateAuditEventStart(PI_KEY, "DOC-001", "INIT_TASK", span);

        assertThat(testAudit.getStatus()).isEqualTo(MigrationAudit.MigrationStatus.RUNNING);
        assertThat(testAudit.getAttemptCount()).isEqualTo(1);
        assertThat(testAudit.getDocumentId()).isEqualTo("DOC-001");
        assertThat(testAudit.getTraceId()).isEqualTo(TRACE_ID);

        verify(auditRepo).save(testAudit);
        verify(eventRepo).save(any(MigrationEvent.class));
    }

    @Test
    @DisplayName("updateAuditEventEnd: Should handle SUCCESS with OCR results")
    void updateAuditEventEndSuccessWithOcr() {
        when(auditRepo.findByProcessInstanceKey(PI_KEY)).thenReturn(Optional.of(testAudit));

        Map<String, Object> varMap = new HashMap<>();
        varMap.put("ocrResults", List.of("Page 1 text", "Page 2 text"));
        varMap.put("ocrPageCount", 2);
        varMap.put("outputFileKey", "s3://path/file.pdf");

        auditEventService.updateAuditEventEnd(
                PI_KEY,
                MigrationAudit.MigrationStatus.SUCCESS,
                null,
                null,
                "perform-ocr",
                "Done",
                varMap,
                span);

        assertThat(testAudit.getStatus()).isEqualTo(MigrationAudit.MigrationStatus.SUCCESS);
        assertThat(testAudit.getOcrSuccess()).isTrue();
        assertThat(testAudit.getOcrPageCount()).isEqualTo(2);
        assertThat(testAudit.getOutputFileKey()).isEqualTo("s3://path/file.pdf");
        assertThat(testAudit.getOcrResultReference()).contains("Page 1 text");

        verify(eventRepo)
                .save(
                        argThat(
                                e ->
                                        e.getEventType()
                                                == MigrationEvent.MigrationEventType
                                                        .TASK_COMPLETED));
    }

    @Test
    @DisplayName("updateAuditEventEnd: Should handle FAILED status and OCR specific failures")
    void updateAuditEventEndFailure() {
        when(auditRepo.findByProcessInstanceKey(PI_KEY)).thenReturn(Optional.of(testAudit));

        auditEventService.updateAuditEventEnd(
                PI_KEY,
                MigrationAudit.MigrationStatus.FAILED,
                "ERR_001",
                "Stacktrace",
                "perform-ocr",
                "Failed task",
                Collections.emptyMap(),
                span);

        assertThat(testAudit.getStatus()).isEqualTo(MigrationAudit.MigrationStatus.FAILED);
        assertThat(testAudit.getErrorCode()).isEqualTo("ERR_001");
        assertThat(testAudit.getOcrErrorCode()).isEqualTo("OCR_TESSERACT_ERROR");

        verify(eventRepo)
                .save(
                        argThat(
                                e ->
                                        e.getEventType()
                                                == MigrationEvent.MigrationEventType.TASK_FAILED));
    }

    @Test
    @DisplayName("handleException: Should record exception on span and throw BpmnError")
    void handleExceptionFlow() {
        when(auditRepo.findByProcessInstanceKey(PI_KEY)).thenReturn(Optional.of(testAudit));
        Exception ex = new RuntimeException("DB Timeout");

        assertThatThrownBy(
                        () ->
                                auditEventService.handleException(
                                        ex, span, PI_KEY, "BPMN_ERR_500", "task-a"))
                .isInstanceOf(BpmnError.class);

        verify(span).recordException(ex);
        verify(auditRepo).save(testAudit);
    }

    @Test
    @DisplayName("updateAuditDetails: Should apply MigrationContext hashes")
    void updateAuditDetailsContextHashes() {
        when(auditRepo.findByProcessInstanceKey(PI_KEY)).thenReturn(Optional.of(testAudit));
        MigrationContext context = new MigrationContext();
        context.setPayloadHash("hash123");
        context.setPdfHash("pdf456");

        auditEventService.updateAuditEventEnd(
                PI_KEY,
                MigrationAudit.MigrationStatus.SUCCESS,
                null,
                null,
                "any",
                "msg",
                Map.of("migrationContext", context),
                span);

        assertThat(testAudit.getInputPayloadHash()).isEqualTo("hash123");
        assertThat(testAudit.getMergedPdfHash()).isEqualTo("pdf456");
    }

    @Test
    @DisplayName("getMigrations: Should sort correctly with valid and invalid keys")
    void getMigrationsSorting() {
        when(auditRepo.getAllByCompletedAtIsNotNull(any(Pageable.class)))
                .thenReturn(Collections.emptyList());

        // Test valid sort
        auditEventService.getMigrations(10, 0, "documentId", "asc");
        // Test invalid sort (should fallback to default)
        auditEventService.getMigrations(10, 0, "invalid_key", "desc");

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(auditRepo, times(2)).getAllByCompletedAtIsNotNull(captor.capture());

        assertThat(captor.getAllValues().get(0).getSort().getOrderFor("documentId")).isNotNull();
        assertThat(captor.getAllValues().get(1).getSort().getOrderFor("createdAt")).isNotNull();
    }

    @Test
    @DisplayName("getMigrations: Should return a fully populated MigrationPage with mapped DTOs")
    void getMigrationsFullPageMapping() {
        // Arrange
        int limit = 2;
        int page = 0;
        Instant now = Instant.now();

        MigrationAudit audit1 = new MigrationAudit();
        audit1.setId(101L);
        audit1.setProcessInstanceKey("PI-101");
        audit1.setDocumentId("DOC-101");
        audit1.setStatus(MigrationAudit.MigrationStatus.SUCCESS);
        audit1.setCreatedAt(now.minusSeconds(100));
        audit1.setLastUpdatedAt(now);
        audit1.setTraceId("TRACE-101");
        audit1.setOcrPageCount(5);
        audit1.setOcrAttempted(true);
        audit1.setOcrSuccess(true);
        audit1.setOcrTotalTextLength(1500L);

        MigrationAudit audit2 = new MigrationAudit();
        audit2.setId(102L);
        audit2.setStatus(MigrationAudit.MigrationStatus.FAILED);

        List<MigrationAudit> mockRecords = List.of(audit1, audit2);

        when(auditRepo.getAllByCompletedAtIsNotNull(any(Pageable.class))).thenReturn(mockRecords);
        when(auditRepo.countByCompletedAtIsNotNull()).thenReturn(50L);

        // Act
        MigrationPage result = auditEventService.getMigrations(limit, page, "createdAt", "desc");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotal()).isEqualTo(50L);
        assertThat(result.getItems()).hasSize(2);

        // Verify detailed mapping for the first item
        Migration firstDto = result.getItems().getFirst();
        assertThat(firstDto.getId()).isEqualTo("101");
        assertThat(firstDto.getProcessInstanceKey()).isEqualTo("PI-101");
        assertThat(firstDto.getDocId()).isEqualTo("DOC-101");
        assertThat(firstDto.getStatus()).isEqualTo("SUCCESS");
        assertThat(firstDto.getCreatedAt()).isEqualTo(audit1.getCreatedAt());
        assertThat(firstDto.getTraceId()).isEqualTo("TRACE-101");
        assertThat(firstDto.getOcrPageCount()).isEqualTo(5);
        assertThat(firstDto.getOcrAttempted()).isTrue();
        assertThat(firstDto.getOcrSuccess()).isTrue();
        assertThat(firstDto.getOcrTotalTextLength()).isEqualTo(1500L);

        // Verify mapping for second item (minimal data)
        Migration secondDto = result.getItems().get(1);
        assertThat(secondDto.getId()).isEqualTo("102");
        assertThat(secondDto.getStatus()).isEqualTo("FAILED");

        // Verify Pageable parameters
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(auditRepo).getAllByCompletedAtIsNotNull(pageableCaptor.capture());
        Pageable capturedPageable = pageableCaptor.getValue();

        assertThat(capturedPageable.getPageNumber()).isZero();
        assertThat(capturedPageable.getPageSize()).isEqualTo(2);
        assertThat(
                        Objects.requireNonNull(capturedPageable.getSort().getOrderFor("createdAt"))
                                .getDirection())
                .isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("getStats: Should aggregate counts from repository")
    void getStatsAggregation() {
        when(auditRepo.count()).thenReturn(10L);
        when(auditRepo.countAllByStatus(MigrationAudit.MigrationStatus.SUCCESS)).thenReturn(5);

        MigrationStats stats = auditEventService.getStats();

        assertThat(stats.getTotal()).isEqualTo(10L);
        assertThat(stats.getSuccess()).isEqualTo(5L);
        verify(auditRepo, times(4)).countAllByStatus(any());
    }

    @Test
    @DisplayName("getDetail: Should throw EntityNotFoundException if ID missing")
    void getDetailNotFound() {
        when(auditRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditEventService.getDetail("99"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("getDetail: Should map all fields correctly")
    void getDetailMapping() {
        testAudit.setStatus(MigrationAudit.MigrationStatus.SUCCESS);
        testAudit.setOcrResultReference("Preview");
        when(auditRepo.findById(1L)).thenReturn(Optional.of(testAudit));
        when(eventRepo.getAllByMigrationAuditId(1L)).thenReturn(Collections.emptyList());

        MigrationDetail detail = auditEventService.getDetail("1");

        assertThat(detail.getId()).isEqualTo("1");
        assertThat(detail.getStatus()).isEqualTo("SUCCESS");
        assertThat(detail.getOcrTextPreview()).isEqualTo("Preview");
    }

    @Test
    @DisplayName("findAudit: Should throw IllegalStateException if PI Key not found")
    void findAuditThrowsException() {
        when(auditRepo.findByProcessInstanceKey("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> auditEventService.updateAuditEventStart("unknown", "d", "t", span))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No audit found");
    }
}
