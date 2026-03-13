/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.service;

import ch.gryphus.chainvault.config.Constants;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.containers.BindMode;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * The type Base service it.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS) // Force immediate cleanup
public abstract class BaseServiceIT {

    // Test credentials - hardcoded for testing purposes only
    private static final String POSTGRES_DB_PASSWORD = "secret";

    /**
     * The constant postgres.
     */
    @SuppressWarnings("resource")
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
                    .withDatabaseName(Constants.POSTGRES_DB_NAME)
                    .withUsername(Constants.POSTGRES_DB_USER)
                    .withPassword(POSTGRES_DB_PASSWORD)
                    .withClasspathResourceMapping(
                            "db/init-scripts", "/docker-entrypoint-initdb.d", BindMode.READ_ONLY);

    static {
        postgres.start();
        // Manual hook that runs later
        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                () -> {
                                    if (postgres.isRunning()) {
                                        postgres.stop();
                                    }
                                }));
    }
}
