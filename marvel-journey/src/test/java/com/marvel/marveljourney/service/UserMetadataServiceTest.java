package com.marvel.marveljourney.service;

import com.marvel.marveljourney.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserMetadataServiceTest {

    @InjectMocks
    private UserMetadataService userMetadataService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setEmail("test@example.com");
    }

    @Test
    void testUpdateMetadata_NewMetadata() {
        String ipAddress = "192.168.0.1";
        String userAgent = "Mozilla/5.0";

        userMetadataService.updateMetadata(user, ipAddress, userAgent);

        assertNotNull(user.getMetadata());
        assertEquals(1, user.getMetadata().size());
        assertEquals(ipAddress, user.getMetadata().get(0).getIpAddress());
        assertEquals(userAgent, user.getMetadata().get(0).getUserAgent());
        assertNotNull(user.getMetadata().get(0).getLastLoginAt());
    }

    @Test
    void testUpdateMetadata_ExistingMetadata() {
        String ipAddress = "192.168.0.1";
        String userAgent = "Mozilla/5.0";
        User.Metadata metadata = new User.Metadata();
        metadata.setIpAddress(ipAddress);
        metadata.setUserAgent(userAgent);
        metadata.setLastLoginAt(Instant.now().minusSeconds(3600));
        user.setMetadata(List.of(metadata));

        userMetadataService.updateMetadata(user, ipAddress, userAgent);

        assertNotNull(user.getMetadata());
        assertEquals(1, user.getMetadata().size());
        assertEquals(ipAddress, user.getMetadata().get(0).getIpAddress());
        assertEquals(userAgent, user.getMetadata().get(0).getUserAgent());
        assertTrue(user.getMetadata().get(0).getLastLoginAt().isAfter(Instant.now().minusSeconds(60)));
    }

    @Test
    void testUpdateMetadata_MultipleMetadata() {
        String ipAddress1 = "192.168.0.1";
        String userAgent1 = "Mozilla/5.0";
        User.Metadata metadata1 = new User.Metadata();
        metadata1.setIpAddress(ipAddress1);
        metadata1.setUserAgent(userAgent1);
        metadata1.setLastLoginAt(Instant.now().minusSeconds(3600));

        String ipAddress2 = "192.168.0.2";
        String userAgent2 = "Mozilla/5.0";
        User.Metadata metadata2 = new User.Metadata();
        metadata2.setIpAddress(ipAddress2);
        metadata2.setUserAgent(userAgent2);
        metadata2.setLastLoginAt(Instant.now().minusSeconds(3600));

        user.setMetadata(List.of(metadata1, metadata2));

        userMetadataService.updateMetadata(user, ipAddress1, userAgent1);

        assertNotNull(user.getMetadata());
        assertEquals(2, user.getMetadata().size());
        assertTrue(user.getMetadata().get(0).getLastLoginAt().isAfter(Instant.now().minusSeconds(60)));
        assertEquals(ipAddress2, user.getMetadata().get(1).getIpAddress());
        assertEquals(userAgent2, user.getMetadata().get(1).getUserAgent());
    }
}