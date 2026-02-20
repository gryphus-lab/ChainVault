package ch.gryphus.chainvault.service;

import ch.gryphus.chainvault.config.SftpTargetConfig;
import ch.gryphus.chainvault.domain.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.tika.Tika;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class MigrationService {

    private final RestClient restClient;
    private final SftpRemoteFileTemplate sftp;
    private final SftpTargetConfig sftpCfg;
    private final XmlMapper xmlMapper;

    public void migrateDocument(String docId) {
        var ctx = new MigrationContext(docId);

        Path zipPath = null;
        Path pdfPath = null;
        byte[] payload = new byte[0];

        try {
            // 1. Extract
            var meta = restClient.get().uri("/documents/{id}", docId).retrieve().body(SourceMetadata.class);
            if (meta != null && meta.getPayloadUrl() != null) {
                payload = restClient.get().uri(meta.getPayloadUrl()).retrieve().body(byte[].class);
                ctx.setPayloadHash(sha256(payload));
            }

            // 2. Extract pages
            List<TiffPage> pages = extractTiffPages(payload, ctx);

            // 3. Chain ZIP
            zipPath = createChainZip(docId, pages, meta, ctx);
            ctx.setZipHash(sha256(zipPath));

            // 4. PDF
            pdfPath = mergeTiffToPdf(pages, docId);
            ctx.setPdfHash(sha256(pdfPath));

            // 5. XML metadata
            String xml = xmlMapper.writeValueAsString(buildXml(Objects.requireNonNull(meta), ctx));

            // 6. SFTP upload
            String folder = "%s/%s".formatted(sftpCfg.getRemoteDirectory(), docId);
            Path finalZipPath = zipPath;
            Path finalPdfPath = pdfPath;
            sftp.execute(s -> {
                s.mkdir(folder);
                s.write(Files.newInputStream(finalZipPath.toFile().toPath()), "%s/%s_chain.zip".formatted(folder, docId));
                s.write(Files.newInputStream(finalPdfPath.toFile().toPath()), "%s/%s.pdf".formatted(folder, docId));
                s.write(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), "%s/%s_meta.xml".formatted(folder, docId));
                return null;
            });

            log.info("Done {} | zipPath={} | pdf={}", docId, ctx.getZipHash(), ctx.getPdfHash());

        } catch (IOException | NoSuchAlgorithmException e) {
            log.error("Failed {}", docId, e);
        } finally {
            // Cleanup temporary files – always try to delete, even on failure
            if (zipPath != null) {
                try {
                    Files.deleteIfExists(zipPath);
                    log.debug("Deleted temp ZIP: {}", zipPath);
                } catch (IOException e) {
                    log.warn("Failed to delete temp ZIP {}: {}", zipPath, e.getMessage());
                }
            }

            if (pdfPath != null) {
                try {
                    Files.deleteIfExists(pdfPath);
                    log.debug("Deleted temp PDF: {}", pdfPath);
                } catch (IOException e) {
                    log.warn("Failed to delete temp PDF {}: {}", pdfPath, e.getMessage());
                }
            }
        }
    }

    String sha256(Path path) throws IOException, NoSuchAlgorithmException {
        return sha256(Files.readAllBytes(path));
    }

    String sha256(byte[] data) throws NoSuchAlgorithmException {
        return Hex.encodeHexString(MessageDigest.getInstance("SHA-256").digest(data));
    }

    private List<TiffPage> extractTiffPages(byte[] payload, MigrationContext ctx) throws IOException, NoSuchAlgorithmException {
        List<TiffPage> pages = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(payload))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().toLowerCase().endsWith(".tif") || entry.getName().toLowerCase().endsWith(".tiff")) {
                    byte[] data = zis.readAllBytes();

                    String pageHash = sha256(data);
                    ctx.addPageHash(entry.getName(), pageHash);
                    pages.add(new TiffPage(entry.getName(), data));
                }
            }
        }
        return pages;
    }

    public Path createChainZip(String docId, List<TiffPage> pages, SourceMetadata sourceMetadata, MigrationContext ctx)
            throws IOException, NoSuchAlgorithmException {

        Path zipPath = Files.createTempFile("%s_chain".formatted(docId), ".zip");

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {

            // 1. Add all TIFF pages
            for (int i = 0; i < pages.size(); i++) {
                TiffPage page = pages.get(i);
                String entryName = String.format("page-%03d_%s", i + 1, page.name());

                zos.putNextEntry(new ZipEntry(entryName));
                zos.write(page.data());
                zos.closeEntry();
            }

            // 2. Add manifest.json
            Map<String, Object> manifest = new LinkedHashMap<>();
            manifest.put("docId", docId);
            manifest.put("timestamp", Instant.now().toString());
            manifest.put("pageCount", pages.size());
            manifest.put("pageHashes", ctx.getPageHashes());
            manifest.put("payloadHash", ctx.getPayloadHash());

            // optional: snapshot of source metadata
            if (sourceMetadata != null) {
                manifest.put("sourceMetadata", Map.of(
                        "docId", sourceMetadata.getDocId(),
                        "title", sourceMetadata.getTitle(),
                        "creationDate", sourceMetadata.getCreationDate(),
                        "clientId", sourceMetadata.getClientId()
                ));
            }

            String manifestJson = new ObjectMapper().writerWithDefaultPrettyPrinter()
                    .writeValueAsString(manifest);

            zos.putNextEntry(new ZipEntry("manifest.json"));
            zos.write(manifestJson.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        // Optional: log final hash for audit trail
        String zipHash = sha256(zipPath);
        log.info("Chain ZIP created: {} | hash = {}", zipPath.getFileName(), zipHash);

        return zipPath;
    }

    public Path mergeTiffToPdf(List<TiffPage> pages, String docId) throws IOException {
        Path pdf = Path.of("/tmp/" + docId + ".pdf");
        try (var doc = new PDDocument()) {
            for (var page : pages) {
                BufferedImage img = ImageIO.read(new ByteArrayInputStream(page.data()));
                try {
                    var pdImage = LosslessFactory.createFromImage(doc, img);
                    var pdPage = new PDPage(new PDRectangle(img.getWidth(), img.getHeight()));
                    doc.addPage(pdPage);

                    try (var cs = new PDPageContentStream(doc, pdPage)) {
                        cs.drawImage(pdImage, 0, 0);
                    }
                } catch (Exception ignored) {
                    log.error("Failed to merge PDF image to pdf: {}", docId);
                }
            }
            doc.save(pdf.toFile());
        }
        return pdf;
    }

    public ArchivalMetadata buildXml(SourceMetadata sourceMetadata, MigrationContext ctx) {
        ArchivalMetadata metadata = new ArchivalMetadata();

        // Core document info
        metadata.setDocumentId(ctx.getDocId());
        metadata.setTitle(sourceMetadata.getTitle() != null ? sourceMetadata.getTitle() : "Untitled Document");
        metadata.setCreationDate(sourceMetadata.getCreationDate());
        metadata.setClientId(sourceMetadata.getClientId());
        metadata.setDocumentType(sourceMetadata.getDocumentType());
        metadata.setPageCount(ctx.getPageHashes().size());

        // Integrity / chain of custody
        metadata.setPayloadHash(ctx.getPayloadHash());
        metadata.setZipHash(ctx.getZipHash());
        metadata.setPdfHash(ctx.getPdfHash());

        // Migration provenance
        MigrationProvenance provenance = new MigrationProvenance();
        provenance.setMigrationTimestamp(Instant.now().toString());
        provenance.setToolVersion("1.0.0");           // or read from pom.properties / build info
        provenance.setOperator("migration-service");  // or System.getProperty("user.name")
        provenance.setPageHashes(ctx.getPageHashes()); // Map<String, String> filename → hash

        metadata.setProvenance(provenance);

        metadata.setCustomFields(Map.of("sourceSystem", "legacy-archive-v1"));
        return metadata;
    }

    public List<TiffPage> unzipTiffPages(byte[] zipBytes) throws IOException {
        var pages = new ArrayList<TiffPage>();

        try (var zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;

                String nameLower = entry.getName().toLowerCase();
                if (getDetectedMimeType(zis).equals("image/tiff") && (nameLower.endsWith(".tif") || nameLower.endsWith(".tiff"))) {
                    byte[] data = zis.readAllBytes();
                    pages.add(new TiffPage(entry.getName(), data));
                }
            }
        }

        if (pages.isEmpty()) {
            throw new IllegalStateException("No TIFF pages found in ZIP");
        }

        return pages;
    }

    public String getDetectedMimeType(InputStream in) throws IOException {
        Tika tika = new Tika();
        return tika.detect(in);
    }
}
