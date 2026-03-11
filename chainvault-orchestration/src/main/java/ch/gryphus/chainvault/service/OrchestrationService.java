/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.service;

import ch.gryphus.chainvault.config.Constants;
import ch.gryphus.chainvault.entity.MigrationAudit;
import ch.gryphus.chainvault.repository.MigrationAuditRepository;
import io.opentelemetry.api.trace.Span;
import java.time.Instant;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;

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
    public String startProcess(Map<String, Object> variables) {
        ProcessInstance processInstance =
                runtimeService.startProcessInstanceByKey(
                        Constants.BPMN_PROCESS_DEFINITION_KEY, variables);

        String processInstanceId = processInstance.getProcessInstanceId();
        String docId = (String) variables.get(Constants.BPMN_PROC_VAR_DOC_ID);

        // Create initial audit record
        var audit = new MigrationAudit();
        audit.setProcessInstanceKey(processInstanceId);
        audit.setProcessDefinitionKey(processInstance.getProcessDefinitionKey());
        audit.setBpmnProcessId(Constants.BPMN_PROCESS_DEFINITION_KEY);
        audit.setDocumentId(docId);
        audit.setStatus(MigrationAudit.MigrationStatus.PENDING);
        audit.setStartedAt(Instant.now());

        String traceId = Span.current().getSpanContext().getTraceId();
        audit.setTraceId(traceId);

        auditRepo.save(audit);

        return processInstanceId;
    }
}
