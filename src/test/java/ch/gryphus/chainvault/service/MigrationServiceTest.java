package ch.gryphus.chainvault.service;

import ch.gryphus.chainvault.config.SftpTargetConfig;
import ch.gryphus.chainvault.domain.ArchivalMetadata;
import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.domain.SourceMetadata;
import ch.gryphus.chainvault.domain.TiffPage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
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
    private SftpTargetConfig sftpCfg;

    @Mock
    private XmlMapper xmlMapper;

    @Spy
    @InjectMocks
    private MigrationService migrationService;

    @Mock
    private ObjectMapper jsonMapper;  // used inside createChainZip

    @TempDir
    Path tempDir;

    @Captor
    ArgumentCaptor<String> xmlCaptor;

    private MigrationContext ctx;
    private SourceMetadata meta;

    @BeforeEach
    void setup() {
        ctx = new MigrationContext("DOC-TEST-001");
        ctx.setPayloadHash("payload-sha256-abc123");

        meta = new SourceMetadata();
        meta.setDocId("DOC-TEST-001");
        meta.setTitle("Test Invoice 2026");
        meta.setCreationDate(Instant.now().toString());
        meta.setClientId("CHE-123.456.789");
        meta.setDocumentType("INVOICE");
        meta.setHash("sha256-abc123");
        meta.setAccountNo("ACC-123");

        // Make fluent chain return itself (common pattern)
        when(restClient.get()).thenReturn(requestSpec);
        when(requestSpec.uri(anyString(), Optional.ofNullable(any()))).thenReturn(requestSpec);
        when(requestSpec.retrieve()).thenReturn(responseSpec);
    }

    // ────────────────────────────────────────────────
    // sha256
    // ────────────────────────────────────────────────
    @Test
    void sha256_shouldComputeCorrectHash() throws Exception {
        byte[] data = "hello world".getBytes();
        String expected = "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9";

        String hash = migrationService.sha256(data);
        assertThat(hash).isEqualTo(expected);
    }

    @Test
    void sha256_path_shouldMatchByteArray() throws Exception {
        Path file = tempDir.resolve("test.txt");
        Files.writeString(file, "hello world");

        String hashFromBytes = migrationService.sha256("hello world".getBytes());
        String hashFromPath = migrationService.sha256(file);

        assertThat(hashFromPath).isEqualTo(hashFromBytes);
    }

    // ────────────────────────────────────────────────
    // unzipTiffPages
    // ────────────────────────────────────────────────
    @Test
    void unzipTiffPages_shouldExtractAndPreserveOrder() throws Exception {
        byte[] zip = createZipWithTiffs(List.of(
                "page-001.tif", "TIFF content 1",
                "page-002.tif", "TIFF content 2",
                "page-003.tif", "TIFF content 3"
        ));

        List<TiffPage> pages = migrationService.unzipTiffPages(zip);

        assertThat(pages).hasSize(3);
        assertThat(pages.get(0).name()).isEqualTo("page-001.tif");
        assertThat(new String(pages.get(0).data())).isEqualTo("TIFF content 1");
        assertThat(pages.get(2).name()).isEqualTo("page-003.tif");
    }

    @Test
    void unzipTiffPages_shouldIgnoreNonTiffFiles() throws Exception {
        byte[] zip = createZipWithTiffs(List.of(
                "page-001.tif", "TIFF1",
                "readme.txt", "ignore me",
                "page-002.tif", "TIFF2"
        ));

        List<TiffPage> pages = migrationService.unzipTiffPages(zip);

        assertThat(pages).hasSize(2);
        assertThat(pages.get(1).name()).isEqualTo("page-002.tif");
    }

    @Test
    void unzipTiffPages_shouldThrowWhenNoTiffs() throws Exception {
        byte[] zip = createZipWithTiffs(List.of(
                "readme.txt", "no tiffs here"
        ));

        assertThatThrownBy(() -> migrationService.unzipTiffPages(zip))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No TIFF pages found in ZIP");
    }

    // ────────────────────────────────────────────────
    // createChainZip
    // ────────────────────────────────────────────────
    @Test
    void createChainZip_shouldProduceValidZipWithManifest() throws Exception {
        // Arrange - ensure meta is non-null and has values
        assertThat(meta).isNotNull();
        assertThat(meta.getTitle()).isNotNull(); // fail fast if setup broken

        List<TiffPage> pages = List.of(
                new TiffPage("sample1.tiff", Files.readAllBytes(Path.of("src/test/resources/tiffs/sample1.tiff"))),
                new TiffPage("sample2.tiff", Files.readAllBytes(Path.of("src/test/resources/tiffs/sample2.tiff")))
        );
        ctx.addPageHash("sample1.tiff", "hash-page1");
        ctx.addPageHash("sample2.tiff", "hash-page2");

        // Act
        Path zip = migrationService.createChainZip("DOC-TEST-001", pages, meta, ctx);

        assertThat(Files.exists(zip)).isTrue();

        // Verify ZIP content
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zip))) {
            ZipEntry entry;
            int tiffCount = 0;
            String manifestContent = null;

            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().startsWith("page-")) {
                    tiffCount++;
                    assertThat(zis.readAllBytes()).hasSizeGreaterThan(5);
                } else if ("manifest.json".equals(entry.getName())) {
                    manifestContent = new String(zis.readAllBytes());
                }
            }

            assertThat(tiffCount).isEqualTo(2);
            assertThat(manifestContent).isNotNull();
            String expectedManifestContent = """
                    {"docId":"DOC-TEST-001","pageCount":2,"pageHashes":{"sample1.tiff"\
                    :"hash-page1"},"payloadHash":"payload-sha256-abc123","sourceMetadata":\
                    {"docId":"DOC-TEST-001","title":"Test Invoice 2026","clientId":"CHE-123.456.789"}}""";
            JSONAssert.assertEquals(expectedManifestContent, manifestContent, JSONCompareMode.LENIENT);
        }
    }

    @Test
    void createChainZip_shouldFailOnNullPages() {
        assertThatThrownBy(() -> migrationService.createChainZip("DOC-001", null, meta, ctx))
                .isInstanceOf(NullPointerException.class);
    }

    // ────────────────────────────────────────────────
    // buildXml
    // ────────────────────────────────────────────────
    @Test
    void buildXml_shouldFillAllRelevantFields() {
        ctx.setZipHash("zip-xyz789");
        ctx.setPdfHash("pdf-abc456");
        ctx.addPageHash("p1.tif", "h1");
        ctx.addPageHash("p2.tif", "h2");

        ArchivalMetadata xml = migrationService.buildXml(meta, ctx);

        assertThat(xml.getDocumentId()).isEqualTo("DOC-TEST-001");
        assertThat(xml.getTitle()).isEqualTo("Test Invoice 2026");
        assertThat(xml.getPageCount()).isEqualTo(2);
        assertThat(xml.getPayloadHash()).isEqualTo("payload-sha256-abc123");
        assertThat(xml.getZipHash()).isEqualTo("zip-xyz789");
        assertThat(xml.getPdfHash()).isEqualTo("pdf-abc456");

        assertThat(xml.getProvenance()).isNotNull();
        assertThat(xml.getProvenance().getMigrationTimestamp()).matches("\\d{4}-\\d{2}-\\d{2}T.*Z");
        assertThat(xml.getProvenance().getPageHashes()).hasSize(2);
    }

    @Test
    void buildXml_shouldHandleMissingMetadataGracefully() {
        SourceMetadata nullMeta = new SourceMetadata(); // all fields null

        ArchivalMetadata xml = migrationService.buildXml(nullMeta, ctx);

        assertThat(xml.getTitle()).isEqualTo("Untitled Document");
        assertThat(xml.getPageCount()).isZero();
        assertThat(xml.getProvenance()).isNotNull();
        assertThat(xml.getCustomFields()).containsEntry("sourceSystem", "legacy-archive-v1");
    }

    // ────────────────────────────────────────────────
    // mergeTiffToPdf
    // ────────────────────────────────────────────────
    @Test
    void mergeTiffToPdf_shouldCreatePdfWithCorrectPageCount() throws Exception {
        List<TiffPage> pages = List.of(
                new TiffPage("sample1.tif", Files.readAllBytes(Path.of("src/test/resources/tiffs/sample1.tiff"))),
                new TiffPage("sample2.tif", Files.readAllBytes(Path.of("src/test/resources/tiffs/sample2.tiff")))
        );

        Path pdfPath = migrationService.mergeTiffToPdf(pages, "DOC-TEST-PDF");

        assertThat(Files.exists(pdfPath)).isTrue();

        try (PDDocument doc = Loader.loadPDF(pdfPath.toFile())) {
            assertThat(doc.getNumberOfPages()).isEqualTo(2);
            PDPage page1 = doc.getPage(0);
            assertThat(page1.getMediaBox().getWidth()).isEqualTo(480);
            assertThat(page1.getMediaBox().getHeight()).isEqualTo(360);
        }
    }

    @Test
    void mergeTiffToPdf_shouldHandleEmptyList() throws Exception {
        Path pdf = migrationService.mergeTiffToPdf(List.of(), "DOC-EMPTY");

        try (PDDocument doc = Loader.loadPDF(pdf.toFile())) {
            assertThat(doc.getNumberOfPages()).isZero();
        }
    }

    // ────────────────────────────────────────────────
    // migrateDocument – happy path (partial integration)
    // ────────────────────────────────────────────────
    @Test
    void migrateDocument_happyPath_shouldCallAllSteps() throws Exception {
        byte[] fakeZipPayload = createZipWithTiffs(List.of("page001.tif", "fake-tiff-data"));

        Path fakePdf = tempDir.resolve("mocked.pdf");
        Files.createFile(fakePdf);  // or write dummy content

        doReturn(fakePdf)
                .when(migrationService)
                .mergeTiffToPdf(anyList(), anyString());

        // Mock RestClient chain (critical!)
        when(restClient.get()).thenReturn(requestSpec);

        // Metadata call
        when(requestSpec.uri("/documents/{id}/metadata", "DOC-HAPPY")).thenReturn(requestSpec);
        when(requestSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(SourceMetadata.class)).thenReturn(meta);

        // Payload call (second get)
        when(requestSpec.uri("/documents/{id}/payload", "DOC-HAPPY")).thenReturn(requestSpec);
        when(requestSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(byte[].class)).thenReturn(fakeZipPayload);

        // Act
        migrationService.migrateDocument("DOC-HAPPY");

        // Assert calls
        verify(restClient, times(2)).get();  // metadata + payload
        verify(requestSpec, times(2)).uri(anyString(), eq("DOC-HAPPY"));
        verify(requestSpec, times(2)).retrieve();
    }

    // Helper to create small test ZIP
    private byte[] createZipWithTiffs(List<String> entries) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (int i = 0; i < entries.size(); i += 2) {
                zos.putNextEntry(new ZipEntry(entries.get(i)));
                zos.write(entries.get(i + 1).getBytes());
                zos.closeEntry();
            }
        }
        return baos.toByteArray();
    }
}