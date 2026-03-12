/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.entity.MigrationAudit;
import ch.gryphus.chainvault.entity.MigrationEvent;
import ch.gryphus.chainvault.repository.MigrationAuditRepository;
import ch.gryphus.chainvault.repository.MigrationEventRepository;
import io.opentelemetry.api.trace.Span;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import org.flowable.engine.delegate.BpmnError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The type Audit event service test.
 */
@ExtendWith(MockitoExtension.class)
class AuditEventServiceTest {

    @Mock private MigrationAuditRepository mockAuditRepo;
    @Mock private MigrationEventRepository mockEventRepo;

    private AuditEventService auditEventServiceUnderTest;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        auditEventServiceUnderTest = new AuditEventService(mockAuditRepo, mockEventRepo);
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
        MigrationContext ctx = new MigrationContext();
        ctx.setPayloadHash("payloadHash");
        ctx.setPdfHash("pdfHash");
        Map<String, Object> varMap =
                Map.of(
                        "outputFileKey",
                        "pathToOutputFile",
                        "chainOfCustodyZip",
                        "pathToChainOfCustodyZipFile",
                        "ctx",
                        ctx);

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
     * Test handle exception migration audit repository find by process instance key returns absent.
     */
    @Test
    void testHandleException_MigrationAuditRepositoryFindByProcessInstanceKeyReturnsAbsent() {
        // Setup
        Span span = Span.current();
        when(mockAuditRepo.findByProcessInstanceKey("piKey")).thenReturn(Optional.empty());

        // Run the test
        assertThatIllegalStateException()
                .isThrownBy(
                        () ->
                                auditEventServiceUnderTest.handleException(
                                        new Exception("errorMsg"),
                                        span,
                                        "piKey",
                                        "errorCode",
                                        "eventTaskType"));
    }
}
