/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import ch.gryphus.chainvault.config.SftpTargetConfig;
import ch.gryphus.chainvault.domain.ArchivalMetadata;
import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.domain.SourceMetadata;
import ch.gryphus.chainvault.domain.TiffPage;
import ch.gryphus.chainvault.utils.HashUtils;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.input.BrokenInputStream;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.tika.Tika;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.http.HttpStatus;
import org.springframework.integration.file.remote.SessionCallback;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.web.client.RestClient;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.xml.XmlMapper;

/**
 * The type Migration service test.
 */
@SuppressWarnings({"rawtypes"})
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class MigrationServiceTest {

    @Mock private RestClient mockRestClient;

    @Mock
    @SuppressWarnings("rawtypes")
    private RestClient.RequestHeadersUriSpec mockRequestHeadersUriSpec;

    @Mock
    @SuppressWarnings("rawtypes")
    private RestClient.RequestHeadersSpec mockRequestHeadersSpec;

    @Mock private RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse mockResponse;

    @Mock private SftpRemoteFileTemplate mockSftpRemoteFileTemplate;
    @Mock private SftpTargetConfig mockSftpTargetConfig;
    @Mock private Session mockSession;

    private MigrationService migrationServiceUnderTest;

    private MigrationContext ctx;
    private SourceMetadata meta;

    private int zipThresholdSize;
    private double zipThresholdRatio;
    private int zipThresholdEntries;

    /**
     * Sets up.
     */
    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        String tempDir = "/tmp";
        zipThresholdSize = 5000000; // 5MB for tests
        zipThresholdRatio = 10.0;
        zipThresholdEntries = 10000;

        migrationServiceUnderTest =
                new MigrationService(
                        mockRestClient,
                        mockSftpRemoteFileTemplate,
                        mockSftpTargetConfig,
                        new XmlMapper(),
                        new ObjectMapper(),
                        new Tika(),
                        tempDir,
                        zipThresholdSize,
                        zipThresholdRatio,
                        zipThresholdEntries);

        String docId = "DOC-TEST-001";
        ctx = new MigrationContext();
        ctx.setDocId(docId);

        meta = new SourceMetadata();
        meta.setDocId(docId);
        meta.setTitle("Test Invoice 2026");
        meta.setCreationDate(Instant.now().toString());
        meta.setClientId("CHE-123.456.789");
        meta.setDocumentType("INVOICE");
        meta.setAccountNo("ACC-123");
        meta.setPayloadUrl("/payload/12345.zip");

        when(mockRestClient.get()).thenReturn(mockRequestHeadersUriSpec);
        when(mockRequestHeadersUriSpec.uri(anyString(), any(Object[].class)))
                .thenReturn(mockRequestHeadersSpec);
        when(mockRequestHeadersSpec.accept(any())).thenReturn(mockRequestHeadersSpec);
    }

    /**
     * Test extract and hash when documents exist.
     *
     * @throws Exception the exception
     */
    @SuppressWarnings("unchecked")
    @Test
    void testExtractAndHash_whenDocumentsExist() throws Exception {
        // Setup
        when(mockRequestHeadersSpec.exchange(
                        any(RestClient.RequestHeadersSpec.ExchangeFunction.class)))
                .thenAnswer(
                        invocation -> {
                            RestClient.RequestHeadersSpec.ExchangeFunction function =
                                    invocation.getArgument(0);

                            when(mockResponse.getStatusCode())
                                    .thenReturn(HttpStatus.OK); // return 200 OK
                            when(mockResponse.bodyTo(SourceMetadata.class))
                                    .thenReturn(meta); // return valid metadata
                            when(mockResponse.bodyTo(byte[].class))
                                    .thenReturn(new byte[] {}); // return valid payload

                            // Execute the lambda manually
                            return function.exchange(null, mockResponse);
                        });

        final Map<String, Object> result = migrationServiceUnderTest.extractAndHash("DOC-TEST-001");

        assertThat(result).hasSize(3); // context + metadata + payload
        Object obj = result.get("ctx");
        assertThat(obj).isInstanceOf(MigrationContext.class);

        MigrationContext migrationContext = (MigrationContext) obj;
        assertThat(migrationContext.getMetadataHash()).isNotNull(); // metadata hash exists
        assertThat(migrationContext.getPayloadHash()).isNotNull(); // payload hash exists
    }

    /**
     * Test extract and hash when document does not exist.
     */
    @SuppressWarnings("unchecked")
    @Test
    void testExtractAndHash_whenDocumentDoesNotExist() {
        when(mockRequestHeadersSpec.exchange(
                        any(RestClient.RequestHeadersSpec.ExchangeFunction.class)))
                .thenAnswer(
                        invocation -> {
                            RestClient.RequestHeadersSpec.ExchangeFunction function =
                                    invocation.getArgument(0);

                            when(mockResponse.getStatusCode())
                                    .thenReturn(HttpStatus.NOT_FOUND); // return 404 error

                            // Execute the lambda manually
                            return function.exchange(null, mockResponse);
                        });

        String docId = "DOC-NOT-EXISTS-001";
        assertThatExceptionOfType(MigrationServiceException.class)
                .isThrownBy(() -> migrationServiceUnderTest.extractAndHash(docId))
                .withMessageContaining("Unable to find document with id: %s".formatted(docId));
    }

    /**
     * Test extract and hash when payload does not exist.
     */
    @SuppressWarnings("unchecked")
    @Test
    void testExtractAndHash_whenPayloadDoesNotExist() {
        String docId = "DOC-NO-PAYLOAD-002";

        // setup
        when(mockRequestHeadersSpec.exchange(
                        any(RestClient.RequestHeadersSpec.ExchangeFunction.class)))
                .thenAnswer( // returns valid metadata
                        invocation -> {
                            RestClient.RequestHeadersSpec.ExchangeFunction function =
                                    invocation.getArgument(0);

                            when(mockResponse.getStatusCode())
                                    .thenReturn(HttpStatus.OK); // return 200 OK
                            when(mockResponse.bodyTo(SourceMetadata.class))
                                    .thenReturn(meta); // return valid metadata
                            return function.exchange(null, mockResponse);
                        })
                .thenAnswer( // returns payload not found
                        invocation -> {
                            RestClient.RequestHeadersSpec.ExchangeFunction function =
                                    invocation.getArgument(0);
                            when(mockResponse.getStatusCode())
                                    .thenReturn(HttpStatus.NOT_FOUND); // return 404 NOT FOUND
                            return function.exchange(null, mockResponse);
                        });

        assertThatExceptionOfType(MigrationServiceException.class)
                .isThrownBy(() -> migrationServiceUnderTest.extractAndHash(docId))
                .withMessageContaining("Unable to find payload for document with id: " + docId);
    }

    /**
     * Test sign tiff page with valid payload zip.
     *
     * @throws Exception the exception
     */
    @Test
    void testSignTiffPage_withValidPayloadZip() throws Exception {
        // Setup
        byte[] payload = Files.readAllBytes(Path.of("src/test/resources/zips/valid_archive.zip"));

        // Run the test
        final List<TiffPage> result = migrationServiceUnderTest.signTiffPages(payload, ctx);

        // Verify the results
        assertThat(result).hasSize(5);
        assertThat(result.getFirst().name()).isEqualTo(("DOC-ARCH-2025-001_001.tiff"));
        assertThat(HashUtils.sha256(result.getFirst().data()))
                .isEqualTo("a7c2d26a6c721dd9dba9cd6aec405552217c6ede0c9cf7cd5bcccca2a3d4e705");
    }

    /**
     * Test upload to sftp.
     *
     * @throws IOException the io exception
     */
    @SuppressWarnings("unchecked")
    @Test
    void testUploadToSftp() throws IOException {
        // Setup
        String docId = "DOC-TEST-001";
        ctx.setDocId(docId);
        ctx.setPayloadHash("payloadHash");
        ctx.setZipHash("zipHash");
        ctx.setPdfHash("pdfHash");
        ctx.setPageHashes(Map.ofEntries(Map.entry("value", "value")));

        when(mockSftpTargetConfig.getRemoteDirectory()).thenReturn("result");
        when(mockSftpRemoteFileTemplate.execute(any(SessionCallback.class)))
                .thenAnswer(
                        invocation -> {
                            SessionCallback<Session, SessionCallback> sessionCallback =
                                    invocation.getArgument(0);
                            when(mockSession.exists(anyString())).thenReturn(false);
                            when(mockSession.mkdir(anyString())).thenReturn(true);
                            doNothing()
                                    .when(mockSession)
                                    .write(any(InputStream.class), anyString());
                            return sessionCallback.doInSession(mockSession);
                        });

        // Run the test
        migrationServiceUnderTest.uploadToSftp(
                ctx,
                docId,
                Files.readString(Path.of("src/test/resources/sftp/sample_meta.xml")),
                Path.of("src/test/resources/sftp/sample.zip"),
                Path.of("src/test/resources/sftp/sample.pdf"));

        // Verify the results
        verify(mockSftpRemoteFileTemplate).execute(any(SessionCallback.class));
    }

    /**
     * Test merge tiff to pdf happy path.
     *
     * @throws Exception the exception
     */
    @Test
    void testMergeTiffToPdf_HappyPath() throws Exception {
        // Setup
        final List<TiffPage> pages =
                List.of(
                        new TiffPage(
                                "sample1.tiff",
                                Files.readAllBytes(
                                        Path.of("src/test/resources/tiffs/sample1.tiff"))),
                        new TiffPage(
                                "sample2.tiff",
                                Files.readAllBytes(
                                        Path.of("src/test/resources/tiffs/sample2.tiff"))));

        // Run the test
        final Path result = migrationServiceUnderTest.mergeTiffToPdf(pages, "docId");

        // Verify the results
        assertThat(result.toFile()).exists();
        byte[] resultBytes = Files.readAllBytes(result);
        assertThat(migrationServiceUnderTest.getDetectedMimeType(resultBytes))
                .isEqualTo("application/pdf");
    }

    /**
     * Test transform metadata to xml.
     */
    @Test
    void testTransformMetadataToXml() {
        // Setup
        meta.setDocId("docId");
        meta.setTitle("title");
        meta.setCreationDate("creationDate");
        meta.setClientId("clientId");
        meta.setDocumentType("documentType");
        meta.setPayloadUrl("payloadUrl");

        ctx.setDocId("docId");
        ctx.setPayloadHash("payloadHash");
        ctx.setZipHash("zipHash");
        ctx.setPdfHash("pdfHash");
        ctx.setPageHashes(Map.ofEntries(Map.entry("value", "value")));

        // Run the test
        final String result = migrationServiceUnderTest.transformMetadataToXml(meta, ctx);

        // Verify the results
        String xmlFilename = "src/test/resources/xmls/ArchivalMetadata.xml";
        Diff diff =
                DiffBuilder.compare(Input.fromFile(xmlFilename))
                        .withTest(result)
                        .ignoreWhitespace()
                        // Ignore all nodes with 'Date' name
                        .withNodeFilter(node -> !"migrationTimestamp".equals(node.getNodeName()))
                        .build();

        assertThat(diff.hasDifferences()).isFalse();
    }

    /**
     * Test get detected mime type.
     *
     * @throws Exception the exception
     */
    @Test
    void testGetDetectedMimeType() throws Exception {
        // Setup
        final InputStream in = new ByteArrayInputStream("content".getBytes());

        // Run the test
        final String result = migrationServiceUnderTest.getDetectedMimeType(in);

        // Verify the results
        assertThat(result).isEqualTo("text/plain");
    }

    /**
     * Test get detected mime type empty in.
     *
     * @throws Exception the exception
     */
    @Test
    void testGetDetectedMimeType_EmptyIn() throws Exception {
        // Setup
        final InputStream in = InputStream.nullInputStream();

        // Run the test
        final String result = migrationServiceUnderTest.getDetectedMimeType(in);

        // Verify the results
        assertThat(result).isEqualTo("application/octet-stream");
    }

    /**
     * Test get detected mime type broken in.
     */
    @Test
    void testGetDetectedMimeType_BrokenIn() {
        // Setup
        final InputStream in = new BrokenInputStream();

        // Run the test
        assertThatThrownBy(() -> migrationServiceUnderTest.getDetectedMimeType(in))
                .isInstanceOf(IOException.class);
    }

    /**
     * Sign tiff pages should extract and preserve order.
     *
     * @throws Exception the exception
     */
    @Test
    void signTiffPages_shouldExtractAndPreserveOrder() throws Exception {
        byte[] zip = Files.readAllBytes(Path.of("src/test/resources/zips/valid_archive.zip"));
        String docId = "DOC-ARCH-2025-001";

        List<TiffPage> pages = migrationServiceUnderTest.signTiffPages(zip, ctx);

        assertThat(pages)
                .hasSize(5)
                .allSatisfy(
                        page -> {
                            int i = pages.indexOf(page) + 1;
                            assertThat(page.name()).isEqualTo((docId + "_%03d.tiff").formatted(i));
                        });
    }

    /**
     * Sign tiff pages should ignore non tiff files.
     *
     * @throws Exception the exception
     */
    @Test
    void signTiffPages_shouldIgnoreNonTiffFiles() throws Exception {
        byte[] zip =
                createZipWithTiffs(
                        List.of(
                                "page-001.tif", "TIFF1",
                                "readme.txt", "ignore me",
                                "page-002.tif", "TIFF2"));

        List<TiffPage> pages = migrationServiceUnderTest.signTiffPages(zip, ctx);

        assertThat(pages).hasSize(2);
        assertThat(pages.get(1).name()).isEqualTo("page-002.tif");
    }

    /**
     * Sign tiff pages should throw when no tiffs.
     *
     * @throws Exception the exception
     */
    @Test
    void signTiffPages_shouldThrowWhenNoTiffs() throws Exception {
        byte[] zip = createZipWithTiffs(List.of("readme.txt", "no tiffs here"));

        assertThatThrownBy(() -> migrationServiceUnderTest.signTiffPages(zip, ctx))
                .isInstanceOf(MigrationServiceException.class)
                .hasMessage("No TIFF pages found in ZIP");
    }

    @Test
    void signTiffPages_shouldThrowException_whenTotalSizeExceeded() throws IOException {
        byte[] overLimitData = Files.readAllBytes(Path.of("src/test/resources/zips/over_10mb.zip"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry("too_large.tiff");
            zos.putNextEntry(entry);
            zos.write(overLimitData);
            zos.closeEntry();
        }
        byte[] payload = baos.toByteArray();

        // 3. Execution & Verification
        assertThatThrownBy(() -> migrationServiceUnderTest.signTiffPages(payload, ctx))
                .isInstanceOf(MigrationServiceException.class)
                .hasMessage(
                        "Total size of the archive is greater than the threshold %d bytes"
                                .formatted(zipThresholdSize));
    }

    /**
     * Sign tiff pages should throw exception when compression ratio exceeded.
     *
     * @throws Exception the exception
     */
    @Test
    void signTiffPages_shouldThrowException_whenCompressionRatioExceeded() throws Exception {
        byte[] uncompressedData = new byte[2_000_000];

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry("zipBomb.txt");
            zos.putNextEntry(entry);
            zos.write(uncompressedData);
            zos.closeEntry();
        }
        byte[] payload = baos.toByteArray();
        assertThatThrownBy(() -> migrationServiceUnderTest.signTiffPages(payload, ctx))
                .isInstanceOf(MigrationServiceException.class)
                .hasMessage(
                        "Ratio between compressed and uncompressed data is greater than %s"
                                .formatted(zipThresholdRatio));
    }

    /**
     * Sign tiff pages should throw exception when too many entries.
     *
     * @throws Exception the exception
     */
    @Test
    void signTiffPages_shouldThrowException_whenTooManyEntries() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (int i = 0; i < 10001; i++) {
                zos.putNextEntry(new ZipEntry("file" + i + ".txt"));
                zos.write("data".getBytes());
                zos.closeEntry();
            }
        }
        byte[] payload = baos.toByteArray();

        assertThatThrownBy(() -> migrationServiceUnderTest.signTiffPages(payload, ctx))
                .isInstanceOf(MigrationServiceException.class)
                .hasMessage(
                        "Number of entries in the archive is greater than %d"
                                .formatted(zipThresholdEntries));
    }

    /**
     * Create chain zip should produce valid zip with manifest.
     *
     * @throws Exception the exception
     */
    // ────────────────────────────────────────────────
    // createChainZip
    // ────────────────────────────────────────────────
    @Test
    void createChainZip_shouldProduceValidZipWithManifest() throws Exception {
        // Arrange - ensure meta is non-null and has values
        assertThat(meta).isNotNull();
        assertThat(meta.getTitle()).isNotNull(); // fail fast if setup broken
        ctx.setPayloadHash("payload-sha256-abc123");

        List<TiffPage> pages =
                List.of(
                        new TiffPage(
                                "sample1.tiff",
                                Files.readAllBytes(
                                        Path.of("src/test/resources/tiffs/sample1.tiff"))),
                        new TiffPage(
                                "sample2.tiff",
                                Files.readAllBytes(
                                        Path.of("src/test/resources/tiffs/sample2.tiff"))));
        ctx.addPageHash("sample1.tiff", "hash-page1");
        ctx.addPageHash("sample2.tiff", "hash-page2");

        // Act
        Path zip = migrationServiceUnderTest.createChainZip("DOC-TEST-001", pages, meta, ctx);

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
            String expectedManifestContent =
                    """
                    {"docId":"DOC-TEST-001","pageCount":2,"pageHashes":{"sample1.tiff"\
                    :"hash-page1"},"payloadHash":"payload-sha256-abc123","sourceMetadata":\
                    {"docId":"DOC-TEST-001","title":"Test Invoice 2026","clientId":"CHE-123.456.789"}}\
                    """;
            JSONAssert.assertEquals(
                    expectedManifestContent, manifestContent, JSONCompareMode.LENIENT);
        }
    }

    /**
     * Create chain zip should fail on null pages.
     */
    @Test
    void createChainZip_shouldFailOnNullPages() {
        assertThatThrownBy(
                        () -> migrationServiceUnderTest.createChainZip("DOC-001", null, meta, ctx))
                .isInstanceOf(NullPointerException.class);
    }

    /**
     * Build xml should fill all relevant fields.
     */
    // ────────────────────────────────────────────────
    // buildXml
    // ────────────────────────────────────────────────
    @Test
    void buildXml_shouldFillAllRelevantFields() {
        ctx.setPayloadHash("payload-sha256-abc123");
        ctx.setZipHash("zip-xyz789");
        ctx.setPdfHash("pdf-abc456");
        ctx.addPageHash("p1.tif", "h1");
        ctx.addPageHash("p2.tif", "h2");

        ArchivalMetadata xml = migrationServiceUnderTest.buildXml(meta, ctx);

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

    /**
     * Build xml should handle missing metadata gracefully.
     */
    @Test
    void buildXml_shouldHandleMissingMetadataGracefully() {
        SourceMetadata nullMeta = new SourceMetadata(); // all fields null

        ArchivalMetadata xml = migrationServiceUnderTest.buildXml(nullMeta, ctx);

        assertThat(xml.getTitle()).isEqualTo("Untitled Document");
        assertThat(xml.getPageCount()).isZero();
        assertThat(xml.getProvenance()).isNotNull();
        assertThat(xml.getCustomFields()).containsEntry("sourceSystem", "legacy-archive-v1");
    }

    /**
     * Merge tiff to pdf should create pdf with correct page count.
     *
     * @throws Exception the exception
     */
    // ────────────────────────────────────────────────
    // mergeTiffToPdf
    // ────────────────────────────────────────────────
    @Test
    void mergeTiffToPdf_shouldCreatePdfWithCorrectPageCount() throws Exception {
        List<TiffPage> pages =
                List.of(
                        new TiffPage(
                                "sample1.tif",
                                Files.readAllBytes(
                                        Path.of("src/test/resources/tiffs/sample1.tiff"))),
                        new TiffPage(
                                "sample2.tif",
                                Files.readAllBytes(
                                        Path.of("src/test/resources/tiffs/sample2.tiff"))));
        Path pdfPath = migrationServiceUnderTest.mergeTiffToPdf(pages, "DOC-TEST-PDF");

        assertThat(Files.exists(pdfPath)).isTrue();

        try (PDDocument doc = Loader.loadPDF(pdfPath.toFile())) {
            assertThat(doc.getNumberOfPages()).isEqualTo(2);
            PDPage page1 = doc.getPage(0);
            assertThat(page1.getMediaBox().getWidth()).isEqualTo(480);
            assertThat(page1.getMediaBox().getHeight()).isEqualTo(360);
        }
    }

    /**
     * Merge tiff to pdf should handle empty list.
     *
     * @throws Exception the exception
     */
    @Test
    void mergeTiffToPdf_shouldHandleEmptyList() throws Exception {
        Path pdf = migrationServiceUnderTest.mergeTiffToPdf(List.of(), "DOC-EMPTY");

        try (PDDocument doc = Loader.loadPDF(pdf.toFile())) {
            assertThat(doc.getNumberOfPages()).isZero();
        }
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
