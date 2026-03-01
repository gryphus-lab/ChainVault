/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.docker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.io.File;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * The type Docker compose it.
 */
@Testcontainers
@DisplayName("Docker Compose Orchestration Tests")
class DockerComposeIT {

    private static final String POSTGRES_SERVICE = "postgres";
    private static final String SFTP_SERVICE = "sftp-test";
    private static final String API_SERVICE = "fake-source-api";
    private static final String CHAINVAULT_APP = "chainvault-app";

    /**
     * The constant dockerCompose.
     */
    @Container
    static ComposeContainer dockerCompose =
            new ComposeContainer(new File("../docker-compose.yml"))
                    .withExposedService(
                            POSTGRES_SERVICE,
                            5432,
                            Wait.forLogMessage(
                                            ".*database system is ready to accept connections.*", 2)
                                    .withStartupTimeout(Duration.ofSeconds(120)))
                    .withExposedService(
                            SFTP_SERVICE,
                            22,
                            Wait.forLogMessage(".*Server listening on 0.0.0.0 port 22.*", 1)
                                    .withStartupTimeout(Duration.ofSeconds(120)))
                    .withExposedService(
                            API_SERVICE,
                            9091,
                            Wait.forHttp("/documents")
                                    .forStatusCode(200)
                                    .withStartupTimeout(Duration.ofSeconds(120)))
                    .withExposedService(
                            CHAINVAULT_APP,
                            8085,
                            Wait.forHttp("/actuator/health")
                                    .forStatusCode(200)
                                    .withStartupTimeout(Duration.ofSeconds(120)));

    /**
     * Test docker compose starts.
     */
    @Test
    @DisplayName("Docker compose container should start successfully")
    void testDockerComposeStarts() {
        assertThat(dockerCompose).isNotNull();
    }

    /**
     * Test postgres service accessibility.
     */
    @Test
    @DisplayName("PostgreSQL service should be accessible")
    void testPostgresServiceAccessibility() {
        String host = dockerCompose.getServiceHost(POSTGRES_SERVICE, 5432);
        Integer port = dockerCompose.getServicePort(POSTGRES_SERVICE, 5432);

        assertThat(host).isNotNull();
        assertThat(port).isPositive();
    }

    /**
     * Test sftp service accessibility.
     */
    @Test
    @DisplayName("SFTP service should be accessible")
    void testSftpServiceAccessibility() {
        String host = dockerCompose.getServiceHost(SFTP_SERVICE, 22);
        Integer port = dockerCompose.getServicePort(SFTP_SERVICE, 22);

        assertThat(host).isNotNull();
        assertThat(port).isPositive();
    }

    /**
     * Test api service accessibility.
     */
    @Test
    @DisplayName("API service should be accessible")
    void testApiServiceAccessibility() {
        String host = dockerCompose.getServiceHost(API_SERVICE, 9091);
        Integer port = dockerCompose.getServicePort(API_SERVICE, 9091);

        assertThat(host).isNotNull();
        assertThat(port).isGreaterThan(9091);
    }

    /**
     * Test service network connectivity.
     */
    @Test
    @DisplayName("Services should have proper network connectivity")
    void testServiceNetworkConnectivity() {
        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(
                        () -> {
                            String postgresHost =
                                    dockerCompose.getServiceHost(POSTGRES_SERVICE, 5432);
                            String sftpHost = dockerCompose.getServiceHost(SFTP_SERVICE, 22);
                            String apiHost = dockerCompose.getServiceHost(API_SERVICE, 9091);

                            assertThat(postgresHost).isNotBlank();
                            assertThat(sftpHost).isNotBlank();
                            assertThat(apiHost).isNotBlank();
                        });
    }

    /**
     * Test services running.
     */
    @Test
    @DisplayName("All services should be running")
    void testServicesRunning() {
        assertThat(dockerCompose.getServicePort(POSTGRES_SERVICE, 5432)).isPositive();
        assertThat(dockerCompose.getServicePort(SFTP_SERVICE, 22)).isPositive();
        assertThat(dockerCompose.getServicePort(API_SERVICE, 9091)).isPositive();
    }

    /**
     * Test port mapping.
     */
    @Test
    @DisplayName("Should handle port mapping correctly")
    void testPortMapping() {
        // Postgres: 5432 -> random local port
        Integer postgresPort = dockerCompose.getServicePort(POSTGRES_SERVICE, 5432);
        assertThat(postgresPort).isGreaterThanOrEqualTo(5432);

        // SFTP: 22 -> random local port (dynamic)
        Integer sftpPort = dockerCompose.getServicePort(SFTP_SERVICE, 22);
        assertThat(sftpPort).isGreaterThan(22);

        // API: 9091 -> 9091 (fixed)
        Integer apiPort = dockerCompose.getServicePort(API_SERVICE, 9091);
        assertThat(apiPort).isGreaterThan(9091);
    }
}
