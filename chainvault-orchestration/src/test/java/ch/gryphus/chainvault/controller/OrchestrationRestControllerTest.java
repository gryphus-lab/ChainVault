/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import ch.gryphus.chainvault.config.Constants;
import ch.gryphus.chainvault.config.TraceIdFilter;
import ch.gryphus.chainvault.service.OrchestrationService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import tools.jackson.databind.ObjectMapper;

/**
 * The type Orchestration rest controller test.
 */
@WebMvcTest(OrchestrationRestController.class)
class OrchestrationRestControllerTest {

    @Autowired private MockMvcTester mockMvcTester;

    @MockitoBean private OrchestrationService mockOrchestrationService;

    @Autowired private ObjectMapper objectMapper;

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
     * Test start process instance.
     */
    @Test
    void testStartProcessInstance() {
        // Setup
        when(mockOrchestrationService.startProcess(any())).thenReturn("test");

        Map<String, Object> variables = Map.of(Constants.BPMN_PROC_VAR_DOC_ID, "123");
        String json = objectMapper.writeValueAsString(variables);

        // Run the test and verify the results
        var response =
                mockMvcTester
                        .post()
                        .uri("/chainvault/process")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .exchange();
        assertThat(response).hasStatus(HttpStatus.CREATED);
    }
}
