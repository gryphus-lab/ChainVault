/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.docker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.awaitility.Awaitility.await;

import java.io.File;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.docker.compose.core.DockerComposeFile;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * The type Docker compose it.
 */
@Testcontainers
@DisplayName("Docker Compose Integration Tests")
class DockerComposeIT {

    // observability services
    private static final String OTEL_LGTM_SERVICE = "otel-lgtm";
    private static final String ALLOY_SERVICE = "alloy";

    // application services
    private static final String POSTGRES_SERVICE = "postgres";
    private static final String SFTP_SERVICE = "sftp-test";
    private static final String API_SERVICE = "fake-source-api";
    private static final String CHAINVAULT_SERVICE = "chainvault-app";

    private static final List<File> FILE_LIST =
            List.of(new File("../docker-compose.yml"), new File("../docker-compose-lgtm.yml"));

    @Container
    private static final ComposeContainer dockerCompose =
            new ComposeContainer(FILE_LIST)
                    .withExposedService(OTEL_LGTM_SERVICE, 3000) // grafana port
                    .withExposedService(ALLOY_SERVICE, 12345) // allow port
                    .withExposedService(
                            POSTGRES_SERVICE,
                            5432,
                            Wait.forLogMessage(
                                            ".*database system is ready to accept connections.*", 2)
                                    .withStartupTimeout(Duration.ofSeconds(10L)))
                    .withExposedService(
                            SFTP_SERVICE,
                            22,
                            Wait.forLogMessage(".*Server listening on 0.0.0.0 port 22.*", 1)
                                    .withStartupTimeout(Duration.ofSeconds(10L)))
                    .withExposedService(
                            API_SERVICE,
                            9091,
                            Wait.forLogMessage(".*JSON Server started on PORT :9091.*", 1)
                                    .withStartupTimeout(Duration.ofSeconds(120L)))
                    .withExposedService(CHAINVAULT_SERVICE, 8085)
                    .withBuild(true);

    /**
     * Test all services start.
     */
    @Test
    @DisplayName("Test all services defined in docker compose YAMLs start successfully")
    void testAllServicesStart() {
        assertThat(dockerCompose).isNotNull();

        // check lgtm services
        assertThat(dockerCompose.getContainerByServiceName(OTEL_LGTM_SERVICE)).isNotNull();
        assertThat(dockerCompose.getContainerByServiceName(ALLOY_SERVICE)).isNotNull();

        // check services
        assertThat(dockerCompose.getContainerByServiceName(POSTGRES_SERVICE)).isNotNull();
        assertThat(dockerCompose.getContainerByServiceName(SFTP_SERVICE)).isNotNull();
        assertThat(dockerCompose.getContainerByServiceName(API_SERVICE)).isNotNull();
        assertThat(dockerCompose.getContainerByServiceName(CHAINVAULT_SERVICE)).isNotNull();

        assertThat(dockerCompose.getServicePort(POSTGRES_SERVICE, 5432)).isPositive();
        assertThat(dockerCompose.getServicePort(SFTP_SERVICE, 22)).isPositive();
        assertThat(dockerCompose.getServicePort(API_SERVICE, 9091)).isPositive();
        assertThat(dockerCompose.getServicePort(CHAINVAULT_SERVICE, 8085)).isPositive();
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
     * Test chainvault rest api accessibility.
     */
    @Test
    @DisplayName("Chainvault REST API should be accessible")
    void testChainvaultRestApiAccessibility() {
        String host = dockerCompose.getServiceHost(CHAINVAULT_SERVICE, 8085);
        Integer port = dockerCompose.getServicePort(CHAINVAULT_SERVICE, 8085);

        assertThat(host).isNotNull();
        assertThat(port).isGreaterThan(9091);
    }

    /**
     * Test service network connectivity.
     */
    @Test
    @DisplayName("Services should have proper network connectivity")
    void testServiceNetworkConnectivity() {
        await().atMost(Duration.ofSeconds(30L))
                .pollInterval(Duration.ofMillis(500L))
                .untilAsserted(
                        () -> {
                            String postgresHost =
                                    dockerCompose.getServiceHost(POSTGRES_SERVICE, 5432);
                            String sftpHost = dockerCompose.getServiceHost(SFTP_SERVICE, 22);
                            String apiHost = dockerCompose.getServiceHost(API_SERVICE, 9091);
                            String chainvaultHost =
                                    dockerCompose.getServiceHost(CHAINVAULT_SERVICE, 8085);

                            assertThat(postgresHost).isNotBlank();
                            assertThat(sftpHost).isNotBlank();
                            assertThat(apiHost).isNotBlank();
                            assertThat(chainvaultHost).isNotBlank();
                        });
    }

    /**
     * Test expected service definitions.
     */
    @Test
    @DisplayName("Test all expected services are defined")
    void testExpectedServiceDefinitions() {
        var list =
                List.of(
                        OTEL_LGTM_SERVICE,
                        ALLOY_SERVICE,
                        POSTGRES_SERVICE,
                        SFTP_SERVICE,
                        API_SERVICE,
                        CHAINVAULT_SERVICE);

        // check docker-compose.yml and docker-compose-lgtm.yml files are processed as expected
        assertThatNoException().isThrownBy(() -> DockerComposeFile.of(FILE_LIST));

        // check docker compose container if the services are started as expected
        await().atMost(Duration.ofSeconds(30L))
                .pollInterval(Duration.ofMillis(500L))
                .untilAsserted(
                        () ->
                                list.forEach(
                                        name -> {
                                            var serviceName =
                                                    dockerCompose.getContainerByServiceName(name);
                                            assertThat(serviceName).isPresent();
                                            var containerState =
                                                    serviceName.get().getContainerInfo().getState();
                                            assertThat(containerState.getStatus())
                                                    .hasToString("running");
                                        }));
    }

    /**
     * Test port mapping.
     */
    @Test
    @DisplayName("Should handle port mapping correctly")
    void testPortMapping() {
        // Postgres: 5432 -> random local port
        Integer postgresPort = dockerCompose.getServicePort(POSTGRES_SERVICE, 5432);
        assertThat(postgresPort).isGreaterThan(5432);

        // SFTP: 22 -> random local port
        Integer sftpPort = dockerCompose.getServicePort(SFTP_SERVICE, 22);
        assertThat(sftpPort).isGreaterThan(22);

        // API: 9091 -> random local port
        Integer apiPort = dockerCompose.getServicePort(API_SERVICE, 9091);
        assertThat(apiPort).isGreaterThan(9091);

        Integer chainvaultPort = dockerCompose.getServicePort(CHAINVAULT_SERVICE, 8085);
        assertThat(chainvaultPort).isGreaterThan(8085);
    }
}
