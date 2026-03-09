/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import ch.gryphus.chainvault.config.Constants;
import ch.gryphus.chainvault.repository.MigrationAuditRepository;
import java.util.Map;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * The type Orchestration service test.
 */
@ExtendWith(MockitoExtension.class)
class OrchestrationServiceTest {

    @Mock private RuntimeService mockRuntimeService;

    @Mock private MigrationAuditRepository auditRepository;

    private OrchestrationService orchestrationServiceUnderTest;

    @Mock private ProcessInstance mockProcessInstance;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        orchestrationServiceUnderTest =
                new OrchestrationService(mockRuntimeService, auditRepository);
        when(mockProcessInstance.getProcessInstanceId()).thenReturn("test");
    }

    /**
     * Test start process.
     */
    @Test
    void testStartProcess() {
        // Setup
        Map<String, Object> variables =
                Map.ofEntries(Map.entry(Constants.BPMN_PROC_VAR_DOC_ID, "123"));
        when(mockRuntimeService.startProcessInstanceByKey(anyString(), anyMap()))
                .thenReturn(mockProcessInstance);

        // Run the test
        String result = orchestrationServiceUnderTest.startProcess(variables);

        // Verify the results
        assertThat(result).isEqualTo("test");
    }
}
