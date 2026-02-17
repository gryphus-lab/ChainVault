package ch.gryphus.demo.migrationtool;

import ch.gryphus.demo.migrationtool.config.SftpTargetConfig;
import ch.gryphus.demo.migrationtool.domain.SourceMetadata;
import ch.gryphus.demo.migrationtool.domain.TiffPage;
import ch.gryphus.demo.migrationtool.service.ArchiveMigrationService;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class MigrationServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @Mock
    private SftpRemoteFileTemplate sftp;

    @Mock
    private XmlMapper xmlMapper;

    @InjectMocks
    private ArchiveMigrationService service;

    @TempDir
    Path tempDir;

    @Captor
    ArgumentCaptor<String> remotePathCaptor;

    @BeforeEach
    void setupMocks() {
        when(restClient.get()).thenReturn(requestSpec);
        when(requestSpec.uri(any(String.class), Optional.ofNullable(any()))).thenReturn(requestSpec);
        when(requestSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void shouldExtractTiffPagesFromZip() throws Exception {
        // only stub what's needed for this test
        byte[] testZip = createTestZipWithTwoTiffs();
        when(responseSpec.body(byte[].class)).thenReturn(testZip);

        List<TiffPage> pages = service.unzipTiffPages(testZip);

        assertThat(pages).hasSize(2);
    }

    @Test
    void shouldCreateChainZipWithPagesAndManifest() throws Exception {
        // Arrange
        List<byte[]> fakePages = List.of(
                "fake-tiff-1".getBytes(),
                "fake-tiff-2".getBytes()
        );

        // Act
        Path chainZip = service.zipPages("doc-123", fakePages, null);  // adjust signature if needed

        // Assert
        assertThat(Files.exists(chainZip)).isTrue();
        assertThat(Files.size(chainZip)).isGreaterThan(100);

        // Optional: unzip and verify contents
        // ...
    }

    @Test
    void shouldUploadThreeFilesViaSftp() throws Exception {
        // Arrange
        Path chainZip = tempDir.resolve("chain.zip");
        Path pdf = tempDir.resolve("doc.pdf");
        Files.writeString(chainZip, "dummy zip");
        Files.writeString(pdf, "dummy pdf");

        String xml = "<doc><id>123</id></doc>";
        String docId = "doc-abc";

        SourceMetadata testSourceMetadata = createTestSourceMetadata(docId);
        when(responseSpec.body(SourceMetadata.class)).thenReturn(testSourceMetadata);
        byte[] testZip = createTestZipWithTwoTiffs();
        when(responseSpec.body(byte[].class)).thenReturn(testZip);
        when(xmlMapper.writeValueAsString(any())).thenReturn(xml);

        // Act
        service.migrateDocument(docId);  // full flow – but we mock internals if needed

        // Assert
        verify(sftp).execute(any());
        verify(sftp, times(3)).send(any(), remotePathCaptor.capture());

        var capturedPaths = remotePathCaptor.getAllValues();
        assertThat(capturedPaths)
                .containsExactly(
                        "/incoming/doc-abc/chain.zip",
                        "/incoming/doc-abc/document.pdf",
                        "/incoming/doc-abc/meta.xml"
                );
    }

    @Test
    void shouldThrowWhenNoTiffsInZip() throws Exception {
        byte[] emptyZip = createEmptyZip();

        assertThatThrownBy(() -> service.unzipTiffPages(emptyZip))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No TIFF");
    }

    // Helpers
    private byte[] createTestZipWithTwoTiffs() throws Exception {
        try (var baos = new ByteArrayOutputStream();
             var zos = new ZipOutputStream(baos)) {

            zos.putNextEntry(new ZipEntry("sample1.tif"));
            zos.write(Files.readAllBytes(Path.of("src/test/resources/sample1.tiff")));
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("sample2.tif"));
            zos.write(Files.readAllBytes(Path.of("src/test/resources/sample2.tiff")));
            zos.closeEntry();

            zos.finish();
            return baos.toByteArray();
        }
    }

    private byte[] createEmptyZip() throws Exception {
        try (var baos = new ByteArrayOutputStream();
             var zos = new ZipOutputStream(baos)) {
            zos.finish();
            return baos.toByteArray();
        }
    }

    private SourceMetadata createTestSourceMetadata(String docId) {
        SourceMetadata metadata = new SourceMetadata();
        metadata.setDocId(docId);
        metadata.setTitle("title");
        metadata.setCreationDate(Instant.now().toString());
        metadata.setClientId("clientId");
        return metadata;
    }
}
