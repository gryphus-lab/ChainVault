/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.service;

import ch.gryphus.chainvault.config.Constants;
import ch.gryphus.chainvault.entity.MigrationAudit;
import ch.gryphus.chainvault.repository.MigrationAuditRepository;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import java.time.Instant;
import java.util.HashMap;
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
    private final Tracer tracer;

    /**
     * Instantiates a new Orchestration service.
     *
     * @param runtimeService the runtime service
     * @param auditRepo      the audit repo
     * @param tracer         the tracer
     */
    public OrchestrationService(
            RuntimeService runtimeService, MigrationAuditRepository auditRepo, Tracer tracer) {
        this.runtimeService = runtimeService;
        this.auditRepo = auditRepo;
        this.tracer = tracer;
    }

    /**
     * Start process string.
     *
     * @param variables the variables
     * @return the string
     */
    public String startProcess(Map<String, Object> variables) {
        Span parentSpan = tracer.spanBuilder("http post /chainvault/process").startSpan();

        // Store the parent context for the async handoff
        Context parentContext = Context.current().with(parentSpan);

        try (Scope scope = parentContext.makeCurrent()) {
            String traceParent =
                    String.format(
                            "00-%s-%s-01",
                            parentSpan.getSpanContext().getTraceId(),
                            parentSpan.getSpanContext().getSpanId());

            Map<String, Object> map = new HashMap<>(variables);
            map.put("traceParent", traceParent);

            ProcessInstance processInstance =
                    runtimeService.startProcessInstanceByKey(
                            Constants.BPMN_PROCESS_DEFINITION_KEY, map);

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
        } finally {
            parentSpan.end();
        }
    }
}
