package com.marvel.marveljourney.service;

import com.marvel.marveljourney.model.User;
import com.marvel.marveljourney.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public String generateRefreshToken(User user, long refreshTokenDurationMs) {
        String refreshToken = UUID.randomUUID().toString();
        String refreshTokenHash = passwordEncoder.encode(refreshToken);

        if (user.getMetadata() == null) {
            user.setMetadata(new ArrayList<>());
        }

        boolean tokenExists = false;
        for (User.Metadata metadata : user.getMetadata()) {
            if (passwordEncoder.matches(refreshToken, metadata.getRefreshTokenHash())) {
                tokenExists = true;
                break;
            }
        }

        if (!tokenExists) {
            User.Metadata newMetadata = new User.Metadata();
            newMetadata.setRefreshTokenHash(refreshTokenHash);
            newMetadata.setRefreshTokenExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
            user.getMetadata().add(newMetadata);
        }

        userRepository.save(user);
        return refreshToken;
    }
}
