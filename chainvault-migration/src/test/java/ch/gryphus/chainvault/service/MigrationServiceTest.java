/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import ch.gryphus.chainvault.config.Constants;
import ch.gryphus.chainvault.config.MigrationProperties;
import ch.gryphus.chainvault.config.SftpTargetConfig;
import ch.gryphus.chainvault.domain.ArchivalMetadata;
import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.domain.SourceMetadata;
import ch.gryphus.chainvault.domain.TiffPage;
import ch.gryphus.chainvault.utils.HashUtils;
import ch.gryphus.chainvault.utils.MigrationUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.integration.file.remote.SessionCallback;
import org.springframework.integration.file.remote.session.Session;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.client.RestClient;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

/**
 * The type Migration service test.
 */
@SuppressWarnings({"unchecked", "rawtypes", "NestedAssignment"})
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class MigrationServiceTest {

    @Mock private RestClient mockRestClient;
    @Mock private HttpRequest mockRequest;

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

    private MigrationContext migrationContext;
    private SourceMetadata meta;

    private Path workingDirectory;
    private Path resourceDirectory = Paths.get("src", "test", "resources");

    /**
     * Sets up.
     *
     * @throws Exception the exception
     */
    @BeforeEach
    void setUp() throws Exception {
        migrationServiceUnderTest =
                new MigrationService(
                        mockRestClient,
                        mockSftpRemoteFileTemplate,
                        mockSftpTargetConfig,
                        new MigrationProperties(
                                "/tmp/migration-%s".formatted(UUID.randomUUID()),
                                5000000,
                                10.0,
                                10000,
                                "eng+deu",
                                300));

        workingDirectory = Path.of(migrationServiceUnderTest.getTempDir());
        Files.createDirectory(workingDirectory);

        migrationContext = new MigrationContext();
        migrationContext.setDocId("DOC-TEST-001");

        meta = new SourceMetadata();
        meta.setDocId("DOC-TEST-001");
        meta.setTitle("Test Invoice 2026");
        meta.setCreationDate(String.valueOf(Instant.now()));
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
     * Tear down.
     *
     * @throws Exception the exception
     */
    @AfterEach
    void tearDown() throws Exception {
        FileSystemUtils.deleteRecursively(workingDirectory);
    }

    /**
     * Test extract and hash when documents exist.
     *
     * @throws Exception the exception
     */
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
                            return function.exchange(mockRequest, mockResponse);
                        });

        Map<String, Object> result = migrationServiceUnderTest.extractAndHash("DOC-TEST-001");

        assertThat(result).hasSize(3); // context + metadata + payload
        Object obj = result.get("migrationContext");
        assertThat(obj).isInstanceOf(MigrationContext.class);

        migrationContext = (MigrationContext) obj;
        assertThat(migrationContext.getMetadataHash()).isNotNull(); // metadata hash exists
        assertThat(migrationContext.getPayloadHash()).isNotNull(); // payload hash exists
    }

    /**
     * Test extract and hash when document does not exist.
     */
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
                            return function.exchange(mockRequest, mockResponse);
                        });

        String docId = "DOC-NOT-EXISTS-001";
        assertThatExceptionOfType(MigrationServiceException.class)
                .isThrownBy(() -> migrationServiceUnderTest.extractAndHash(docId))
                .withMessageContaining("Unable to find document with id: %s".formatted(docId));
    }

    /**
     * Test extract and hash when no payload url exists.
     *
     * @throws Exception the exception
     */
    @Test
    void testExtractAndHash_whenNoPayloadUrlExists() throws Exception {
        String docId = "DOC-NO-PAYLOAD-URL-001";
        meta.setPayloadUrl(null);

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
                            return function.exchange(mockRequest, mockResponse);
                        });

        Map<String, Object> result = migrationServiceUnderTest.extractAndHash(docId);
        assertThat(result).hasSize(2); // context + metadata
        Object obj = result.get("migrationContext");
        assertThat(obj).isInstanceOf(MigrationContext.class);

        migrationContext = (MigrationContext) obj;
        assertThat(migrationContext.getMetadataHash()).isNotNull(); // metadata hash exists
        assertThat(migrationContext.getPayloadHash()).isNull(); // payload hash does not exist
    }

    /**
     * Test extract and hash when payload does not exist.
     */
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
                            return function.exchange(mockRequest, mockResponse);
                        })
                .thenAnswer( // returns payload not found
                        invocation -> {
                            RestClient.RequestHeadersSpec.ExchangeFunction function =
                                    invocation.getArgument(0);
                            when(mockResponse.getStatusCode())
                                    .thenReturn(HttpStatus.NOT_FOUND); // return 404 NOT FOUND
                            return function.exchange(mockRequest, mockResponse);
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
        byte[] payload =
                Files.readAllBytes(
                        Path.of("%s/zips/valid_archive.zip".formatted(resourceDirectory)));

        // Run the test
        List<TiffPage> result =
                migrationServiceUnderTest.signTiffPages(
                        payload, migrationContext, workingDirectory);

        // Verify the results
        assertThat(result).hasSize(5);
        assertThat(result.getFirst().name()).isEqualTo(("DOC-ARCH-2025-001_001.tiff"));
        assertThat(HashUtils.sha256(result.getFirst().data()))
                .isEqualTo("a7c2d26a6c721dd9dba9cd6aec405552217c6ede0c9cf7cd5bcccca2a3d4e705");
    }

    /**
     * Test prepare sftp session when valid metadata and payload exist.
     *
     * @throws IOException the io exception
     */
    @Test
    void testPrepareSftp_Session_whenValidMetadataAndPayloadExist() throws IOException {
        // Setup
        String docId = "DOC-TEST-001";
        migrationContext.setDocId(docId);
        migrationContext.setPayloadHash("payloadHash");
        migrationContext.setZipHash("zipHash");
        migrationContext.setPdfHash("pdfHash");
        migrationContext.setPageHashes(Map.ofEntries(Map.entry("value", "value")));

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
        migrationServiceUnderTest.prepareSftpSession(
                docId,
                Files.readString(Path.of("%s/sftp/sample_meta.xml".formatted(resourceDirectory))),
                Path.of("%s/sftp/sample.zip".formatted(resourceDirectory)),
                Path.of("%s/sftp/sample.pdf".formatted(resourceDirectory)),
                "abcde");

        // Verify the results
        verify(mockSftpRemoteFileTemplate).execute(any(SessionCallback.class));
    }

    /**
     * Test prepare sftp session when only valid metadata exists.
     *
     * @throws IOException the io exception
     */
    @Test
    void testPrepareSftp_Session_whenOnlyValidMetadataExists() throws IOException {
        // Setup
        String docId = "DOC-TEST-001";
        migrationContext.setDocId(docId);
        migrationContext.setZipHash("zipHash");
        migrationContext.setPageHashes(Map.ofEntries(Map.entry("value", "value")));

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
        migrationServiceUnderTest.prepareSftpSession(
                docId,
                Files.readString(Path.of("%s/sftp/sample_meta.xml".formatted(resourceDirectory))),
                Path.of("%s/sftp/sample.zip".formatted(resourceDirectory)),
                null,
                "abcde");

        // Verify the results
        verify(mockSftpRemoteFileTemplate).execute(any(SessionCallback.class));
    }

    /**
     * Test merge tiff to pdf should generate expected result.
     *
     * @throws Exception the exception
     */
    @Test
    void testMergeTiffToPdf_shouldGenerateExpectedResult() throws Exception {
        // Setup
        List<TiffPage> pages =
                List.of(
                        new TiffPage(
                                "sample1.tiff",
                                Files.readAllBytes(
                                        Path.of(
                                                "%s/tiffs/sample1.tiff"
                                                        .formatted(resourceDirectory)))),
                        new TiffPage(
                                "sample2.tiff",
                                Files.readAllBytes(
                                        Path.of(
                                                "%s/tiffs/sample2.tiff"
                                                        .formatted(resourceDirectory)))));

        // Run the test
        Path result =
                MigrationUtils.mergeTiffToPdf(
                        pages, Constants.BPMN_PROC_VAR_DOC_ID, workingDirectory);

        // Verify the results
        assertThat(result.toFile()).exists();
        byte[] resultBytes = Files.readAllBytes(result);
        assertThat(MigrationUtils.getDetectedMimeType(resultBytes))
                .isEqualTo(MediaType.APPLICATION_PDF_VALUE);
    }

    /**
     * Test transform metadata to xml should generate expected result.
     */
    @Test
    void testTransformMetadataToXml_shouldGenerateExpectedResult() {
        // Setup
        migrationContext.setPayloadHash("payload-sha256-abc123");
        migrationContext.setZipHash("zipHash-sha256-abc123");
        migrationContext.setPdfHash("pdfHash-sha256-abc123");
        migrationContext.setPageHashes(
                Map.ofEntries(Map.entry("file1.tiff", "pageHash-sha256-abc123")));

        // Run the test
        String result =
                migrationServiceUnderTest.transformMetadataToXml(meta, migrationContext, null);

        // Verify the results
        String xmlFilename = "%s/xmls/ArchivalMetadata.xml".formatted(resourceDirectory);
        Diff diff =
                DiffBuilder.compare(Input.fromFile(xmlFilename))
                        .withTest(result)
                        .ignoreWhitespace()
                        // Ignore all nodes with timestamps
                        .withNodeFilter(
                                node ->
                                        !"creationDate".equals(node.getNodeName())
                                                && !"migrationTimestamp".equals(node.getNodeName()))
                        .build();

        assertThat(diff.hasDifferences()).isFalse();
    }

    /**
     * Test sign tiff pages should extract and preserve order.
     *
     * @throws Exception the exception
     */
    @Test
    void testSignTiffPages_shouldExtractAndPreserveOrder() throws Exception {
        byte[] zip =
                Files.readAllBytes(
                        Path.of("%s/zips/valid_archive.zip".formatted(resourceDirectory)));
        String docId = "DOC-ARCH-2025-001";

        List<TiffPage> pages =
                migrationServiceUnderTest.signTiffPages(zip, migrationContext, workingDirectory);

        assertThat(pages)
                .hasSize(5)
                .allSatisfy(
                        page -> {
                            int i = pages.indexOf(page) + 1;
                            assertThat(page.name()).isEqualTo("%s_%03d.tiff".formatted(docId, i));
                        });
    }

    /**
     * Test sign tiff pages should ignore non tiff files.
     *
     * @throws Exception the exception
     */
    @Test
    void testSignTiffPages_shouldIgnoreNonTiffFiles() throws Exception {
        byte[] zip =
                createZipWithTiffs(
                        List.of(
                                "page-001.tif", "TIFF1",
                                "readme.txt", "ignore me",
                                "page-002.tif", "TIFF2"));

        List<TiffPage> pages =
                migrationServiceUnderTest.signTiffPages(zip, migrationContext, workingDirectory);

        assertThat(pages).hasSize(2);
        assertThat(pages.get(1).name()).isEqualTo("page-002.tif");
    }

    /**
     * Test sign tiff pages should throw when no tiffs.
     *
     * @throws Exception the exception
     */
    @Test
    void testSignTiffPages_shouldThrowWhenNoTiffs() throws Exception {
        byte[] zip = createZipWithTiffs(List.of("readme.txt", "no tiffs here"));

        assertThatThrownBy(
                        () ->
                                migrationServiceUnderTest.signTiffPages(
                                        zip, migrationContext, workingDirectory))
                .isInstanceOf(MigrationServiceException.class)
                .hasMessage("No TIFF pages found in ZIP");
    }

    /**
     * Test sign tiff pages should throw exception when total size exceeded.
     *
     * @throws IOException the io exception
     */
    @Test
    void testSignTiffPages_shouldThrowException_whenTotalSizeExceeded() throws IOException {
        byte[] overLimitData =
                Files.readAllBytes(Path.of("%s/zips/over_10mb.zip".formatted(resourceDirectory)));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry("too_large.tiff");
            zos.putNextEntry(entry);
            zos.write(overLimitData);
            zos.closeEntry();
        }
        byte[] payload = baos.toByteArray();

        // 3. Execution & Verification
        assertThatThrownBy(
                        () ->
                                migrationServiceUnderTest.signTiffPages(
                                        payload, migrationContext, workingDirectory))
                .isInstanceOf(MigrationServiceException.class)
                .hasMessage(
                        "Total size of the archive is greater than the threshold %d bytes"
                                .formatted(migrationServiceUnderTest.getZipThresholdSize()));
    }

    /**
     * Test sign tiff pages should throw exception when compression ratio exceeded.
     *
     * @throws Exception the exception
     */
    @Test
    void testSignTiffPages_shouldThrowException_whenCompressionRatioExceeded() throws Exception {
        byte[] uncompressedData = new byte[2_000_000];

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry("zipBomb.txt");
            zos.putNextEntry(entry);
            zos.write(uncompressedData);
            zos.closeEntry();
        }
        byte[] payload = baos.toByteArray();
        assertThatThrownBy(
                        () ->
                                migrationServiceUnderTest.signTiffPages(
                                        payload, migrationContext, workingDirectory))
                .isInstanceOf(MigrationServiceException.class)
                .hasMessage(
                        "Ratio between compressed and uncompressed data is greater than %s"
                                .formatted(migrationServiceUnderTest.getZipThresholdRatio()));
    }

    /**
     * Test sign tiff pages should throw exception when too many entries.
     *
     * @throws Exception the exception
     */
    @Test
    void testSignTiffPages_shouldThrowException_whenTooManyEntries() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (int i = 0; i < 10001; i++) {
                zos.putNextEntry(new ZipEntry("file%d.txt".formatted(i)));
                zos.write("data".getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
        }
        byte[] payload = baos.toByteArray();

        assertThatThrownBy(
                        () ->
                                migrationServiceUnderTest.signTiffPages(
                                        payload, migrationContext, workingDirectory))
                .isInstanceOf(MigrationServiceException.class)
                .hasMessage(
                        "Number of entries in the archive is greater than %d"
                                .formatted(migrationServiceUnderTest.getZipThresholdEntries()));
    }

    /**
     * Test create chain zip should produce valid zip with manifest when metadata and payload exists.
     *
     * @throws Exception the exception
     */
    // ────────────────────────────────────────────────
    // createChainZip
    // ────────────────────────────────────────────────
    @Test
    void testCreateChainZip_shouldProduceValidZipWithManifestWhenMetadataAndPayloadExists()
            throws Exception {
        // Arrange - ensure meta is non-null and has values
        assertThat(meta).isNotNull();
        assertThat(meta.getTitle()).isNotNull(); // fail fast if setup broken
        migrationContext.setPayloadHash("payload-sha256-abc123");

        List<TiffPage> pages =
                List.of(
                        new TiffPage(
                                "sample1.tiff",
                                Files.readAllBytes(
                                        Path.of(
                                                "%s/tiffs/sample1.tiff"
                                                        .formatted(resourceDirectory)))),
                        new TiffPage(
                                "sample2.tiff",
                                Files.readAllBytes(
                                        Path.of(
                                                "%s/tiffs/sample2.tiff"
                                                        .formatted(resourceDirectory)))));
        migrationContext.addPageHash("sample1.tiff", "hash-page1");
        migrationContext.addPageHash("sample2.tiff", "hash-page2");

        // Act
        Path zip =
                migrationServiceUnderTest.prepareFiles(
                        workingDirectory, meta, migrationContext, pages);

        assertThat(Files.exists(zip)).isTrue();

        // Verify ZIP content
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zip))) {
            ZipEntry entry;
            int tiffCount = 0;
            String actualResult = null;

            while ((entry = zis.getNextEntry()) != null) {
                if ("manifest.json".equals(entry.getName())) {
                    actualResult = new String(zis.readAllBytes(), StandardCharsets.UTF_8);
                } else if (entry.getName().endsWith(".tiff") || entry.getName().endsWith(".tif")) {
                    tiffCount++;
                    assertThat(zis.readAllBytes()).hasSizeGreaterThan(5);
                }
            }

            assertThat(tiffCount).isEqualTo(2);
            assertThat(actualResult).isNotNull();
            String expectedResult =
                    """
                    {"docId":"DOC-TEST-001","pageCount":2,"pageHashes":{"sample1.tiff"\
                    :"hash-page1"},"payloadHash":"payload-sha256-abc123","sourceMetadata":\
                    {"docId":"DOC-TEST-001","title":"Test Invoice 2026","clientId":"CHE-123.456.789"}}\
                    """;
            JSONAssert.assertEquals(expectedResult, actualResult, JSONCompareMode.LENIENT);
        }
    }

    /**
     * Test create chain zip should produce valid zip with manifest when only metadata exists.
     *
     * @throws Exception the exception
     */
    @Test
    void testCreateChainZip_shouldProduceValidZipWithManifestWhenOnlyMetadataExists()
            throws Exception {
        // Arrange - ensure meta is non-null and has values
        assertThat(meta).isNotNull();
        assertThat(meta.getTitle()).isNotNull(); // fail fast if setup broken

        List<TiffPage> pages = Collections.emptyList(); // empty list

        // Act
        Path zip =
                migrationServiceUnderTest.prepareFiles(
                        workingDirectory, meta, migrationContext, pages);

        assertThat(Files.exists(zip)).isTrue();

        // Verify ZIP content
        validateContents(zip);

        zip =
                migrationServiceUnderTest.prepareFiles(
                        workingDirectory, meta, migrationContext, null);

        assertThat(Files.exists(zip)).isTrue();

        // Verify ZIP content
        validateContents(zip);
    }

    private static void validateContents(Path zip) throws IOException, JSONException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zip))) {
            ZipEntry entry;
            String actualResult = null;

            while ((entry = zis.getNextEntry()) != null) {
                if ("manifest.json".equals(entry.getName())) {
                    actualResult = new String(zis.readAllBytes(), StandardCharsets.UTF_8);
                }
            }

            assertThat(actualResult).isNotNull();
            String expectedResult =
                    """
                    {"docId":"DOC-TEST-001","sourceMetadata":\
                    {"docId":"DOC-TEST-001","title":"Test Invoice 2026","clientId":"CHE-123.456.789"}}\
                    """;
            JSONAssert.assertEquals(expectedResult, actualResult, JSONCompareMode.LENIENT);
        }
    }

    /**
     * Test build xml should fill all relevant fields.
     */
    // ────────────────────────────────────────────────
    // buildXml
    // ────────────────────────────────────────────────
    @Test
    void testBuildXml_shouldFillAllRelevantFields() {
        migrationContext.setPayloadHash("payload-sha256-abc123");
        migrationContext.setZipHash("zip-xyz789");
        migrationContext.setPdfHash("pdf-abc456");
        migrationContext.addPageHash("p1.tif", "h1");
        migrationContext.addPageHash("p2.tif", "h2");

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("ocrResults", "test");
        inputMap.put("ocrFullTextLength", 123);
        ArchivalMetadata xml = MigrationUtils.buildXml(meta, migrationContext, inputMap);

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
     * Test build xml should handle missing metadata gracefully.
     */
    @Test
    void testBuildXml_shouldHandleMissingMetadataGracefully() {
        SourceMetadata nullMeta = new SourceMetadata(); // all fields null

        ArchivalMetadata xml = MigrationUtils.buildXml(nullMeta, migrationContext, null);

        assertThat(xml.getTitle()).isEqualTo("Untitled Document");
        assertThat(xml.getPageCount()).isZero();
        assertThat(xml.getProvenance()).isNotNull();
        assertThat(xml.getCustomFields()).containsEntry("sourceSystem", "legacy-archive-v1");
    }

    /**
     * Test merge tiff to pdf should create pdf with correct page count.
     *
     * @throws Exception the exception
     */
    // ────────────────────────────────────────────────
    // mergeTiffToPdf
    // ────────────────────────────────────────────────
    @Test
    void testMergeTiffToPdf_shouldCreatePdfWithCorrectPageCount() throws Exception {
        List<TiffPage> pages =
                List.of(
                        new TiffPage(
                                "sample1.tif",
                                Files.readAllBytes(
                                        Path.of(
                                                "%s/tiffs/sample1.tiff"
                                                        .formatted(resourceDirectory)))),
                        new TiffPage(
                                "sample2.tif",
                                Files.readAllBytes(
                                        Path.of(
                                                "%s/tiffs/sample2.tiff"
                                                        .formatted(resourceDirectory)))));
        Path pdfPath = MigrationUtils.mergeTiffToPdf(pages, "DOC-TEST-PDF", workingDirectory);

        assertThat(Files.exists(pdfPath)).isTrue();

        try (PDDocument doc = Loader.loadPDF(pdfPath.toFile())) {
            assertThat(doc.getNumberOfPages()).isEqualTo(2);
            PDPage page1 = doc.getPage(0);
            assertThat(page1.getMediaBox().getWidth()).isEqualTo(480);
            assertThat(page1.getMediaBox().getHeight()).isEqualTo(360);
        }
    }

    /**
     * Test merge tiff to pdf should handle empty list.
     *
     * @throws Exception the exception
     */
    @Test
    void testMergeTiffToPdf_shouldHandleEmptyList() throws Exception {
        Path pdf = MigrationUtils.mergeTiffToPdf(List.of(), "DOC-EMPTY", workingDirectory);

        try (PDDocument doc = Loader.loadPDF(pdf.toFile())) {
            assertThat(doc.getNumberOfPages()).isZero();
        }
    }

    /**
     * Test perform ocr on tiff pages should return expected content.
     *
     * @throws Exception the exception
     */
    @Test
    void testPerformOcrOnTiffPages_shouldReturnExpectedContent() throws Exception {
        // Setup
        byte[] data =
                Files.readAllBytes(Path.of("%s/tiffs/test_ocr.tiff".formatted(resourceDirectory)));
        List<TiffPage> pages = List.of(new TiffPage("test_ocr.tiff", data));

        // Run the test
        List<String> result = migrationServiceUnderTest.performOcrOnTiffPages(pages);

        // Verify the results
        String expectedContent =
                Files.readString(
                                Path.of(
                                        "%s/tiffs/test_ocr_result.txt"
                                                .formatted(resourceDirectory)),
                                StandardCharsets.UTF_8)
                        .trim();
        assertThat(result).isEqualTo(List.of(expectedContent));
    }

    /**
     * Test perform ocr on tiff pages should not throw exception for invalid content.
     */
    @Test
    void testPerformOcrOnTiffPages_shouldNotThrowExceptionForInvalidContent() {
        // Setup
        List<TiffPage> pages =
                List.of(
                        new TiffPage(
                                "bad_sample.tiff", "contents".getBytes(StandardCharsets.UTF_8)));

        // Verify the results
        assertThatNoException()
                .isThrownBy(() -> migrationServiceUnderTest.performOcrOnTiffPages(pages));
    }

    /**
     * Test perform ocr on tiff pages should not throw exception when input is null or empty.
     */
    @Test
    void testPerformOcrOnTiffPages_shouldNotThrowExceptionWhenInputIsNullOrEmpty() {
        // check for null
        assertThatNoException()
                .isThrownBy(() -> migrationServiceUnderTest.performOcrOnTiffPages(null));

        // check for empty list
        List<TiffPage> pages = Collections.emptyList();
        assertThatNoException()
                .isThrownBy(() -> migrationServiceUnderTest.performOcrOnTiffPages(pages));
    }

    /**
     * Test perform ocr on tiff pages should not throw exception when size is too small.
     *
     * @throws Exception the exception
     */
    @Test
    void testPerformOcrOnTiffPages_shouldNotThrowExceptionWhenSizeIsTooSmall() throws Exception {
        List<TiffPage> pages =
                List.of(
                        new TiffPage(
                                "too_small_sizr.tiff",
                                Files.readAllBytes(
                                        Path.of(
                                                "%s/tiffs/too_small_size.tiff"
                                                        .formatted(resourceDirectory)))));
        assertThatNoException()
                .isThrownBy(() -> migrationServiceUnderTest.performOcrOnTiffPages(pages));
    }

    // Helper to create small test ZIP
    private static byte[] createZipWithTiffs(List<String> entries) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (int i = 0; i < entries.size(); i += 2) {
                zos.putNextEntry(new ZipEntry(entries.get(i)));
                zos.write(entries.get(i + 1).getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
        }
        return baos.toByteArray();
    }
}
