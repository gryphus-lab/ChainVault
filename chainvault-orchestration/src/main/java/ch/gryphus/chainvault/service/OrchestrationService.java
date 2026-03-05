/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.service;

import ch.gryphus.chainvault.entity.MigrationAudit;
import ch.gryphus.chainvault.repository.MigrationAuditRepository;
import io.opentelemetry.api.trace.Span;
import java.time.Instant;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The type Orchestration service.
 */
@Slf4j
@Service
public class OrchestrationService {
    private final RuntimeService runtimeService;
    private final MigrationAuditRepository auditRepo;

    /**
     * Instantiates a new Orchestration service.
     *
     * @param runtimeService the runtime service
     * @param auditRepo      the audit repo
     */
    @Autowired
    public OrchestrationService(RuntimeService runtimeService, MigrationAuditRepository auditRepo) {
        this.runtimeService = runtimeService;
        this.auditRepo = auditRepo;
    }

    /**
     * Start process string.
     *
     * @param variables the variables
     * @return the string
     */
    @Transactional
    public String startProcess(Map<String, Object> variables) {
        ProcessInstance processInstance =
                runtimeService.startProcessInstanceByKey("chainvault", variables);

        String processInstanceId = processInstance.getProcessInstanceId();

        // Create audit record
        MigrationAudit audit = new MigrationAudit();
        audit.setProcessInstanceKey(processInstanceId);
        audit.setProcessDefinitionKey(processInstance.getProcessDefinitionKey());
        audit.setBpmnProcessId("chainvault");
        audit.setDocumentId((String) variables.get("docId"));
        audit.setStatus(MigrationAudit.MigrationStatus.RUNNING);
        audit.setStartedAt(Instant.now());

        String traceId = Span.current().getSpanContext().getTraceId();
        audit.setTraceId(traceId);

        auditRepo.save(audit);

        return processInstanceId;
    }
}
