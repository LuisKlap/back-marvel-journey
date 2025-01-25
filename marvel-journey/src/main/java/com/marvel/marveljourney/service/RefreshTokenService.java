package com.marvel.marveljourney.service;

import com.marvel.marveljourney.model.User;
import com.marvel.marveljourney.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired
    private UserRepository userRepository;

    public String generateRefreshToken(User user, long refreshTokenDurationMs) {
        String refreshToken = UUID.randomUUID().toString();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String refreshTokenHash = passwordEncoder.encode(refreshToken);
        user.setRefreshTokenHash(refreshTokenHash);
        user.setRefreshTokenExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        userRepository.save(user);
        return refreshToken;
    }

    public void updateRefreshToken(User user, long refreshTokenDurationMs) {
        user.setRefreshTokenExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        userRepository.save(user);
    }
}
