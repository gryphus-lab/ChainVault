package ch.gryphus.demo.migrationtool.service;

import ch.gryphus.demo.migrationtool.config.SftpTargetConfig;
import ch.gryphus.demo.migrationtool.domain.ArchivalMetadata;
import ch.gryphus.demo.migrationtool.domain.MigrationContext;
import ch.gryphus.demo.migrationtool.domain.SourceMetadata;
import ch.gryphus.demo.migrationtool.domain.TiffPage;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.jobrunr.jobs.annotations.Job;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArchiveMigrationService {

    private final RestClient restClient;
    private final SftpRemoteFileTemplate sftp;
    private final SftpTargetConfig sftpCfg;
    private final XmlMapper xmlMapper;

    @Job(name = "Migrate %0")
    public void migrateDocument(String docId) throws NoSuchAlgorithmException, IOException {
        var ctx = new MigrationContext(docId);

        try {
            // 1. Metadata + payload
            var meta = restClient.get().uri("/documents/{id}/metadata", docId).retrieve().body(SourceMetadata.class);
            byte[] payload = restClient.get().uri("/documents/{id}/payload", docId).retrieve().body(byte[].class);

            ctx.setPayloadHash(sha256(payload));

            // 2. Extract pages
            List<TiffPage> pages = extractPages(payload, ctx);

            // 3. Chain ZIP
            Path zip = createChainZip(docId, pages, meta, ctx);
            ctx.setZipHash(sha256(zip));

            // 4. PDF
            Path pdf = mergeTiffToPdf(pages, docId);
            ctx.setPdfHash(sha256(pdf));

            // 5. XML metadata
            String xml = xmlMapper.writeValueAsString(buildXml(meta, ctx));

            // 6. SFTP upload
            String folder = sftpCfg.getRemoteDirectory() + "/" + docId;
            sftp.execute(s -> {
                s.mkdir(folder);
                s.write(Files.newInputStream(zip.toFile().toPath()), folder + "/" + docId + "_chain.zip");
                s.write(Files.newInputStream(pdf.toFile().toPath()), folder + "/" + docId + ".pdf");
                s.write(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), folder + "/" + docId + "_meta.xml");
                return null;
            });

            log.info("Done {} | zip={} | pdf={}", docId, ctx.getZipHash(), ctx.getPdfHash());

        } catch (Exception e) {
            log.error("Failed {}", docId, e);
            throw e;
        }
    }

    private String sha256(Path path) throws IOException, NoSuchAlgorithmException {
        return sha256(Files.readAllBytes(path));
    }

    private String sha256(byte[] data) throws NoSuchAlgorithmException {
        return Hex.encodeHexString(MessageDigest.getInstance("SHA-256").digest(data));
    }

    private List<TiffPage> extractPages(byte[] payload, MigrationContext ctx) throws IOException, NoSuchAlgorithmException {
        List<TiffPage> pages = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(payload))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().toLowerCase().endsWith(".tif") || entry.getName().toLowerCase().endsWith(".tiff")) {
                    byte[] data = zis.readAllBytes(); // careful with very large pages → consider streaming
                    String pageHash = sha256(data);
                    ctx.addPageHash(entry.getName(), pageHash);
                    pages.add(new TiffPage(entry.getName(), data));
                }
            }
        }
        return pages;
    }

    private Path createChainZip(String id, List<TiffPage> pages, SourceMetadata meta, MigrationContext ctx) {
        return null;
    }

    private Path mergeTiffToPdf(List<TiffPage> pages, String docId) throws IOException {
        Path pdf = Path.of("/tmp/" + docId + ".pdf");
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

    private ArchivalMetadata buildXml(SourceMetadata meta, MigrationContext ctx) {
        return new ArchivalMetadata();
    }
    
    public List<TiffPage> unzipTiffPages(byte[] zipBytes) throws IOException {
        var pages = new ArrayList<TiffPage>();

        try (var zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;

                String nameLower = entry.getName().toLowerCase();
                if (nameLower.endsWith(".tif") || nameLower.endsWith(".tiff")) {
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

    public Path zipPages(String s, List<byte[]> fakePages, Object o) {
        return null;
    }

    public void migrate(String docId) {
    }
}
