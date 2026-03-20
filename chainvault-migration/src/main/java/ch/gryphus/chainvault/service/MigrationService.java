/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.service;

import ch.gryphus.chainvault.config.MigrationProperties;
import ch.gryphus.chainvault.config.SftpTargetConfig;
import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.domain.SourceMetadata;
import ch.gryphus.chainvault.domain.TiffPage;
import ch.gryphus.chainvault.utils.HashUtils;
import ch.gryphus.chainvault.utils.MigrationUtils;
import ch.gryphus.chainvault.utils.OcrUtils;
import ch.gryphus.chainvault.utils.SftpUtils;
import ch.gryphus.chainvault.utils.SourceApiUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.io.FileUtils;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.xml.XmlMapper;

/**
 * The type Migration service.
 */
@Slf4j
@Service
public class MigrationService {

    private final RestClient restClient;
    private final SftpRemoteFileTemplate remoteFileTemplate;

    @Getter private final SftpTargetConfig sftpTargetConfig;
    private final XmlMapper xmlMapper;
    private final ObjectMapper objectMapper;

    private final MigrationContext migrationContext;
    private final MigrationProperties props;

    private final ThreadLocal<Tesseract> tesseractThreadLocal;

    /**
     * Instantiates a new Migration service.
     *
     * @param restClient         the rest client
     * @param remoteFileTemplate the remote file template
     * @param sftpTargetConfig   the sftp target config
     * @param props              the props
     */
    public MigrationService(
            RestClient restClient,
            SftpRemoteFileTemplate remoteFileTemplate,
            SftpTargetConfig sftpTargetConfig,
            MigrationProperties props) {
        this.restClient = restClient;
        this.remoteFileTemplate = remoteFileTemplate;
        this.sftpTargetConfig = sftpTargetConfig;
        this.props = props;
        xmlMapper = new XmlMapper();
        objectMapper = new ObjectMapper();

        migrationContext = new MigrationContext();

        tesseractThreadLocal =
                ThreadLocal.withInitial(
                        () -> {
                            Tesseract t = new Tesseract();
                            t.setLanguage(props.tesseractLanguage());
                            t.setVariable("user_defined_dpi", String.valueOf(props.tesseractDpi()));
                            t.setPageSegMode(3);
                            t.setOcrEngineMode(3);
                            return t;
                        });
    }

    /**
     * Gets temp dir.
     *
     * @return the temp dir
     */
    public String getTempDir() {
        return props.tempDir();
    }

    /**
     * Gets zip threshold ratio.
     *
     * @return the zip threshold ratio
     */
    double getZipThresholdRatio() {
        return props.zipThresholdRatio();
    }

    /**
     * Gets zip threshold size.
     *
     * @return the zip threshold size
     */
    long getZipThresholdSize() {
        return props.zipThresholdSize();
    }

    /**
     * Gets zip threshold entries.
     *
     * @return the zip threshold entries
     */
    int getZipThresholdEntries() {
        return props.zipThresholdEntries();
    }

    /**
     * Extract and hash map.
     *
     * @param docId the doc id
     * @return the map
     * @throws NoSuchAlgorithmException the no such algorithm exception
     */
    public Map<String, Object> extractAndHash(String docId) throws NoSuchAlgorithmException {
        Map<String, Object> map = new HashMap<>();
        byte[] payload;

        migrationContext.setDocId(docId);
        map.put("migrationContext", migrationContext);

        // get source metadata
        var meta = SourceApiUtils.getSourceMetadata(restClient, docId);
        migrationContext.setMetadataHash(HashUtils.sha256(objectMapper.writeValueAsBytes(meta)));
        map.put("meta", meta);

        // get payload url
        if (meta.getPayloadUrl() != null) {
            payload = SourceApiUtils.getPayloadBytes(restClient, docId, meta);
            migrationContext.setPayloadHash(HashUtils.sha256(payload));
            map.put("payload", payload);
        }

        return map;
    }

    /**
     * Sign source payload list.
     *
     * @param payload          the payload
     * @param migrationContext the migration context
     * @param workingDirectory the working directory
     * @return the list
     * @throws IOException              the io exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     */
    public List<TiffPage> signSourcePayload(
            byte[] payload, @NonNull MigrationContext migrationContext, Path workingDirectory)
            throws IOException, NoSuchAlgorithmException {
        List<TiffPage> pages = new ArrayList<>();

        // security hotspot fix against zip bombs
        File file =
                new File("%s/temp_%s.zip".formatted(workingDirectory, migrationContext.getDocId()));
        FileUtils.writeByteArrayToFile(file, payload);

        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            long totalSizeArchive = 0L;
            long totalEntryArchive = 0L;

            while (entries.hasMoreElements()) {
                ZipEntry entry1 = entries.nextElement();

                try (InputStream is = new BufferedInputStream(zipFile.getInputStream(entry1));
                        OutputStream os =
                                new BufferedOutputStream(
                                        new FileOutputStream(
                                                "%s/output_onlyfortesting.txt"
                                                        .formatted(workingDirectory)))) {

                    totalEntryArchive++;

                    int nBytes;
                    byte[] buffer = new byte[2048];
                    long totalSizeEntry = 0L;

                    while ((nBytes = is.read(buffer)) > 0) {
                        os.write(buffer, 0, nBytes);
                        totalSizeEntry += nBytes;
                        totalSizeArchive = totalSizeArchive + nBytes;

                        double compressionRatio =
                                (double) totalSizeEntry / entry1.getCompressedSize();
                        if (compressionRatio > getZipThresholdRatio()) {
                            throw new MigrationServiceException(
                                    "Ratio between compressed and uncompressed data is greater than %s"
                                            .formatted(getZipThresholdRatio()));
                        }
                    }
                }

                if (totalSizeArchive > getZipThresholdSize()) {
                    throw new MigrationServiceException(
                            "Total size of the archive is greater than the threshold %d bytes"
                                    .formatted(getZipThresholdSize()));
                }

                if (totalEntryArchive > getZipThresholdEntries()) {
                    throw new MigrationServiceException(
                            "Number of entries in the archive is greater than %d"
                                    .formatted(getZipThresholdEntries()));
                }
            }
        }

        // process the zip file post-zip bomb checks
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(payload))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().toLowerCase(Locale.ROOT).endsWith(".tif")
                        || entry.getName().toLowerCase(Locale.ROOT).endsWith(".tiff")) {
                    byte[] data = zis.readAllBytes();

                    String pageHash = HashUtils.sha256(data);
                    migrationContext.addPageHash(entry.getName(), pageHash);
                    pages.add(new TiffPage(entry.getName(), data));
                }
            }
        }

        if (pages.isEmpty()) {
            throw new MigrationServiceException("No TIFF pages found in ZIP");
        }

        return pages;
    }

    /**
     * Prepare chain zip path.
     *
     * @param workingDirectory the working directory
     * @param sourceMetadata   the source metadata
     * @param migrationContext the migration context
     * @param pages            the pages
     * @return the path
     * @throws IOException              the io exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     */
    public Path prepareChainZip(
            Path workingDirectory,
            @NonNull SourceMetadata sourceMetadata,
            @NonNull MigrationContext migrationContext,
            List<TiffPage> pages)
            throws IOException, NoSuchAlgorithmException {

        String docId = sourceMetadata.getDocId();
        Path zipPath = new File("%s/%s_chain.zip".formatted(workingDirectory, docId)).toPath();

        MigrationUtils.createChainZipFile(sourceMetadata, migrationContext, pages, zipPath);

        String zipHash = HashUtils.sha256(zipPath);
        log.info("Chain ZIP created: {} | hash = {}", zipPath.getFileName(), zipHash);

        return zipPath;
    }

    /**
     * Perform ocr list.
     *
     * @param pages the pages
     * @return the list
     * @throws IOException        the io exception
     * @throws TesseractException the tesseract exception
     */
    public List<String> performOcr(List<TiffPage> pages) throws IOException, TesseractException {
        Tesseract tesseract = tesseractThreadLocal.get(); // per-thread singleton
        List<String> results = OcrUtils.getOcrResults(pages, tesseract);
        tesseractThreadLocal.remove();
        return results;
    }

    /**
     * Create merged pdf path.
     *
     * @param pages            the pages
     * @param docId            the doc id
     * @param workingDirectory the working directory
     * @return the path
     * @throws IOException the io exception
     */
    public Path createMergedPdf(List<TiffPage> pages, String docId, Path workingDirectory)
            throws IOException {
        return MigrationUtils.mergePagesToPdf(pages, docId, workingDirectory);
    }

    /**
     * Transform metadata to xml string.
     *
     * @param sourceMetadata   the source metadata
     * @param migrationContext the migration context
     * @param map              the map
     * @return the string
     */
    public String transformMetadataToXml(
            @NonNull SourceMetadata sourceMetadata,
            @NonNull MigrationContext migrationContext,
            Map<String, Object> map) {

        var archivalMetadata = MigrationUtils.buildXml(sourceMetadata, migrationContext, map);
        return xmlMapper
                .rebuild()
                .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .build()
                .writeValueAsString(archivalMetadata);
    }

    /**
     * Create sftp upload target string.
     *
     * @param docId             the doc id
     * @param xml               the xml
     * @param zipPath           the zip path
     * @param pdfPath           the pdf path
     * @param processInstanceId the process instance id
     * @return the string
     */
    public String createSftpUploadTarget(
            String docId, String xml, Path zipPath, Path pdfPath, String processInstanceId) {

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("docId", docId);
        inputMap.put("xml", xml);
        inputMap.put("zipPath", zipPath);
        inputMap.put("pdfPath", pdfPath);
        inputMap.put("processInstanceId", processInstanceId);

        SftpUtils.executeSftpCommands(
                sftpTargetConfig.getRemoteDirectory(), remoteFileTemplate, inputMap);
        log.info(
                "Done {} | zipHash={} | pdfHash={}",
                docId,
                migrationContext.getZipHash(),
                migrationContext.getPdfHash());

        return "%s/%s-%s"
                .formatted(sftpTargetConfig.getRemoteDirectory(), docId, processInstanceId);
    }
}
