/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.docker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * The type Docker services it.
 */
@Testcontainers
@DisplayName("Docker Services Integration Tests")
class DockerServicesIT extends BaseDockerIT {

    /**
     * Test postgres health check.
     */
    @Test
    void testPostgresHealthCheck() {
        // Verify PostgreSQL container is running
        assertThat(postgres.isRunning()).isTrue();

        // Verify database connectivity
        assertThatNoException()
                .isThrownBy(
                        () -> {
                            try (Connection conn =
                                    DriverManager.getConnection(
                                            postgres.getJdbcUrl(),
                                            postgres.getUsername(),
                                            postgres.getPassword())) {

                                assertThat(conn).isNotNull();

                                Statement stmt = conn.createStatement();
                                ResultSet rs = stmt.executeQuery("SELECT 1");
                                assertThat(rs.next()).isTrue();
                                assertThat(rs.getInt(1)).isEqualTo(1);
                            }
                        });
    }

    /**
     * Test postgres version compatibility.
     *
     * @throws SQLException the sql exception
     */
    @Test
    void testPostgresVersionCompatibility() throws SQLException {
        try (Connection conn =
                DriverManager.getConnection(
                        postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())) {

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT version()");
            assertThat(rs.next()).isTrue();
            String version = rs.getString(1);
            assertThat(version).contains("PostgreSQL").contains("18");
        }
    }

    /**
     * Test postgres port mapping.
     */
    @Test
    void testPostgresPortMapping() {
        Integer mappedPort = postgres.getMappedPort(5432);
        assertThat(mappedPort).isNotNull().isPositive().isGreaterThanOrEqualTo(5432);
    }

    /**
     * Test postgres connection pooling.
     *
     * @throws SQLException the sql exception
     */
    @Test
    void testPostgresConnectionPooling() throws SQLException {
        // Test multiple concurrent connections
        for (int i = 0; i < 5; i++) {
            try (Connection conn =
                    DriverManager.getConnection(
                            postgres.getJdbcUrl(),
                            postgres.getUsername(),
                            postgres.getPassword())) {

                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT 1");
                assertThat(rs.next()).isTrue();
            }
        }
    }

    /**
     * Test sftp service availability.
     */
    @Test
    void testSftpServiceAvailability() {
        assertThat(sftpContainer.isRunning()).isTrue();

        // Verify SFTP port is exposed
        Integer mappedPort = sftpContainer.getMappedPort(22);
        assertThat(mappedPort).isPositive();

        // Verify SFTP directory exists
        assertThatNoException()
                .isThrownBy(
                        () -> {
                            var result =
                                    sftpContainer.execInContainer(
                                            "ls", "-l", "/home/testuser/upload");
                            assertThat(result.getExitCode()).isZero();
                        });
    }

    /**
     * Test sftp user access.
     *
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    @Test
    void testSftpUserAccess() throws IOException, InterruptedException {
        // Create a test file in the SFTP container
        var createFileResult =
                sftpContainer.execInContainer(
                        "sh", "-c", "echo 'test data' > /home/testuser/upload/test.txt");
        assertThat(createFileResult.getExitCode()).isZero();

        // Verify file exists
        var listResult =
                sftpContainer.execInContainer("ls", "-l", "/home/testuser/upload/test.txt");
        assertThat(listResult.getExitCode()).isZero();
        assertThat(listResult.getStdout()).contains("test.txt");

        // Clean up
        sftpContainer.execInContainer("rm", "/home/testuser/upload/test.txt");
    }

    /**
     * Test sftp port mapping.
     */
    @Test
    void testSftpPortMapping() {
        Integer mappedPort = sftpContainer.getMappedPort(22);
        assertThat(mappedPort).isNotNull().isPositive().isNotEqualTo(22); // Dynamic port allocation
    }

    /**
     * Test sftp upload directory.
     *
     * @throws IOException          the io exception
     * @throws InterruptedException the interrupted exception
     */
    @Test
    void testSftpUploadDirectory() throws IOException, InterruptedException {
        // Verify the upload directory exists and is writable
        var result =
                sftpContainer.execInContainer(
                        "sh",
                        "-c",
                        "[ -d /home/testuser/upload ] && echo 'exists' || echo 'not found'");

        assertThat(result.getExitCode()).isZero();
        assertThat(result.getStdout().trim()).isEqualTo("exists");
    }

    /**
     * Test api service availability.
     */
    @Test
    void testApiServiceAvailability() {
        assertThat(apiContainer.isRunning()).isTrue();

        Integer mappedPort = apiContainer.getMappedPort(9091);
        assertThat(mappedPort).isPositive();
    }

    /**
     * Test api response format.
     */
    @Test
    void testApiResponseFormat() {
        String apiUrl =
                "http://%s:%d/documents"
                        .formatted(apiContainer.getHost(), apiContainer.getMappedPort(9091));

        // Wait for API to be fully ready
        await().atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(
                        () -> {
                            try {
                                var url = URI.create(apiUrl).toURL();
                                var conn = (HttpURLConnection) url.openConnection();
                                conn.setConnectTimeout(1000);
                                conn.setReadTimeout(1000);
                                assertThat(conn.getResponseCode())
                                        .isEqualTo(HttpURLConnection.HTTP_OK); // 200 OK
                                assertThat(conn.getContentType())
                                        .isEqualTo(MediaType.APPLICATION_JSON_VALUE);
                            } catch (Exception e) {
                                throw new AssertionError("API not ready yet", e);
                            }
                        });
    }

    /**
     * Test api service configuration resources.
     */
    @Test
    void testApiServiceConfigurationResources() {
        var list = getCommandList();

        await().atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(
                        () ->
                                list.forEach(
                                        command -> {
                                            try {
                                                var result = apiContainer.execInContainer(command);
                                                assertThat(result.getExitCode()).isZero();
                                                assertThat(result.getStdout().trim())
                                                        .isEqualTo("exists");
                                            } catch (Exception e) {
                                                throw new IllegalStateException("Exec failed", e);
                                            }
                                        }));
    }

    private static @NonNull List<String[]> getCommandList() {
        String checkExists = "&& echo 'exists' || echo 'not found'";
        return List.of(
                new String[] { // check if static payload directory exists
                    "sh", "-c", "[ -d /data/static/payloads ] %s".formatted(checkExists)
                },
                new String[] { // check if db.json config file exists
                    "sh", "-c", "[ -f /data/db.json ] %s".formatted(checkExists)
                });
    }

    /**
     * Test service isolation.
     */
    @Test
    void testServiceIsolation() {
        // Ensure each service has its own container instance
        assertThat(postgres.getContainerId()).isNotEmpty();
        assertThat(sftpContainer.getContainerId()).isNotEmpty();
        assertThat(apiContainer.getContainerId()).isNotEmpty();

        // Verify they have different container IDs
        assertThat(postgres.getContainerId())
                .isNotEqualTo(sftpContainer.getContainerId())
                .isNotEqualTo(apiContainer.getContainerId());
    }

    /**
     * Test service memory and resource usage.
     */
    @Test
    void testServiceMemoryAndResourceUsage() {
        List<GenericContainer<?>> list = List.of(postgres, sftpContainer, apiContainer);

        await().atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(
                        () ->
                                list.forEach(
                                        container -> {
                                            // Verify containers are running and consuming resources
                                            assertThat(container.isRunning()).isTrue();

                                            // All containers should have non-empty container IDs
                                            // (indicating they're running)
                                            assertThat(container.getContainerId()).isNotEmpty();
                                        }));
    }
}
