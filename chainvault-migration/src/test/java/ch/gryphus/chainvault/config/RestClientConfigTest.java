/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

/**
 * The type Rest client config test.
 */
class RestClientConfigTest {

    private RestClientConfig restClientConfigUnderTest;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        restClientConfigUnderTest = new RestClientConfig();
    }

    /**
     * Test rest client.
     */
    @Test
    void testRestClient() {
        // Run the test
        RestClient result = restClientConfigUnderTest.restClient("baseUrl", "token");

        // Verify the results
        assertThat(result).isNotNull();
    }
}
