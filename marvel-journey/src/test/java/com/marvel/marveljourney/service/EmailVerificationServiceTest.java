package com.marvel.marveljourney.service;

import com.marvel.marveljourney.dto.VerificationRequest;
import com.marvel.marveljourney.exception.UserNotFoundException;
import com.marvel.marveljourney.model.User;
import com.marvel.marveljourney.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class EmailVerificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendVerificationCode() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString());

        emailVerificationService.sendVerificationCode(email);

        verify(emailService, times(1)).sendVerificationEmail(eq(email), anyString());
    }

    @Test
    void testUpdateVerificationCode() {
        String email = "test@example.com";
        String code = "123456";
        Instant createdAt = Instant.now();
        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        emailVerificationService.updateVerificationCode(email, code, createdAt);

        verify(userRepository, times(1)).save(user);
        assertEquals(code, user.getVerificationCode().getCode());
        assertEquals(createdAt, user.getVerificationCode().getCreatedAt());
    }

    @Test
    void testVerifyEmail_Success() {
        String email = "test@example.com";
        String code = "123456";
        User user = new User();
        user.setEmail(email);
        User.VerificationCode verificationCode = new User.VerificationCode();
        verificationCode.setCode(code);
        verificationCode.setCreatedAt(Instant.now());
        user.setVerificationCode(verificationCode);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        VerificationRequest request = new VerificationRequest(email, code);
        Map<String, String> response = emailVerificationService.verifyEmail(request);

        assertEquals("Email successfully verified.", response.get("message"));
        verify(userRepository, times(1)).verifyEmail(email);
    }

    @Test
    void testVerifyEmail_UserNotFound() {
        String email = "test@example.com";
        String code = "123456";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        VerificationRequest request = new VerificationRequest(email, code);

        assertThrows(UserNotFoundException.class, () -> emailVerificationService.verifyEmail(request));
    }

    @Test
    void testVerifyEmail_InvalidCode() {
        String email = "test@example.com";
        String code = "123456";
        User user = new User();
        user.setEmail(email);
        User.VerificationCode verificationCode = new User.VerificationCode();
        verificationCode.setCode("654321");
        verificationCode.setCreatedAt(Instant.now());
        user.setVerificationCode(verificationCode);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        VerificationRequest request = new VerificationRequest(email, code);

        assertThrows(IllegalArgumentException.class, () -> emailVerificationService.verifyEmail(request));
    }

    @Test
    void testVerifyEmail_ExpiredCode() {
        String email = "test@example.com";
        String code = "123456";
        User user = new User();
        user.setEmail(email);
        User.VerificationCode verificationCode = new User.VerificationCode();
        verificationCode.setCode(code);
        verificationCode.setCreatedAt(Instant.now().minusSeconds(3600)); // 1 hour ago
        user.setVerificationCode(verificationCode);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        VerificationRequest request = new VerificationRequest(email, code);

        assertThrows(IllegalArgumentException.class, () -> emailVerificationService.verifyEmail(request));
    }
}
