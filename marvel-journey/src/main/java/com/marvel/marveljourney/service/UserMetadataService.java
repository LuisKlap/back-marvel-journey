package com.marvel.marveljourney.service;

import com.marvel.marveljourney.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;

@Service
public class UserMetadataService {

    private static final Logger logger = LoggerFactory.getLogger(UserMetadataService.class);

    public void updateMetadata(User user, String ipAddress, String userAgent) {
        if (user.getMetadata() == null) {
            user.setMetadata(new ArrayList<>());
        }

        boolean metadataExists = false;
        for (User.Metadata metadata : user.getMetadata()) {
            if (metadata.getIpAddress().equals(ipAddress) && metadata.getUserAgent().equals(userAgent)) {
                metadata.setLastLoginAt(Instant.now());
                metadataExists = true;
                break;
            }
        }

        if (!metadataExists) {
            User.Metadata newMetadata = new User.Metadata();
            newMetadata.setLastLoginAt(Instant.now());
            newMetadata.setIpAddress(ipAddress);
            newMetadata.setUserAgent(userAgent);
            user.getMetadata().add(newMetadata);
        }

        logger.info("Atualizando metadata para o usuário: {}", user.getEmail());
    }
}