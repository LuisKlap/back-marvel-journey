package com.marvel.marveljourney.service;

import com.marvel.marveljourney.model.User;
import com.marvel.marveljourney.repository.UserRepository;
import com.marvel.marveljourney.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

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

    public User findByRefreshToken(String refreshToken) {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.getMetadata() != null) {
                for (User.Metadata metadata : user.getMetadata()) {
                    if (passwordEncoder.matches(refreshToken, metadata.getRefreshTokenHash())) {
                        return user;
                    }
                }
            }
        }
        return null;
    }

    public Map<String, String> refreshToken(String refreshToken, long jwtExpirationTime, String issuer, String audience,
                                            long refreshTokenDurationMs) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new IllegalArgumentException("Refresh token is required.");
        }

        User user = findByRefreshToken(refreshToken);
        if (user == null) {
            throw new IllegalArgumentException("Invalid or expired refresh token.");
        }

        boolean isTokenExpired = true;
        for (User.Metadata metadata : user.getMetadata()) {
            if (passwordEncoder.matches(refreshToken, metadata.getRefreshTokenHash()) &&
                    metadata.getRefreshTokenExpiryDate().isAfter(Instant.now())) {
                isTokenExpired = false;
                break;
            }
        }

        if (isTokenExpired) {
            throw new IllegalArgumentException("Invalid or expired refresh token.");
        }

        String token = jwtUtil.generateToken(user.getEmail(), jwtExpirationTime, issuer, audience, user.getRoles());
        String newRefreshToken = refreshTokenService.generateRefreshToken(user, refreshTokenDurationMs);
        return Map.of("token", token, "refreshToken", newRefreshToken);
    }

    public void deleteRefreshToken(User user, String refreshToken) {
        if (user.getMetadata() != null) {
            user.getMetadata().removeIf(metadata -> passwordEncoder.matches(refreshToken, metadata.getRefreshTokenHash()));
            userRepository.save(user);
        }
    }
}
