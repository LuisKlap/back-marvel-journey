package com.marvel.marveljourney.service;

import com.marvel.marveljourney.model.User;
import com.marvel.marveljourney.repository.UserRepository;
import com.marvel.marveljourney.util.MfaUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private MfaUtil mfaUtil;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindByEmail() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Optional<User> foundUser = userService.findByEmail(email);

        assertTrue(foundUser.isPresent());
        assertEquals(email, foundUser.get().getEmail());
    }

    @Test
    void testSaveUser() {
        User user = new User();
        user.setEmail("test@example.com");
        when(userRepository.save(user)).thenReturn(user);

        User savedUser = userService.saveUser(user);

        assertNotNull(savedUser);
        assertEquals("test@example.com", savedUser.getEmail());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        user.setEmail("test@example.com");
        when(userRepository.save(user)).thenReturn(user);

        User updatedUser = userService.updateUser(user);

        assertNotNull(updatedUser);
        assertEquals("test@example.com", updatedUser.getEmail());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testValidatePassword() {
        String rawPassword = "password";
        String encodedPassword = "encodedPassword";
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        boolean isValid = userService.validatePassword(rawPassword, encodedPassword);

        assertTrue(isValid);
        verify(passwordEncoder, times(1)).matches(rawPassword, encodedPassword);
    }

    @Test
    void testEnableMfa() {
        User user = new User();
        user.setEmail("test@example.com");
        String secret = "secret";
        when(mfaUtil.generateSecretKey()).thenReturn(secret);

        String returnedSecret = userService.enableMfa(user);

        assertEquals(secret, returnedSecret);
        assertTrue(user.isMfaEnabled());
        assertEquals(secret, user.getMfaSecret());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testVerifyMfa() {
        User user = new User();
        user.setMfaSecret("secret");
        int code = 123456;
        when(mfaUtil.validateCode("secret", code)).thenReturn(true);

        boolean isValid = userService.verifyMfa(user, code);

        assertTrue(isValid);
        verify(mfaUtil, times(1)).validateCode("secret", code);
    }
}