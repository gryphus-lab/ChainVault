/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.service;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.await;

import ch.gryphus.chainvault.config.Constants;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * The type Orchestration service it.
 */
@SpringBootTest
@Testcontainers
class OrchestrationServiceIT extends BaseServiceIT {

    /**
     * The constant jsonServer.
     */
    @SuppressWarnings("resource")
    // Fake REST API (json-server with db.json)
    @Container
    static final GenericContainer<?> jsonServer =
            new GenericContainer<>(DockerImageName.parse("node:25-alpine"))
                    .withPrivilegedMode(true)
                    .withCommand(
                            "sh",
                            "-c",
                            "npm install -g json-server && json-server --watch /data/db.json"
                                    + " --static /data/static --port 9090")
                    .withClasspathResourceMapping("db.json", "/data/db.json", BindMode.READ_ONLY)
                    .withClasspathResourceMapping("static", "/data/static", BindMode.READ_ONLY)
                    .withExposedPorts(9090)
                    .waitingFor(Wait.forHttp("/").forStatusCode(200));

    /**
     * The constant sftpContainer.
     */
    @SuppressWarnings("resource")
    @Container
    static final GenericContainer<?> sftpContainer =
            new GenericContainer<>(DockerImageName.parse("atmoz/sftp:latest"))
                    .withCommand("testuser:testpass123:::upload")
                    .withExposedPorts(22)
                    .waitingFor(Wait.forLogMessage(".*Server listening on 0.0.0.0 port 22.*", 1));

    @Autowired private MigrationService migrationService;

    @Autowired private OrchestrationService orchestrationService;

    /**
     * Configure properties.
     *
     * @param registry the registry
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Fake source API
        String apiUrl =
                "http://%s:%d".formatted(jsonServer.getHost(), jsonServer.getMappedPort(9090));
        registry.add("source.api.base-url", () -> apiUrl);
        registry.add("source.api.token", () -> "dummy-token");

        // Postgres
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        // SFTP
        registry.add("target.sftp.host", sftpContainer::getHost);
        registry.add("target.sftp.port", () -> String.valueOf(sftpContainer.getMappedPort(22)));
        registry.add("target.sftp.username", () -> "testuser");
        registry.add("target.sftp.password", () -> "testpass123");
        registry.add("target.sftp.remote-directory", () -> "/upload");
        registry.add("target.sftp.allow-unknown-keys", () -> "true");
    }

    /**
     * Migrate document should upload to real sftp.
     *
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    @Test
    void migrateDocument_shouldUploadToRealSftp() throws IOException, InterruptedException {
        String docId = "DOC-ARCH-2025-001";
        Map<String, Object> variables = Map.of(Constants.BPMN_PROC_VAR_DOC_ID, docId);

        // Check if process workflow is started
        String processInstanceId = orchestrationService.startProcess(variables);
        assertThat(processInstanceId).isNotNull();

        // Wait for upload to appear in SFTP (poll the container)
        String expectedDir = "/home/testuser/upload/%s-%s".formatted(processInstanceId, docId);

        await().atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(
                        () -> {
                            String lsResult =
                                    sftpContainer
                                            .execInContainer("ls", "-l", expectedDir)
                                            .getStdout();

                            assertThat(lsResult)
                                    .as("SFTP directory should contain uploaded files")
                                    .contains("_chain.zip")
                                    .contains(".pdf")
                                    .contains("_meta.xml");
                        });

        // Optional: check file count
        String fileCount =
                sftpContainer
                        .execInContainer("sh", "-c", "ls %s | wc -l".formatted(expectedDir))
                        .getStdout()
                        .trim();
        assertThat(Integer.parseInt(fileCount.trim())).isEqualTo(3);
    }

    /**
     * Migrate document with non existing doc should fail gracefully.
     */
    @Test
    void migrateDocument_withNonExistingDoc_shouldFailGracefully() {
        String invalidId = "DOC-NOT-EXISTS-999";
        Map<String, Object> variables = Map.of(Constants.BPMN_PROC_VAR_DOC_ID, invalidId);

        // exception is handled as an on the BPM layer
        assertThatNoException().isThrownBy(() -> orchestrationService.startProcess(variables));
    }
}
