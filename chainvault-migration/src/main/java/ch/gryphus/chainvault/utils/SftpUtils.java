/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.utils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;

/**
 * The type Sftp utils.
 */
@Slf4j
public final class SftpUtils {
    private SftpUtils() {
        /* This utility class should not be instantiated */
    }

    /**
     * Upload.
     *
     * @param remoteDirectory    the remote directory
     * @param remoteFileTemplate the remote file template
     * @param map                the map
     */
    public static void upload(
            String remoteDirectory,
            @NonNull SftpRemoteFileTemplate remoteFileTemplate,
            @NonNull Map<String, Object> map) {
        Object docId = map.get("docId");
        String folder = "%s/%s-%s".formatted(remoteDirectory, docId, map.get("processInstanceId"));

        remoteFileTemplate.execute(
                session -> {
                    log.info("sftp upload started");
                    session.mkdir(folder);
                    session.write(
                            Files.newInputStream((Path) map.get("zipPath")),
                            "%s/%s_chain.zip".formatted(folder, docId));
                    var pdfPath = map.get("pdfPath");
                    if (pdfPath != null) { // when PDF was not generated
                        session.write(
                                Files.newInputStream((Path) pdfPath),
                                "%s/%s.pdf".formatted(folder, docId));
                    }
                    session.write(
                            new ByteArrayInputStream(
                                    map.get("xml").toString().getBytes(StandardCharsets.UTF_8)),
                            "%s/%s_meta.xml".formatted(folder, docId));
                    log.info("sftp upload completed");
                    return null;
                });
    }
}
