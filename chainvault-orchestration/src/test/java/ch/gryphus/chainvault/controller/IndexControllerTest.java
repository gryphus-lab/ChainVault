/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import ch.gryphus.chainvault.config.TraceIdFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(IndexController.class)
class IndexControllerTest {

    @Autowired private MockMvcTester mockMvcTester;
    @MockitoBean private TraceIdFilter traceIdFilter;

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

    @Test
    void testHome() {
        // Setup
        // Run the test and verify the results
        var result = mockMvcTester.get().uri("/").exchange();
        assertThat(result).hasStatus(HttpStatus.OK).hasViewName("index");
    }
}
