package ch.gryphus.chainvault.config;

import lombok.Getter;
import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;

@Configuration
public class SftpTargetConfig {

    @Value("${target.sftp.host}")
    private String host;

    @Value("${target.sftp.port:22}")
    private int port;

    @Value("${target.sftp.username}")
    private String username;

    @Value("${target.sftp.password}")
    private String password;

    @Value("${target.sftp.private-key-path}")
    private Resource privateKey;

    @Value("${target.sftp.known-hosts}")
    private Resource knownHosts;

    // Getter for remote dir (used in service)
    @Getter
    @Value("${target.sftp.remote-directory:/incoming/migration}")
    private String remoteDirectory;

    @Bean
    public CachingSessionFactory<SftpClient.DirEntry> sftpSessionFactory() {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);  // true = allow unknown keys (dev); false = strict in prod

        factory.setHost(host);
        factory.setPort(port);
        factory.setUser(username);

        if (!password.isEmpty()) {
            factory.setPassword(password);
        } else if (privateKey != null && privateKey.exists()) {
            factory.setPrivateKey(privateKey);
        }

        // Strict host key checking (recommended for compliance)
        if (knownHosts != null && knownHosts.exists()) {
            factory.setKnownHostsResource(knownHosts);
        } else {
            // Fallback: disable strict checking (less secure, log warning)
            factory.setAllowUnknownKeys(true);
        }

        // Cache sessions for reuse (critical for performance with many uploads)
        return new CachingSessionFactory<>(factory, 10);  // Cache up to 10 sessions
    }

    @Bean
    public SftpRemoteFileTemplate sftpRemoteFileTemplate(SessionFactory<SftpClient.DirEntry> sessionFactory) {
        return new SftpRemoteFileTemplate(sessionFactory);
    }
}