/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.service;

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

    /**
     * Instantiates a new Orchestration service.
     *
     * @param runtimeService the runtime service
     */
    @Autowired
    public OrchestrationService(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
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
        return processInstance.getProcessInstanceId();
    }
}
