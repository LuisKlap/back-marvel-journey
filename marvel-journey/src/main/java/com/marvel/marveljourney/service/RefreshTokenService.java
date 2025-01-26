package com.marvel.marveljourney.service;

import com.marvel.marveljourney.model.User;
import com.marvel.marveljourney.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired
    private UserRepository userRepository;

    public String generateRefreshToken(User user, long refreshTokenDurationMs) {
        String refreshToken = UUID.randomUUID().toString();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String refreshTokenHash = passwordEncoder.encode(refreshToken);

        if (user.getMetadata() == null) {
            List<User.Metadata> metadataList = new ArrayList<>();
            metadataList.add(new User.Metadata());
            user.setMetadata(metadataList);
        }

        for (User.Metadata metadata : user.getMetadata()) {
            metadata.setRefreshTokenHash(refreshTokenHash);
            metadata.setRefreshTokenExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        }
        userRepository.save(user);
        return refreshToken;
    }
}
