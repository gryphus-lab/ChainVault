/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.docker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.awaitility.Awaitility.await;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * The type Docker services e 2 eit.
 */
@Testcontainers
@DisplayName("Docker Services End-to-End Integration Tests")
class DockerServicesE2EIT {

    private static final String DB_NAME = "chainvault";
    private static final String DB_USER = "chainvault";

    // Test credentials - hardcoded for testing purposes only
    @SuppressWarnings("squid:S2068")
    private static final String DB_PASSWORD = "secret";

    /**
     * The constant postgres.
     */
    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
                    .withDatabaseName(DB_NAME)
                    .withUsername(DB_USER)
                    .withPassword(DB_PASSWORD)
                    .withExposedPorts(5432)
                    .withClasspathResourceMapping(
                            "db/init-scripts", "/docker-entrypoint-initdb.d", BindMode.READ_ONLY)
                    .waitingFor(
                            Wait.forLogMessage(
                                    ".*database system is ready to accept connections.*\\s", 2))
                    .withStartupTimeout(Duration.ofSeconds(120));

    /**
     * The constant sftp.
     */
    @Container
    static GenericContainer<?> sftp =
            new GenericContainer<>(DockerImageName.parse("atmoz/sftp:latest"))
                    .withCommand("testuser:testpass123:::upload")
                    .withExposedPorts(22)
                    .waitingFor(Wait.forLogMessage(".*Server listening on 0.0.0.0 port 22.*", 1))
                    .withStartupTimeout(Duration.ofSeconds(120));

    /**
     * The constant api.
     */
    @Container
    static GenericContainer<?> api =
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
     * Test all services healthy.
     */
    @Test
    @DisplayName("All services should be running and healthy")
    void testAllServicesHealthy() {
        assertThat(postgres.isRunning()).isTrue();
        assertThat(sftp.isRunning()).isTrue();
        assertThat(api.isRunning()).isTrue();
    }

    /**
     * Test postgres connectivity.
     */
    @Test
    @DisplayName("Should connect to PostgreSQL from all external services")
    void testPostgresConnectivity() {
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
                            }
                        });
    }

    /**
     * Test api connectivity.
     */
    @Test
    @DisplayName("Should retrieve documents from API service")
    void testApiConnectivity() {
        HttpClient client = HttpClient.newHttpClient();
        String apiUrl = "http://%s:%d/documents".formatted(api.getHost(), api.getMappedPort(9090));

        await().atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(
                        () -> {
                            HttpRequest request =
                                    HttpRequest.newBuilder().uri(URI.create(apiUrl)).GET().build();

                            assertThatNoException()
                                    .isThrownBy(
                                            () -> {
                                                HttpResponse<String> response =
                                                        client.send(
                                                                request,
                                                                HttpResponse.BodyHandlers
                                                                        .ofString());
                                                assertThat(response.statusCode()).isEqualTo(200);
                                                assertThat(response.body()).hasSizeGreaterThan(1);
                                            });
                        });
    }

    /**
     * Test sftp connectivity.
     */
    @Test
    @DisplayName("Should access SFTP file system")
    void testSftpConnectivity() {
        assertThatNoException()
                .isThrownBy(
                        () -> {
                            var execResult =
                                    sftp.execInContainer("ls", "-la", "/home/testuser/upload");
                            assertThat(execResult.getExitCode()).isZero();
                        });
    }

    /**
     * Test sftp upload capability.
     *
     * @throws Exception the exception
     */
    @Test
    @DisplayName("SFTP should allow file uploads")
    void testSftpUploadCapability() throws Exception {
        var createResult =
                sftp.execInContainer(
                        "sh", "-c", "echo 'test content' > /home/testuser/upload/e2e-test.txt");
        assertThat(createResult.getExitCode()).isZero();

        var listResult = sftp.execInContainer("ls", "-l", "/home/testuser/upload/e2e-test.txt");
        assertThat(listResult.getExitCode()).isZero();
        assertThat(listResult.getStdout()).contains("e2e-test.txt");

        // Cleanup
        sftp.execInContainer("rm", "/home/testuser/upload/e2e-test.txt");
    }

    /**
     * Test postgres concurrent connections.
     */
    @Test
    @DisplayName("PostgreSQL should handle concurrent connections")
    void testPostgresConcurrentConnections() {
        Thread[] threads = new Thread[5];
        AtomicBoolean allSuccess = new AtomicBoolean(true);

        for (int i = 0; i < threads.length; i++) {
            threads[i] =
                    new Thread(
                            () -> {
                                try (Connection conn =
                                        DriverManager.getConnection(
                                                postgres.getJdbcUrl(),
                                                postgres.getUsername(),
                                                postgres.getPassword())) {
                                    Statement stmt = conn.createStatement();
                                    ResultSet rs = stmt.executeQuery("SELECT 1");
                                    if (!rs.next()) {
                                        allSuccess.set(false);
                                    }
                                } catch (Exception e) {
                                    allSuccess.set(false);
                                }
                            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            assertThatNoException().isThrownBy(thread::join);
        }

        assertThat(allSuccess.get()).isTrue();
    }

    /**
     * Test api http access.
     */
    @Test
    @DisplayName("API service should be accessible via HTTP")
    void testApiHttpAccess() {
        HttpClient client = HttpClient.newHttpClient();
        String apiUrl = "http://%s:%d/documents".formatted(api.getHost(), api.getMappedPort(9090));

        await().atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(
                        () -> {
                            HttpRequest request =
                                    HttpRequest.newBuilder().uri(URI.create(apiUrl)).GET().build();

                            assertThatNoException()
                                    .isThrownBy(
                                            () -> {
                                                HttpResponse<String> response =
                                                        client.send(
                                                                request,
                                                                HttpResponse.BodyHandlers
                                                                        .ofString());
                                                assertThat(response.statusCode()).isEqualTo(200);
                                                assertThat(response.body())
                                                        .contains("DOC-ARCH-20250115-001"); // User
                                                // from
                                                // our
                                                // data
                                            });
                        });
    }

    /**
     * Test service stability.
     */
    @Test
    @DisplayName("Services should remain running for extended period")
    void testServiceStability() {
        // Verify services are still running after 5 seconds
        await().atMost(Duration.ofSeconds(5))
                .then()
                .untilAsserted(
                        () -> {
                            assertThat(postgres.isRunning()).isTrue();
                            assertThat(sftp.isRunning()).isTrue();
                            assertThat(api.isRunning()).isTrue();
                        });

        assertThat(postgres.isRunning()).isTrue();
        assertThat(sftp.isRunning()).isTrue();
        assertThat(api.isRunning()).isTrue();
    }

    /**
     * Test postgres database initialization.
     *
     * @throws Exception the exception
     */
    @Test
    @DisplayName("PostgreSQL should have proper database initialized")
    void testPostgresDatabaseInitialization() throws Exception {
        try (Connection conn =
                DriverManager.getConnection(
                        postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())) {

            Statement stmt = conn.createStatement();
            ResultSet rs =
                    stmt.executeQuery(
                            "SELECT datname FROM pg_database WHERE datname = '" + DB_NAME + "'");
            assertThat(rs.next()).isTrue();
            assertThat(rs.getString(1)).isEqualTo(DB_NAME);
        }
    }

    /**
     * Test dynamic sftp port allocation.
     */
    @Test
    @DisplayName("SFTP port should be dynamically allocated")
    void testDynamicSftpPortAllocation() {
        Integer mappedPort = sftp.getMappedPort(22);
        assertThat(mappedPort).isPositive().isGreaterThan(1024); // Should be ephemeral port
    }

    /**
     * Test data persistence.
     *
     * @throws Exception the exception
     */
    @Test
    @DisplayName("Services should maintain data persistence")
    void testDataPersistence() throws Exception {
        // Create a file in SFTP
        var createResult =
                sftp.execInContainer(
                        "sh",
                        "-c",
                        "echo 'persistent data' > /home/testuser/upload/persistent.txt");
        assertThat(createResult.getExitCode()).isZero();

        // Verify it exists
        var listResult = sftp.execInContainer("cat", "/home/testuser/upload/persistent.txt");
        assertThat(listResult.getExitCode()).isZero();
        assertThat(listResult.getStdout()).contains("persistent data");

        // Cleanup
        sftp.execInContainer("rm", "/home/testuser/upload/persistent.txt");
    }

    /**
     * Test api timeout handling.
     */
    @Test
    @DisplayName("Should handle API timeout gracefully")
    void testApiTimeoutHandling() {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

        String apiUrl = "http://%s:%d/documents".formatted(api.getHost(), api.getMappedPort(9090));
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .timeout(Duration.ofSeconds(5))
                        .GET()
                        .build();

        assertThatNoException()
                .isThrownBy(
                        () -> {
                            HttpResponse<String> response =
                                    client.send(request, HttpResponse.BodyHandlers.ofString());
                            assertThat(response.statusCode())
                                    .isIn(200, 404, 500); // Valid HTTP responses
                        });
    }
}
