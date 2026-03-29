/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.workflow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.Assertions.assertThatRuntimeException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.model.entity.*;
import ch.gryphus.chainvault.repository.MigrationAuditRepository;
import ch.gryphus.chainvault.repository.MigrationEventRepository;
import io.opentelemetry.api.trace.Span;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.flowable.engine.delegate.BpmnError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Limit;

/**
 * The type Audit event service test.
 */
@ExtendWith(MockitoExtension.class)
class AuditEventServiceTest {

    @Mock private MigrationAuditRepository mockAuditRepo;
    @Mock private MigrationEventRepository mockEventRepo;

    private AuditEventService auditEventServiceUnderTest;
    private MigrationContext migrationContext;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        auditEventServiceUnderTest = new AuditEventService(mockAuditRepo, mockEventRepo);
        migrationContext = new MigrationContext();
    }

    /**
     * Test update audit event start.
     */
    @Test
    void testUpdateAuditEventStart() {
        // Setup
        Span span = Span.current();

        // Configure MigrationAuditRepository.findByProcessInstanceKey(...).
        Optional<MigrationAudit> migrationAudit =
                Optional.of(
                        MigrationAudit.builder()
                                .id(0L)
                                .processInstanceKey("processInstanceId")
                                .documentId("docId")
                                .status(MigrationAudit.MigrationStatus.PENDING)
                                .failureReason("errorMsg")
                                .errorCode("errorCode")
                                .attemptCount(0)
                                .startedAt(
                                        LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0)
                                                .toInstant(ZoneOffset.UTC))
                                .completedAt(
                                        LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0)
                                                .toInstant(ZoneOffset.UTC))
                                .inputPayloadHash("inputPayloadHash")
                                .outputFileKey("outputFileKey")
                                .chainOfCustodyZip("chainOfCustodyZip")
                                .mergedPdfHash("mergedPdfHash")
                                .traceId("traceId")
                                .build());
        when(mockAuditRepo.findByProcessInstanceKey("processInstanceId"))
                .thenReturn(migrationAudit);

        // Run the test
        auditEventServiceUnderTest.updateAuditEventStart(
                "processInstanceId", "docId", "eventTaskType", span);

        // Verify the results
        verify(mockAuditRepo).save(any(MigrationAudit.class));
        verify(mockEventRepo).save(any(MigrationEvent.class));
    }

    /**
     * Test update audit event start migration audit repository find by process instance key returns absent.
     */
    @Test
    void testUpdateAuditEventStart_MigrationAuditRepositoryFindByProcessInstanceKeyReturnsAbsent() {
        // Setup
        Span span = Span.current();
        when(mockAuditRepo.findByProcessInstanceKey("processInstanceId"))
                .thenReturn(Optional.empty());

        // Run the test
        assertThatThrownBy(
                        () ->
                                auditEventServiceUnderTest.updateAuditEventStart(
                                        "processInstanceId", "docId", "eventTaskType", span))
                .isInstanceOf(IllegalStateException.class);
    }

    /**
     * Test update audit event end.
     */
    @Test
    void testUpdateAuditEventEnd() {
        // Setup
        migrationContext.setPayloadHash("payloadHash");
        migrationContext.setPdfHash("pdfHash");
        Map<String, Object> varMap =
                Map.of(
                        "outputFileKey",
                        "pathToOutputFile",
                        "chainOfCustodyZip",
                        "pathToChainOfCustodyZipFile",
                        "migrationContext",
                        migrationContext);

        // Configure MigrationAuditRepository.findByProcessInstanceKey(...).
        Optional<MigrationAudit> migrationAudit =
                Optional.of(
                        MigrationAudit.builder()
                                .id(0L)
                                .processInstanceKey("processInstanceId")
                                .documentId("docId")
                                .status(MigrationAudit.MigrationStatus.PENDING)
                                .failureReason("errorMsg")
                                .errorCode("errorCode")
                                .attemptCount(0)
                                .startedAt(
                                        LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0)
                                                .toInstant(ZoneOffset.UTC))
                                .completedAt(
                                        LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0)
                                                .toInstant(ZoneOffset.UTC))
                                .inputPayloadHash("inputPayloadHash")
                                .outputFileKey("outputFileKey")
                                .chainOfCustodyZip("chainOfCustodyZip")
                                .mergedPdfHash("mergedPdfHash")
                                .traceId("traceId")
                                .build());
        when(mockAuditRepo.findByProcessInstanceKey("piKey")).thenReturn(migrationAudit);

        // Run the test
        auditEventServiceUnderTest.updateAuditEventEnd(
                "piKey",
                MigrationAudit.MigrationStatus.PENDING,
                "errorCode",
                null,
                "eventTaskType",
                "eventMsg",
                varMap);

        // Verify the results
        verify(mockAuditRepo).save(any(MigrationAudit.class));
        verify(mockEventRepo).save(any(MigrationEvent.class));
    }

    /**
     * Test update audit event end migration audit repository find by process instance key returns absent.
     */
    @Test
    void testUpdateAuditEventEnd_MigrationAuditRepositoryFindByProcessInstanceKeyReturnsAbsent() {
        // Setup
        Map<String, Object> varMap = Map.ofEntries(Map.entry("value", "value"));
        when(mockAuditRepo.findByProcessInstanceKey("piKey")).thenReturn(Optional.empty());

        // Run the test
        assertThatThrownBy(
                        () ->
                                auditEventServiceUnderTest.updateAuditEventEnd(
                                        "piKey",
                                        MigrationAudit.MigrationStatus.PENDING,
                                        "errorCode",
                                        "errorMsg",
                                        "eventTaskType",
                                        "eventMsg",
                                        varMap))
                .isInstanceOf(IllegalStateException.class);
    }

    /**
     * Test handle exception throws bpmn error.
     */
    @Test
    void testHandleException_ThrowsBpmnError() {
        // Setup
        Span span = Span.current();

        // Configure MigrationAuditRepository.findByProcessInstanceKey(...).
        Optional<MigrationAudit> migrationAudit =
                Optional.of(
                        MigrationAudit.builder()
                                .id(0L)
                                .processInstanceKey("processInstanceId")
                                .documentId("docId")
                                .status(MigrationAudit.MigrationStatus.PENDING)
                                .failureReason("errorMsg")
                                .errorCode("errorCode")
                                .attemptCount(0)
                                .startedAt(
                                        LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0)
                                                .toInstant(ZoneOffset.UTC))
                                .completedAt(
                                        LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0)
                                                .toInstant(ZoneOffset.UTC))
                                .inputPayloadHash("inputPayloadHash")
                                .outputFileKey("outputFileKey")
                                .chainOfCustodyZip("chainOfCustodyZip")
                                .mergedPdfHash("mergedPdfHash")
                                .traceId("traceId")
                                .build());
        when(mockAuditRepo.findByProcessInstanceKey("piKey")).thenReturn(migrationAudit);

        // Run the test
        assertThatRuntimeException()
                .isThrownBy(
                        () ->
                                auditEventServiceUnderTest.handleException(
                                        new Exception("errorMsg"),
                                        span,
                                        "piKey",
                                        "errorCode",
                                        "eventTaskType"))
                .isInstanceOf(BpmnError.class);
        verify(mockAuditRepo).save(any(MigrationAudit.class));
        verify(mockEventRepo).save(any(MigrationEvent.class));
    }

    /**
     * Test handle exception logs ocr exception details.
     */
    @Test
    void testHandleException_LogsOCRExceptionDetails() {
        // Setup
        Span span = Span.current();

        // Configure MigrationAuditRepository.findByProcessInstanceKey(...).
        Optional<MigrationAudit> migrationAudit =
                Optional.of(
                        MigrationAudit.builder()
                                .id(0L)
                                .processInstanceKey("processInstanceId")
                                .documentId("docId")
                                .status(MigrationAudit.MigrationStatus.PENDING)
                                .failureReason("errorMsg")
                                .errorCode("OCR_FAILED")
                                .attemptCount(0)
                                .startedAt(
                                        LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0)
                                                .toInstant(ZoneOffset.UTC))
                                .completedAt(
                                        LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0)
                                                .toInstant(ZoneOffset.UTC))
                                .inputPayloadHash("inputPayloadHash")
                                .traceId("traceId")
                                .build());
        when(mockAuditRepo.findByProcessInstanceKey("piKey")).thenReturn(migrationAudit);

        // Run the test
        assertThatRuntimeException()
                .isThrownBy(
                        () ->
                                auditEventServiceUnderTest.handleException(
                                        new Exception("errorMsg"),
                                        span,
                                        "piKey",
                                        "errorCode",
                                        "perform-ocr"))
                .isInstanceOf(BpmnError.class);
        verify(mockAuditRepo).save(any(MigrationAudit.class));
        verify(mockEventRepo).save(any(MigrationEvent.class));
    }

    /**
     * Test handle exception migration audit repository find by process instance key returns absent.
     */
    @Test
    void testHandleException_MigrationAuditRepositoryFindByProcessInstanceKeyReturnsAbsent() {
        // Setup
        Span span = Span.current();
        when(mockAuditRepo.findByProcessInstanceKey("piKey")).thenReturn(Optional.empty());

        // Run the test
        assertThatException()
                .isThrownBy(
                        () ->
                                auditEventServiceUnderTest.handleException(
                                        new Exception("errorMsg"),
                                        span,
                                        "piKey",
                                        "errorCode",
                                        "eventTaskType"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testGetMigrations() {
        // Setup
        final Migration migration = new Migration();
        migration.setId("0");
        migration.setDocId("DOC-TEST-123");
        migration.setStatus("PENDING");
        migration.setCreatedAt(LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0).toInstant(ZoneOffset.UTC));
        migration.setTraceId("traceId");
        migration.setOcrAttempted(false);
        migration.setOcrSuccess(false);
        migration.setOcrPageCount(0);
        migration.setOcrTotalTextLength(0L);
        final List<Migration> expectedResult = List.of(migration);

        // Configure MigrationAuditRepository.getAllByCompletedAtIsNotNull(...).
        final List<MigrationAudit> list =
                List.of(
                        MigrationAudit.builder()
                                .id(0L)
                                .processInstanceKey("processInstanceId")
                                .documentId("DOC-TEST-123")
                                .status(MigrationAudit.MigrationStatus.PENDING)
                                .failureReason("errorMsg")
                                .errorCode("errorCode")
                                .attemptCount(0)
                                .createdAt(
                                        LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0)
                                                .toInstant(ZoneOffset.UTC))
                                .startedAt(
                                        LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0)
                                                .toInstant(ZoneOffset.UTC))
                                .completedAt(
                                        LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0)
                                                .toInstant(ZoneOffset.UTC))
                                .inputPayloadHash("inputPayloadHash")
                                .outputFileKey("outputFileKey")
                                .chainOfCustodyZip("chainOfCustodyZip")
                                .mergedPdfHash("mergedPdfHash")
                                .traceId("traceId")
                                .ocrAttempted(false)
                                .ocrPageCount(0)
                                .ocrTotalTextLength(0L)
                                .ocrSuccess(false)
                                .ocrErrorCode("ocrErrorCode")
                                .ocrErrorMessage("errorMsg")
                                .ocrCompletedAt(
                                        LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0)
                                                .toInstant(ZoneOffset.UTC))
                                .build());
        when(mockAuditRepo.getAllByCompletedAtIsNotNull(any(Limit.class))).thenReturn(list);

        // Run the test
        final List<Migration> result = auditEventServiceUnderTest.getMigrations(0);

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetMigrations_MigrationAuditRepositoryReturnsNoItems() {
        // Setup
        when(mockAuditRepo.getAllByCompletedAtIsNotNull(any(Limit.class)))
                .thenReturn(Collections.emptyList());

        // Run the test
        final List<Migration> result = auditEventServiceUnderTest.getMigrations(0);

        // Verify the results
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    @Test
    void testGetStats() {
        // Setup
        final MigrationStats expectedResult = new MigrationStats();
        expectedResult.setTotal(20L);
        expectedResult.setPending(5);
        expectedResult.setRunning(5);
        expectedResult.setSuccess(5);
        expectedResult.setFailed(5);

        when(mockAuditRepo.count()).thenReturn(20L);
        when(mockAuditRepo.countAllByStatus(any())).thenReturn(5);

        // Run the test
        final MigrationStats result = auditEventServiceUnderTest.getStats();

        // Verify the results
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void testGetDetail_ReturnsExpectedResults() {
        // Setup
        final MigrationDetail expectedResult = new MigrationDetail();
        expectedResult.setId("123");
        expectedResult.setDocId("docId");
        expectedResult.setStatus("status");
        expectedResult.setCreatedAt(Instant.now());
        expectedResult.setTraceId("traceId");
        expectedResult.setOcrAttempted(false);
        expectedResult.setOcrSuccess(false);
        expectedResult.setOcrPageCount(0);
        expectedResult.setOcrTotalTextLength(0L);
        expectedResult.setEvents(
                List.of(
                        MigrationEvent.builder()
                                .migrationAuditId(123L)
                                .eventType(MigrationEvent.MigrationEventType.PROCESS_STARTED)
                                .taskType("eventTaskType")
                                .message("eventMsg")
                                .errorCode("errorCode")
                                .errorMessage("errorMsg")
                                .createdAt(Instant.now())
                                .eventData(Map.ofEntries(Map.entry("value", "value")))
                                .traceId("traceId")
                                .build()));
        expectedResult.setChainZipUrl("chainOfCustodyZip");
        expectedResult.setPdfUrl("outputFileKey");

        // Configure MigrationAuditRepository.getReferenceById(...).
        final MigrationAudit migrationAudit =
                MigrationAudit.builder()
                        .id(123L)
                        .processInstanceKey("processInstanceId")
                        .documentId("docId")
                        .status(MigrationAudit.MigrationStatus.PENDING)
                        .failureReason("errorMsg")
                        .errorCode("errorCode")
                        .attemptCount(0)
                        .createdAt(Instant.now())
                        .startedAt(Instant.now())
                        .completedAt(Instant.now())
                        .inputPayloadHash("inputPayloadHash")
                        .outputFileKey("outputFileKey")
                        .chainOfCustodyZip("chainOfCustodyZip")
                        .mergedPdfHash("mergedPdfHash")
                        .traceId("traceId")
                        .ocrAttempted(false)
                        .ocrPageCount(0)
                        .ocrTotalTextLength(0L)
                        .ocrSuccess(false)
                        .ocrErrorCode("ocrErrorCode")
                        .ocrErrorMessage("errorMsg")
                        .ocrCompletedAt(Instant.now())
                        .build();
        when(mockAuditRepo.getReferenceById(any())).thenReturn(migrationAudit);

        // Configure MigrationEventRepository.getAllByMigrationAuditId(...).
        final List<MigrationEvent> migrationEvents =
                List.of(
                        MigrationEvent.builder()
                                .migrationAuditId(123L)
                                .eventType(MigrationEvent.MigrationEventType.PROCESS_STARTED)
                                .taskType("eventTaskType")
                                .message("eventMsg")
                                .errorCode("errorCode")
                                .errorMessage("errorMsg")
                                .createdAt(Instant.now())
                                .eventData(Map.ofEntries(Map.entry("value", "value")))
                                .traceId("traceId")
                                .build());
        when(mockEventRepo.getAllByMigrationAuditId(any())).thenReturn(migrationEvents);

        // Run the test
        final MigrationDetail result = auditEventServiceUnderTest.getDetail("123");

        // Verify the results
        assertThat(result.getEvents()).hasSize(1);
    }

    @Test
    void testGetDetail_MigrationEventRepositoryReturnsNoItems() {
        // Setup
        MigrationAudit mockAudit = mock(MigrationAudit.class);
        when(mockAuditRepo.getReferenceById(any())).thenReturn(mockAudit);
        when(mockEventRepo.getAllByMigrationAuditId(any())).thenReturn(Collections.emptyList());

        // Run the test
        final MigrationDetail result = auditEventServiceUnderTest.getDetail("123");

        // Verify the results
        assertThat(result)
                .hasAllNullFieldsOrPropertiesExcept(
                        "events",
                        "pageCount",
                        "ocrAttempted",
                        "ocrSuccess",
                        "ocrPageCount",
                        "ocrTotalTextLength");
        assertThat(result.getEvents()).isEmpty();
    }
}
