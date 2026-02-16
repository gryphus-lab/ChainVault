package ch.gryphus.demo.migrationtool;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Hex;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jobrunr.jobs.annotations.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;
import org.springframework.web.client.RestClient;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@SpringBootApplication
public class MigrationApp {

    private static final Logger log = LoggerFactory.getLogger(MigrationApp.class);

    public static void main(String[] args) {
        SpringApplication.run(MigrationApp.class, args);
    }

    @Bean
    RestClient restClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:8081")           // ← change
                .defaultHeader("Authorization", "Bearer dummy-token")
                .build();
    }

    @Bean
    SftpRemoteFileTemplate sftpTemplate() {
        var factory = new DefaultSftpSessionFactory(true);
        factory.setHost("localhost");
        factory.setPort(2222);
        factory.setUser("test");
        factory.setPassword("test");
        factory.setAllowUnknownKeys(true);  // production: use known_hosts + keys

        return new SftpRemoteFileTemplate(new CachingSessionFactory<>(factory));
    }

    @Bean
    MigrationService migrationService(RestClient rest, SftpRemoteFileTemplate sftp) {
        return new MigrationService(rest, sftp);
    }

    static class MigrationService {

        private static final Logger log = LoggerFactory.getLogger(MigrationService.class);

        private final RestClient rest;
        private final SftpRemoteFileTemplate sftp;

        MigrationService(RestClient rest, SftpRemoteFileTemplate sftp) {
            this.rest = rest;
            this.sftp = sftp;
        }

        @Job(name = "Migrate document {0}")
        public void migrate(String id) throws Exception {
            log.info("Starting {}", id);

            // 1. Fetch payload (ZIP of TIFFs)
            byte[] zipBytes = rest.get()
                    .uri("/documents/{id}/payload", id)
                    .retrieve()
                    .body(byte[].class);

            // 2. Extract TIFF pages
            List<byte[]> tiffs = new ArrayList<>();
            try (var zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
                ZipEntry e;
                while ((e = zis.getNextEntry()) != null) {
                    String n = e.getName().toLowerCase();
                    if (!e.isDirectory() && (n.endsWith(".tif") || n.endsWith(".tiff"))) {
                        tiffs.add(zis.readAllBytes());
                    }
                }
            }
            if (tiffs.isEmpty()) throw new IllegalStateException("No TIFF found");

            // 3. Chain-of-custody ZIP + manifest
            Path chainZip = Files.createTempFile("chain-" + id, ".zip");
            try (var zos = new ZipOutputStream(Files.newOutputStream(chainZip))) {
                for (int i = 0; i < tiffs.size(); i++) {
                    zos.putNextEntry(new ZipEntry("page-%03d.tif".formatted(i + 1)));
                    zos.write(tiffs.get(i));
                    zos.closeEntry();
                }
                zos.putNextEntry(new ZipEntry("manifest.json"));
                zos.write("{\"docId\":\"%s\",\"pageCount\":%d}".formatted(id, tiffs.size()).getBytes());
                zos.closeEntry();
            }

            // 4. Merge to PDF (lossless)
            Path pdf = Files.createTempFile("doc-" + id, ".pdf");
            try (var doc = new PDDocument()) {
                for (byte[] bytes : tiffs) {
                    BufferedImage img = ImageIO.read(new ByteArrayInputStream(bytes));
                    PDImageXObject pdImg = LosslessFactory.createFromImage(doc, img);
                    PDPage page = new PDPage(new PDRectangle(img.getWidth(), img.getHeight()));
                    doc.addPage(page);
                    try (var cs = new PDPageContentStream(doc, page)) {
                        cs.drawImage(pdImg, 0, 0);
                    }
                }
                doc.save(pdf.toFile());
            }

            // 5. Minimal XML metadata
            String xml = """
                    <Document>
                      <id>%s</id>
                      <pages>%d</pages>
                      <chainHash>%s</chainHash>
                    </Document>
                    """.formatted(id, tiffs.size(), sha256(chainZip));

            // 6. SFTP upload
            String remoteDir = "/incoming/" + id;
            sftp.execute(s -> {
                s.mkdir(remoteDir);
                s.write(Files.newInputStream(chainZip), remoteDir + "/chain.zip");
                s.write(Files.newInputStream(pdf), remoteDir + "/document.pdf");
                s.write(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)), remoteDir + "/meta.xml");
                return null;
            });

            // cleanup
            Files.deleteIfExists(chainZip);
            Files.deleteIfExists(pdf);

            log.info("Completed {} (pages: {})", id, tiffs.size());
        }

        private String sha256(Path file) throws Exception {
            return Hex.encodeHexString(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file)));
        }

        record TiffPage(String name, byte[] data) {}

        List<TiffPage> unzipTiffPages(byte[] zipBytes) throws IOException {
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

        public Path zipPages(String id, List<byte[]> pages, Map m) throws IOException {
            var p = Path.of("/tmp/" + id + "-c.zip");
            try (var zos = new ZipOutputStream(Files.newOutputStream(p))) {
                for (int i = 0; i < pages.size(); i++) {
                    zos.putNextEntry(new ZipEntry("p" + (i+1) + ".tif"));
                    zos.write(pages.get(i));
                    zos.closeEntry();
                }
                zos.putNextEntry(new ZipEntry("meta.json"));
                zos.write(new ObjectMapper().writeValueAsBytes(m));
                zos.closeEntry();
            }
            return p;
        }
    }

    static class Starter {
        private final MigrationService service;

        Starter(MigrationService service) {
            this.service = service;
        }

        @EventListener(ApplicationReadyEvent.class)
        public void onStart() {
            // Fake / test documents – replace with real inventory call
            List.of("doc-001", "doc-002", "doc-003")
                    .forEach(id -> org.jobrunr.scheduling.BackgroundJob.enqueue(() -> service.migrate(id)));
        }
    }
}