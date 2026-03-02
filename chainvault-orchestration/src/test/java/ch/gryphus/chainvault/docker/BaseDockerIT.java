/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.docker;

import java.time.Duration;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public abstract class BaseDockerIT {

    protected static final String DB_NAME = "chainvault";
    private static final String DB_USER = "chainvault";

    // Test credentials - hardcoded for testing purposes only
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
}
