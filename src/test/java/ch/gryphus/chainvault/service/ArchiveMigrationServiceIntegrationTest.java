package ch.gryphus.chainvault.service;

import org.junit.jupiter.api.Disabled;
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

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.awaitility.Awaitility.await;

@Disabled
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

        // SFTP
        registry.add("target.sftp.host", sftpContainer::getHost);
        registry.add("target.sftp.port", () -> String.valueOf(sftpContainer.getMappedPort(22)));
        registry.add("target.sftp.username", () -> "testuser");
        registry.add("target.sftp.password", () -> "testpass123");
        registry.add("target.sftp.remote-directory", () -> "/upload");
        registry.add("target.sftp.allow-unknown-keys", () -> "true");
    }

    @Test
    void migrateDocument_shouldUploadToRealSftp() throws Exception {
        String docId = "DOC-ARCH-20250115-001";  // exists in your verbose db.json

        // Act – full real flow
        service.migrateDocument(docId);

        // Wait for upload to appear in SFTP (poll the container)
        String expectedDir = "/upload/" + docId;

        await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> {
                    String lsResult = sftpContainer.execInContainer("ls", "-l", expectedDir).getStdout();

                    assertThat(lsResult)
                            .as("SFTP directory should contain uploaded files")
                            .contains("_chain.zip")
                            .contains(".pdf")
                            .contains("_meta.xml");
                });

        // Optional: check file count
        String fileCount = sftpContainer.execInContainer("ls", expectedDir, "|", "wc", "-l").getStdout().trim();
        assertThat(Integer.parseInt(fileCount.trim())).isEqualTo(3);
    }

    @Test
    void migrateDocument_withNonExistingDoc_shouldFailGracefully() {
        String invalidId = "DOC-NOT-EXISTS-999";

        // No exception expected if your code handles 404 gracefully
        // or assertThrows if you want it to fail loudly
        assertThatNoException()
                .isThrownBy(() -> service.migrateDocument(invalidId));
    }
}