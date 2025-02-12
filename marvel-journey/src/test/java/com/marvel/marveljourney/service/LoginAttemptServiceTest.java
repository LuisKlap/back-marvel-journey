package com.marvel.marveljourney.service;

import com.marvel.marveljourney.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class LoginAttemptServiceTest {

    @InjectMocks
    private LoginAttemptService loginAttemptService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setEmail("test@example.com");
        user.setFailedLoginAttempts(0);
        user.setLockoutEndTime(null);
    }

    @Test
    void testIncreaseFailedAttempts() {
        loginAttemptService.increaseFailedAttempts(user);
        assertEquals(1, user.getFailedLoginAttempts());

        loginAttemptService.increaseFailedAttempts(user);
        assertEquals(2, user.getFailedLoginAttempts());

        user.setFailedLoginAttempts(4);
        loginAttemptService.increaseFailedAttempts(user);
        assertEquals(5, user.getFailedLoginAttempts());
        assertNotNull(user.getLockoutEndTime());
    }

    @Test
    void testResetFailedAttempts() {
        user.setFailedLoginAttempts(3);
        user.setLockoutEndTime(Instant.now().plusSeconds(60));
        loginAttemptService.resetFailedAttempts(user);
        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLockoutEndTime());
    }

    @Test
    void testIsAccountLocked() {
        assertFalse(loginAttemptService.isAccountLocked(user));

        user.setLockoutEndTime(Instant.now().plusSeconds(60));
        assertTrue(loginAttemptService.isAccountLocked(user));

        user.setLockoutEndTime(Instant.now().minusSeconds(60));
        assertFalse(loginAttemptService.isAccountLocked(user));
    }
}
