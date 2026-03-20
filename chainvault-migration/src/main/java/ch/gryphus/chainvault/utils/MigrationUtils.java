/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.utils;

import ch.gryphus.chainvault.config.Constants;
import ch.gryphus.chainvault.domain.ArchivalMetadata;
import ch.gryphus.chainvault.domain.MigrationContext;
import ch.gryphus.chainvault.domain.MigrationProvenance;
import ch.gryphus.chainvault.domain.SourceMetadata;
import ch.gryphus.chainvault.domain.TiffPage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import lombok.NonNull;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.tika.Tika;
import tools.jackson.databind.ObjectMapper;

/**
 * The type Migration utils.
 */
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
     * Merge pages to pdf path.
     *
     * @param pages            the pages
     * @param docId            the doc id
     * @param workingDirectory the working directory
     * @return the path
     * @throws IOException the io exception
     */
    public static Path mergePagesToPdf(List<TiffPage> pages, String docId, Path workingDirectory)
            throws IOException {
        Path pdf = Path.of("%s/%s-merged.pdf".formatted(workingDirectory, docId));
        try (var doc = new PDDocument()) {
            for (var page : pages) {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(page.data()));
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
            List<TiffPage> pages,
            Path zipPath)
            throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            Map<String, Object> manifest = new LinkedHashMap<>();
            String docId = sourceMetadata.getDocId();
            manifest.put(Constants.BPMN_PROC_VAR_DOC_ID, docId);

            if (pages != null && !pages.isEmpty()) {
                for (TiffPage page : pages) {
                    String entryName = "%s".formatted(page.name());

                    zos.putNextEntry(new ZipEntry(entryName));
                    zos.write(page.data());
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
}
