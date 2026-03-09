/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * The type Sftp properties.
 */
@Data
@Component
@ConfigurationProperties(prefix = "target.sftp")
public class SftpProperties {
    private String host;
    private int port = 22;
    private String username;
    private String password;
    private Resource privateKey;
    private Resource knownHosts;
    private String remoteDirectory = "/incoming/migration";
    private boolean allowUnknownKeys;
}
