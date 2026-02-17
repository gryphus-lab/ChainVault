package ch.gryphus.demo.migrationtool.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

@SpringBootTest
@Testcontainers
class ArchiveMigrationServiceIntegrationTest {

    @Container
    static GenericContainer<?> sftpContainer = new GenericContainer<>(DockerImageName.parse("atmoz/sftp:latest"))
            .withCommand("testuser:testpass123:::upload")
            .withExposedPorts(22)
            .waitingFor(Wait.forLogMessage(".*Server listening on 0.0.0.0 port 22.*", 1));

    @Autowired
    private ArchiveMigrationService service;

    @DynamicPropertySource
    static void overrideSftpProperties(DynamicPropertyRegistry registry) {
        registry.add("target.sftp.host", sftpContainer::getHost);
        registry.add("target.sftp.port", () -> String.valueOf(sftpContainer.getMappedPort(22)));
        registry.add("target.sftp.username", () -> "testuser");
        registry.add("target.sftp.password", () -> "testpass123");
        registry.add("target.sftp.remote-directory", () -> "/upload");
        registry.add("target.sftp.allow-unknown-keys", () -> "true");
    }

    @Test
    void migrateDocument_shouldUploadToRealSftp() throws Exception {
        String docId = "DOC-IT-001";

        // Assume mocks for restClient or real calls – add as needed

        service.migrateDocument(docId);

        // Verify upload (exec ls in container)
        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            String uploadDir = "/upload/" + docId;
            String lsResult = sftpContainer.execInContainer("ls", uploadDir).getStdout();

            assertThat(lsResult).contains("_chain.zip");
            assertThat(lsResult).contains(".pdf");
            assertThat(lsResult).contains("_meta.xml");
        });
    }
}