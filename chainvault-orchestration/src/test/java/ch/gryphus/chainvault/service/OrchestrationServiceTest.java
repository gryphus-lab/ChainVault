package ch.gryphus.chainvault.service;

import ch.gryphus.chainvault.repository.MigrationAuditRepository;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * The type Orchestration service test.
 */
@ExtendWith(MockitoExtension.class)
class OrchestrationServiceTest {

    @Mock
    private RuntimeService mockRuntimeService;

    @Mock private MigrationAuditRepository migrationAuditRepository;

    private OrchestrationService orchestrationServiceUnderTest;

    @Mock
    private ProcessInstance mockProcessInstance;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        orchestrationServiceUnderTest = new OrchestrationService(mockRuntimeService, migrationAuditRepository);
        when(mockProcessInstance.getProcessInstanceId()).thenReturn("test");
    }

    /**
     * Test start process.
     */
    @Test
    void testStartProcess() {
        // Setup
        final Map<String, Object> variables = Map.ofEntries(Map.entry("docId", "123"));
        when(mockRuntimeService.startProcessInstanceByKey(anyString(), anyMap())).thenReturn(mockProcessInstance);

        // Run the test
        final String result = orchestrationServiceUnderTest.startProcess(variables);

        // Verify the results
        assertThat(result).isEqualTo("test");
    }
}
