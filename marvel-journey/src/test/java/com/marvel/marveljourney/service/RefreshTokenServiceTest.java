package com.marvel.marveljourney.service;

import com.marvel.marveljourney.model.User;
import com.marvel.marveljourney.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RefreshTokenServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setEmail("test@example.com");
        user.setMetadata(new ArrayList<>());
        user.getMetadata().add(new User.Metadata());
    }

    @Test
    void testGenerateRefreshToken() {
        long refreshTokenDurationMs = 3600000;
        String refreshToken = refreshTokenService.generateRefreshToken(user, refreshTokenDurationMs);

        assertNotNull(refreshToken);
        assertTrue(new BCryptPasswordEncoder().matches(refreshToken, user.getMetadata().get(0).getRefreshTokenHash()));
        assertNotNull(user.getMetadata().get(0).getRefreshTokenExpiryDate());
        assertTrue(user.getMetadata().get(0).getRefreshTokenExpiryDate().isAfter(Instant.now()));

        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testGenerateRefreshTokenWithNullMetadata() {
        user.setMetadata(null);
        long refreshTokenDurationMs = 3600000;
        String refreshToken = refreshTokenService.generateRefreshToken(user, refreshTokenDurationMs);

        assertNotNull(refreshToken);
        assertNotNull(user.getMetadata());
        assertTrue(new BCryptPasswordEncoder().matches(refreshToken, user.getMetadata().get(0).getRefreshTokenHash()));
        assertNotNull(user.getMetadata().get(0).getRefreshTokenExpiryDate());
        assertTrue(user.getMetadata().get(0).getRefreshTokenExpiryDate().isAfter(Instant.now()));

        verify(userRepository, times(1)).save(user);
    }
}
