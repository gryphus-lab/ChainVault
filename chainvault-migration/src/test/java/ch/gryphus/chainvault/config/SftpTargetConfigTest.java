/*
 * Copyright (c) 2026. Gryphus Lab
 */
package ch.gryphus.chainvault.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import org.apache.sshd.sftp.client.SftpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;

@ExtendWith(MockitoExtension.class)
class SftpTargetConfigTest {

    @Mock private SftpProperties mockProps;
    @Mock private SessionFactory<SftpClient.DirEntry> mockSessionFactory;

    private SftpTargetConfig sftpTargetConfigUnderTest;

    @BeforeEach
    void setUp() {
        sftpTargetConfigUnderTest = new SftpTargetConfig(mockProps);
    }

    @Test
    void testSftpSessionFactory() {
        // Setup
        when(mockProps.getHost()).thenReturn("host");
        when(mockProps.getPort()).thenReturn(0);
        when(mockProps.getUsername()).thenReturn("user");
        when(mockProps.getPassword()).thenReturn("result");

        // Configure SftpProperties.getKnownHosts(...).
        Resource resource = new ByteArrayResource("content".getBytes(StandardCharsets.UTF_8));
        when(mockProps.getKnownHosts()).thenReturn(resource);

        // Run the test
        CachingSessionFactory<SftpClient.DirEntry> result =
                sftpTargetConfigUnderTest.sftpSessionFactory();

        // Verify the results
        assertThat(result).isNotNull();
    }

    @Test
    void testSftpSessionFactory_SftpPropertiesGetPasswordReturnsNull() {
        // Setup
        when(mockProps.getHost()).thenReturn("host");
        when(mockProps.getPort()).thenReturn(0);
        when(mockProps.getUsername()).thenReturn("user");
        when(mockProps.getPassword()).thenReturn(null);

        // Configure SftpProperties.getPrivateKey(...).
        Resource resource = new ByteArrayResource("content".getBytes(StandardCharsets.UTF_8));
        when(mockProps.getPrivateKey()).thenReturn(resource);

        // Configure SftpProperties.getKnownHosts(...).
        Resource resource1 = new ByteArrayResource("content".getBytes(StandardCharsets.UTF_8));
        when(mockProps.getKnownHosts()).thenReturn(resource1);

        // Run the test
        CachingSessionFactory<SftpClient.DirEntry> result =
                sftpTargetConfigUnderTest.sftpSessionFactory();

        // Verify the results
        assertThat(result).isNotNull();
    }

    @Test
    void testSftpSessionFactory_SftpPropertiesGetPrivateKeyReturnsNull() {
        // Setup
        when(mockProps.getHost()).thenReturn("host");
        when(mockProps.getPort()).thenReturn(0);
        when(mockProps.getUsername()).thenReturn("user");
        when(mockProps.getPassword()).thenReturn(null);
        when(mockProps.getPrivateKey()).thenReturn(null);

        // Configure SftpProperties.getKnownHosts(...).
        Resource resource = new ByteArrayResource("content".getBytes(StandardCharsets.UTF_8));
        when(mockProps.getKnownHosts()).thenReturn(resource);

        // Run the test
        CachingSessionFactory<SftpClient.DirEntry> result =
                sftpTargetConfigUnderTest.sftpSessionFactory();

        // Verify the results
        assertThat(result).isNotNull();
    }

    @Test
    void testSftpSessionFactory_SftpPropertiesGetKnownHostsReturnsNull() {
        // Setup
        when(mockProps.getHost()).thenReturn("host");
        when(mockProps.getPort()).thenReturn(0);
        when(mockProps.getUsername()).thenReturn("user");
        when(mockProps.getPassword()).thenReturn("result");
        when(mockProps.getKnownHosts()).thenReturn(null);
        when(mockProps.isAllowUnknownKeys()).thenReturn(false);

        // Run the test
        CachingSessionFactory<SftpClient.DirEntry> result =
                sftpTargetConfigUnderTest.sftpSessionFactory();

        // Verify the results
        assertThat(result).isNotNull();
    }

    @Test
    void testSftpRemoteFileTemplate() {
        // Run the test
        SftpRemoteFileTemplate result =
                sftpTargetConfigUnderTest.sftpRemoteFileTemplate(mockSessionFactory);

        // Verify the results
        assertThat(result).isNotNull();
    }

    @Test
    void testGetRemoteDirectory() {
        // Setup
        when(mockProps.getRemoteDirectory()).thenReturn("remoteDirectory");

        // Run the test
        String result = sftpTargetConfigUnderTest.getRemoteDirectory();

        // Verify the results
        assertThat(result).isEqualTo("remoteDirectory");
    }
}
