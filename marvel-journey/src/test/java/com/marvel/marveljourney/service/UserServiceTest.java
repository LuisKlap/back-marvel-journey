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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setEmail("test@example.com");
        user.setFailedLoginAttempts(0);
        user.setMfa(new User.MfaData());
        user.setLoginAttempts(new User.LoginAttempts());
    }

    @Test
    void testFindByEmail() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        User foundUser = userService.findByEmail("test@example.com");
        assertNotNull(foundUser);
        assertEquals("test@example.com", foundUser.getEmail());
    }

    @Test
    void testSaveUser() {
        when(userRepository.save(user)).thenReturn(user);
        User savedUser = userService.saveUser(user);
        assertNotNull(savedUser);
        assertEquals("test@example.com", savedUser.getEmail());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testUpdateUser() {
        when(userRepository.save(user)).thenReturn(user);
        User updatedUser = userService.updateUser(user);
        assertNotNull(updatedUser);
        assertEquals("test@example.com", updatedUser.getEmail());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testValidatePassword() {
        when(passwordEncoder.matches("rawPassword", "encodedPassword")).thenReturn(true);
        boolean isValid = userService.validatePassword("rawPassword", "encodedPassword");
        assertTrue(isValid);
    }

    @Test
    void testIncreaseFailedAttempts() {
        userService.increaseFailedAttempts(user);
        assertEquals(1, user.getFailedLoginAttempts());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testResetFailedAttempts() {
        user.setFailedLoginAttempts(3);
        userService.resetFailedAttempts(user);
        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLockoutEndTime());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testIsAccountLocked() {
        user.setLockoutEndTime(Instant.now().plusMillis(10000));
        assertTrue(userService.isAccountLocked(user));
    }

    @Test
    void testUpdateLoginAttempts() {
        userService.updateLoginAttempts(user);
        assertNotNull(user.getLoginAttempts());
        assertEquals(1, user.getLoginAttempts().getCount());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testResetLoginAttempts() {
        userService.resetLoginAttempts(user);
        assertEquals(0, user.getLoginAttempts().getCount());
        assertNull(user.getLoginAttempts().getLastAttemptAt());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testUpdateMetadata() {
        userService.updateMetadata(user, "127.0.0.1", "Mozilla/5.0");
        assertNotNull(user.getMetadata());
        assertEquals(1, user.getMetadata().size());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testVerifyEmail() {
        userService.verifyEmail("test@example.com");
        verify(userRepository, times(1)).verifyEmail("test@example.com");
    }

    @Test
    void testEmailIsVerified() {
        when(userRepository.emailIsVerified("test@example.com")).thenReturn(true);
        assertTrue(userService.emailIsVerified("test@example.com"));
    }

    @Test
    void testSaveUserSecret() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        userService.saveUserSecret("test@example.com", "secret");
        assertEquals("secret", user.getMfa().getSecret());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testGetUserSecret() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        user.getMfa().setSecret("secret");
        assertEquals("secret", userService.getUserSecret("test@example.com"));
    }

    @Test
    void testFindByRefreshToken() {
        String refreshToken = "validRefreshToken";
        String refreshTokenHash = passwordEncoder.encode(refreshToken);
    
        User.Metadata metadata = new User.Metadata();
        metadata.setRefreshTokenHash(refreshTokenHash);
    
        user.setMetadata(List.of(metadata));
    
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(passwordEncoder.matches(refreshToken, refreshTokenHash)).thenReturn(true);
    
        User foundUser = userService.findByRefreshToken(refreshToken);
    
        assertNotNull(foundUser);
        assertEquals(user, foundUser);
    }
    
    @Test
    void testLogoutUser() {
        String token = "validToken";
        String refreshTokenHash = passwordEncoder.encode(token);
    
        User.Metadata metadata = new User.Metadata();
        metadata.setRefreshTokenHash(refreshTokenHash);
    
        user.setMetadata(List.of(metadata));
    
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(passwordEncoder.matches(token, refreshTokenHash)).thenReturn(true);
    
        userService.logoutUser(token);
    
        assertNull(user.getMetadata().get(0).getRefreshTokenHash());
        assertNull(user.getMetadata().get(0).getRefreshTokenExpiryDate());
        verify(userRepository, times(1)).save(user);
    }
}