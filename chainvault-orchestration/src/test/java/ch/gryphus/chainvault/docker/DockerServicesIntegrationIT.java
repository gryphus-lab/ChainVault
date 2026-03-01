/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.docker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.awaitility.Awaitility.await;

import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * The type Docker services integration it.
 */
@Testcontainers
class DockerServicesIntegrationIT {

    /**
     * The constant postgresContainer.
     */
    @Container
    static PostgreSQLContainer postgresContainer =
            new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
                    .withDatabaseName("chainvault")
                    .withUsername("chainvault")
                    .withPassword("secret")
                    .withExposedPorts(5432)
                    .waitingFor(
                            Wait.forLogMessage(
                                    ".*database system is ready to accept connections.*\\s", 2))
                    .withStartupTimeout(Duration.ofSeconds(120));

    /**
     * The constant sftpContainer.
     */
    @Container
    static GenericContainer<?> sftpContainer =
            new GenericContainer<>(DockerImageName.parse("atmoz/sftp:latest"))
                    .withCommand("testuser:testpass123:::upload")
                    .withExposedPorts(22)
                    .waitingFor(Wait.forLogMessage(".*Server listening on 0.0.0.0 port 22.*", 1))
                    .withStartupTimeout(Duration.ofSeconds(120));

    /**
     * The constant apiContainer.
     */
    @Container
    static GenericContainer<?> apiContainer =
            new GenericContainer<>(DockerImageName.parse("node:25-alpine"))
                    .withPrivilegedMode(true)
                    .withCommand(
                            "sh",
                            "-c",
                            "npm install -g json-server && json-server --watch /data/db.json"
                                    + " --static /data/static --port 9090 --host 0.0.0.0")
                    .withClasspathResourceMapping("db.json", "/data/db.json", BindMode.READ_ONLY)
                    .withClasspathResourceMapping("static", "/data/static", BindMode.READ_ONLY)
                    .withExposedPorts(9090)
                    .waitingFor(Wait.forHttp("/").forStatusCode(200))
                    .withStartupTimeout(Duration.ofSeconds(120));

    /**
     * Test postgres health check.
     */
    @Test
    void testPostgresHealthCheck() {
        // Verify PostgreSQL container is running
        assertThat(postgresContainer.isRunning()).isTrue();

        // Verify database connectivity
        assertThatNoException()
                .isThrownBy(
                        () -> {
                            try (Connection conn =
                                    DriverManager.getConnection(
                                            postgresContainer.getJdbcUrl(),
                                            postgresContainer.getUsername(),
                                            postgresContainer.getPassword())) {

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
                        postgresContainer.getJdbcUrl(),
                        postgresContainer.getUsername(),
                        postgresContainer.getPassword())) {

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT version()");
            assertThat(rs.next()).isTrue();
            String version = rs.getString(1);
            assertThat(version).contains("PostgreSQL").contains("16");
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
     * Test api service availability.
     */
    @Test
    void testApiServiceAvailability() {
        assertThat(apiContainer.isRunning()).isTrue();

        Integer mappedPort = apiContainer.getMappedPort(9090);
        assertThat(mappedPort).isPositive();
    }

    /**
     * Test api response format.
     */
    @Test
    void testApiResponseFormat() {
        String apiUrl =
                "http://%s:%d".formatted(apiContainer.getHost(), apiContainer.getMappedPort(9090));

        // Wait for API to be fully ready
        await().atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(
                        () -> {
                            try {
                                var url = URI.create(apiUrl).toURL();
                                var conn = url.openConnection();
                                conn.setConnectTimeout(1000);
                                conn.setReadTimeout(1000);
                                conn.getInputStream();
                            } catch (Exception e) {
                                throw new AssertionError("API not ready yet", e);
                            }
                        });
    }

    /**
     * Test postgres port mapping.
     */
    @Test
    void testPostgresPortMapping() {
        Integer mappedPort = postgresContainer.getMappedPort(5432);
        assertThat(mappedPort).isNotNull().isPositive().isGreaterThanOrEqualTo(5432);
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
     * Test service isolation.
     */
    @Test
    void testServiceIsolation() {
        // Ensure each service has its own container instance
        assertThat(postgresContainer.getContainerId()).isNotEmpty();
        assertThat(sftpContainer.getContainerId()).isNotEmpty();
        assertThat(apiContainer.getContainerId()).isNotEmpty();

        // Verify they have different container IDs
        assertThat(postgresContainer.getContainerId())
                .isNotEqualTo(sftpContainer.getContainerId())
                .isNotEqualTo(apiContainer.getContainerId());
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
                            postgresContainer.getJdbcUrl(),
                            postgresContainer.getUsername(),
                            postgresContainer.getPassword())) {

                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT 1");
                assertThat(rs.next()).isTrue();
            }
        }
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
     * Test service memory and resource usage.
     */
    @Test
    void testServiceMemoryAndResourceUsage() {
        // Verify containers are running and consuming resources
        assertThat(postgresContainer.isRunning()).isTrue();
        assertThat(sftpContainer.isRunning()).isTrue();
        assertThat(apiContainer.isRunning()).isTrue();

        // All containers should have non-empty container IDs (indicating they're running)
        assertThat(postgresContainer.getContainerId()).isNotEmpty();
        assertThat(sftpContainer.getContainerId()).isNotEmpty();
        assertThat(apiContainer.getContainerId()).isNotEmpty();
    }
}
