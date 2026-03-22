/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.utils;

import ch.gryphus.chainvault.config.Constants;
import ch.gryphus.chainvault.config.MigrationProperties;
import ch.gryphus.chainvault.domain.ArchivalMetadata;
import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.domain.MigrationProvenance;
import ch.gryphus.chainvault.domain.OcrPage;
import ch.gryphus.chainvault.domain.SourceMetadata;
import ch.gryphus.chainvault.service.MigrationServiceException;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.tika.Tika;
import tools.jackson.databind.ObjectMapper;

/**
 * The type Migration utils.
 */
@Slf4j
public final class MigrationUtils {
    private MigrationUtils() {
        // empty constructor
    }

    private static final Tika tika = new Tika();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Gets detected mime type.
     *
     * @param in the in
     * @return the detected mime type
     * @throws IOException the io exception
     */
    public static String getDetectedMimeType(InputStream in) throws IOException {
        return tika.detect(in);
    }

    /**
     * Gets detected mime type.
     *
     * @param bytes the bytes
     * @return the detected mime type
     */
    public static String getDetectedMimeType(byte[] bytes) {
        return tika.detect(bytes);
    }

    /**
     * Generate signed payload list.
     *
     * @param payload          the payload
     * @param migrationContext the migration context
     * @param workingDirectory the working directory
     * @param props            the props
     * @return the list
     * @throws IOException              the io exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     */
    public static @NonNull List<OcrPage> generateSignedPayload(
            byte[] payload,
            @NonNull MigrationContext migrationContext,
            Path workingDirectory,
            MigrationProperties props)
            throws IOException, NoSuchAlgorithmException {
        List<OcrPage> pages = new ArrayList<>();

        // security hotspot fix against zip bombs
        File file =
                new File("%s/temp_%s.zip".formatted(workingDirectory, migrationContext.getDocId()));
        FileUtils.writeByteArrayToFile(file, payload);

        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            long totalSizeArchive = 0L;
            long totalEntryArchive = 0L;

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

                try (InputStream is = new BufferedInputStream(zipFile.getInputStream(entry));
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
                                (double) totalSizeEntry / entry.getCompressedSize();
                        if (compressionRatio > props.zipThresholdRatio()) {
                            throw new MigrationServiceException(
                                    "Ratio between compressed and uncompressed data is greater than %s"
                                            .formatted(props.zipThresholdRatio()));
                        }
                    }
                }

                if (totalSizeArchive > props.zipThresholdSize()) {
                    throw new MigrationServiceException(
                            "Total size of the archive is greater than the threshold %d bytes"
                                    .formatted(props.zipThresholdSize()));
                }

                if (totalEntryArchive > props.zipThresholdEntries()) {
                    throw new MigrationServiceException(
                            "Number of entries in the archive is greater than %d"
                                    .formatted(props.zipThresholdEntries()));
                }
            }
        }

        // process the zip file post-zip bomb checks
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(payload))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();

                byte[] data = zis.readAllBytes();
                String mimeType = getDetectedMimeType(data);

                switch (mimeType) {
                    case "application/pdf" -> {
                        // Extract PDF pages as individual PNG images
                        List<OcrPage> pdfPages = extractPdfPages(data, entryName);
                        pages.addAll(pdfPages);

                        String pdfHash = HashUtils.sha256(data);
                        migrationContext.addPageHash(entryName, pdfHash);
                    }
                    case "image/tiff", "image/png", "image/jpeg", "image/bmp" -> {
                        pages.add(new OcrPage(entryName, data, mimeType, null));

                        String pageHash = HashUtils.sha256(data);
                        migrationContext.addPageHash(entryName, pageHash);
                    }
                    case null, default -> {
                        // do nothing
                    }
                }
            }
        }
        return pages;
    }

    private static List<OcrPage> extractPdfPages(byte[] pdfBytes, String originalName) {
        List<OcrPage> pdfPages = new ArrayList<>();

        try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
            PDFRenderer renderer = new PDFRenderer(doc);

            for (int pageNum = 0; pageNum < doc.getNumberOfPages(); pageNum++) {
                BufferedImage image = renderer.renderImageWithDPI(pageNum, 300, ImageType.RGB);

                // Convert rendered page to PNG bytes (Tesseract-friendly)
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "png", baos);
                byte[] pngData = baos.toByteArray();

                String pageName = "%s_page%03d.png".formatted(originalName, pageNum + 1);
                pdfPages.add(new OcrPage(pageName, pngData, "image/png", null));
            }
        } catch (IOException e) {
            throw new MigrationServiceException(
                    "Error extracting pages from PDF file: %s, caused by: %s"
                            .formatted(originalName, e));
        }

        return pdfPages;
    }

    /**
     * Merge pages to pdf path.
     *
     * @param pages            the pages
     * @param docId            the doc id
     * @param workingDirectory the working directory
     * @return the path
     * @throws IOException the io exception
     */
    public static Path mergePagesToPdf(
            List<? extends OcrPage> pages, String docId, Path workingDirectory) throws IOException {
        Path pdf = Path.of("%s/%s-merged.pdf".formatted(workingDirectory, docId));
        try (var doc = new PDDocument()) {
            for (var page : pages) {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(page.getData()));
                var pdImage = LosslessFactory.createFromImage(doc, img);
                var pdPage = new PDPage(new PDRectangle(img.getWidth(), img.getHeight()));
                doc.addPage(pdPage);

                try (var cs = new PDPageContentStream(doc, pdPage)) {
                    cs.drawImage(pdImage, 0, 0);
                }
            }
            doc.save(pdf.toFile());
        }
        return pdf;
    }

    /**
     * Create chain zip file.
     *
     * @param sourceMetadata   the source metadata
     * @param migrationContext the migration context
     * @param pages            the pages
     * @param zipPath          the zip path
     * @throws IOException the io exception
     */
    public static void createChainZipFile(
            @NonNull SourceMetadata sourceMetadata,
            @NonNull MigrationContext migrationContext,
            List<? extends OcrPage> pages,
            Path zipPath)
            throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            Map<String, Object> manifest = new LinkedHashMap<>();
            String docId = sourceMetadata.getDocId();
            manifest.put(Constants.BPMN_PROC_VAR_DOC_ID, docId);

            if (pages != null && !pages.isEmpty()) {
                for (OcrPage page : pages) {
                    String entryName = "%s".formatted(page.getName());

                    zos.putNextEntry(new ZipEntry(entryName));
                    zos.write(page.getData());
                    zos.closeEntry();
                }

                manifest.put("pageCount", pages.size());
                manifest.put("pageHashes", migrationContext.getPageHashes());
                manifest.put("payloadHash", migrationContext.getPayloadHash());
            }

            manifest.put("timestamp", Instant.now().toString());

            manifest.put(
                    "sourceMetadata",
                    Map.of(
                            Constants.BPMN_PROC_VAR_DOC_ID,
                            docId,
                            "title",
                            sourceMetadata.getTitle(),
                            "creationDate",
                            sourceMetadata.getCreationDate(),
                            "clientId",
                            sourceMetadata.getClientId()));

            String manifestJson =
                    objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(manifest);

            zos.putNextEntry(new ZipEntry("manifest.json"));
            zos.write(manifestJson.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
    }

    /**
     * Build xml archival metadata.
     *
     * @param sourceMetadata   the source metadata
     * @param migrationContext the migration context
     * @param inputMap         the input map
     * @return the archival metadata
     */
    public static ArchivalMetadata buildXml(
            @NonNull SourceMetadata sourceMetadata,
            @NonNull MigrationContext migrationContext,
            Map<String, Object> inputMap) {
        ArchivalMetadata metadata = new ArchivalMetadata();

        metadata.setDocumentId(migrationContext.getDocId());
        metadata.setTitle(
                sourceMetadata.getTitle() != null
                        ? sourceMetadata.getTitle()
                        : "Untitled Document");
        metadata.setCreationDate(sourceMetadata.getCreationDate());
        metadata.setClientId(sourceMetadata.getClientId());
        metadata.setDocumentType(sourceMetadata.getDocumentType());
        metadata.setPageCount(migrationContext.getPageHashes().size());

        metadata.setPayloadHash(migrationContext.getPayloadHash());
        metadata.setZipHash(migrationContext.getZipHash());
        metadata.setPdfHash(migrationContext.getPdfHash());

        MigrationProvenance provenance = new MigrationProvenance();
        provenance.setMigrationTimestamp(Instant.now().toString());
        provenance.setToolVersion("1.0.0");
        provenance.setOperator("migration-service");
        provenance.setPageHashes(migrationContext.getPageHashes());

        metadata.setProvenance(provenance);

        Map<String, Object> customFields = new HashMap<>();
        if (inputMap != null) {
            customFields.putAll(inputMap);
        }
        customFields.put("sourceSystem", "legacy-archive-v1");
        metadata.setCustomFields(customFields);

        return metadata;
    }
}
