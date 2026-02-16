package ch.gryphus.demo.migrationtool.config;

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

import java.io.File;

@Configuration
public class SftpTargetConfig {

    @Value("${target.sftp.host}")
    private String host;

    @Value("${target.sftp.port:22}")
    private int port;

    @Value("${target.sftp.username}")
    private String username;

    @Value("${target.sftp.password:}")  // Use secrets manager in prod (Vault / env / Kubernetes secrets)
    private String password;

    @Value("${target.sftp.private-key-path:}")  // Optional: path to id_rsa
    private String privateKeyPath;

    @Value("${target.sftp.known-hosts:}")
    private String knownHostsPath;  // For strict checking

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
        } else if (!privateKeyPath.isEmpty()) {
            factory.setPrivateKey((Resource) new File(privateKeyPath));
            // factory.setPrivateKeyPassphrase(...) if encrypted key
        }

        // Strict host key checking (recommended for compliance)
        if (!knownHostsPath.isEmpty()) {
            factory.setKnownHostsResource((Resource) new File(knownHostsPath));
        } else {
            // Fallback: disable strict checking (less secure, log warning)
            factory.setAllowUnknownKeys(true);
        }

        // Optional: tuning
        //NOSONAR factory.setPreferredAuthentications("publickey,password");
        //factory.setServerAliveInterval(60_000);  // Keep-alive

        // Cache sessions for reuse (critical for performance with many uploads)
        return new CachingSessionFactory<>(factory, 10);  // Cache up to 10 sessions
    }

    @Bean
    public SftpRemoteFileTemplate sftpRemoteFileTemplate(SessionFactory<SftpClient.DirEntry> sessionFactory) {
        return new SftpRemoteFileTemplate(sessionFactory);
    }
}