package com.marvel.marveljourney.controller;

import com.marvel.marveljourney.dto.*;
import com.marvel.marveljourney.exception.ErrorCode;
import com.marvel.marveljourney.exception.UserNotFoundException;
import com.marvel.marveljourney.service.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private UserAuthService userAuthService;

    @Mock
    private EmailVerificationService emailVerificationService;

    @Mock
    private TwoFactorAuthService twoFactorAuthService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest();
        doNothing().when(userAuthService).registerUser(any(RegisterRequest.class));

        ResponseEntity<?> response = authController.registerUser(registerRequest);

        assertEquals(200, response.getStatusCodeValue());
        verify(userAuthService, times(1)).registerUser(any(RegisterRequest.class));
    }

    @Test
    void testVerifyEmail() throws Exception {
        VerificationRequest verificationRequest = new VerificationRequest();
        when(emailVerificationService.verifyEmail(any(VerificationRequest.class))).thenReturn(Map.of("message", "Verification successful"));

        ResponseEntity<?> response = authController.verifyEmail(verificationRequest);

        assertEquals(200, response.getStatusCodeValue());
        verify(emailVerificationService, times(1)).verifyEmail(any(VerificationRequest.class));
    }

    @Test
    void testLoginUser() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        when(userAuthService.loginUser(any(LoginRequest.class), anyLong(), anyString(), anyString(), anyLong()))
                .thenReturn(Map.of("token", "jwt-token"));

        ResponseEntity<?> response = authController.loginUser(loginRequest);

        assertEquals(200, response.getStatusCodeValue());
        verify(userAuthService, times(1)).loginUser(any(LoginRequest.class), anyLong(), anyString(), anyString(), anyLong());
    }

    @Test
    void testRefreshToken() throws Exception {
        Map<String, String> request = Map.of("refreshToken", "some-refresh-token");
        when(refreshTokenService.refreshToken(anyString(), anyLong(), anyString(), anyString(), anyLong()))
                .thenReturn(Map.of("token", "new-jwt-token"));

        ResponseEntity<?> response = authController.refreshToken(request);

        assertEquals(200, response.getStatusCodeValue());
        verify(refreshTokenService, times(1)).refreshToken(anyString(), anyLong(), anyString(), anyString(), anyLong());
    }

    @Test
    void testSetup2FA() throws Exception {
        MfaRequest mfaRequest = new MfaRequest();
        mfaRequest.setEmail("test@example.com");
        when(twoFactorAuthService.setup2FA(anyString())).thenReturn(new byte[0]);

        ResponseEntity<byte[]> response = authController.setup2FA(mfaRequest);

        assertEquals(200, response.getStatusCodeValue());
        verify(twoFactorAuthService, times(1)).setup2FA(anyString());
    }

    @Test
    void testVerify2FA() throws Exception {
        VerificationRequest verificationRequest = new VerificationRequest();
        verificationRequest.setEmail("test@example.com");
        verificationRequest.setCode("123456");
        when(twoFactorAuthService.verify2FA(anyString(), anyString())).thenReturn(true);

        ResponseEntity<?> response = authController.verify2FA(verificationRequest);

        assertEquals(200, response.getStatusCodeValue());
        verify(twoFactorAuthService, times(1)).verify2FA(anyString(), anyString());
    }

    @Test
    void testLogoutUser() throws Exception {
        Map<String, String> request = Map.of("refreshToken", "some-refresh-token");
        doNothing().when(userAuthService).logoutUser(anyString());

        ResponseEntity<?> response = authController.logoutUser(request);

        assertEquals(200, response.getStatusCodeValue());
        verify(userAuthService, times(1)).logoutUser(anyString());
    }

    @Test
    void testSendVerificationCode() throws Exception {
        VerificationRequest verificationRequest = new VerificationRequest();
        verificationRequest.setEmail("test@example.com");
        doNothing().when(emailVerificationService).sendVerificationCode(anyString());

        ResponseEntity<?> response = authController.sendVerificationCode(verificationRequest);

        assertEquals(200, response.getStatusCodeValue());
        verify(emailVerificationService, times(1)).sendVerificationCode(anyString());
    }

    @Test
    void testCheckEmail() throws Exception {
        Map<String, String> request = Map.of("email", "test@example.com");
        when(userService.checkEmailExists(anyString())).thenReturn(true);

        ResponseEntity<?> response = authController.checkEmail(request);

        assertEquals(200, response.getStatusCodeValue());
        verify(userService, times(1)).checkEmailExists(anyString());
    }

    @Test
    void testConfirmPasswordReset() throws Exception {
        PasswordResetRequest passwordResetRequest = new PasswordResetRequest();
        doNothing().when(userAuthService).confirmPasswordReset(any(PasswordResetRequest.class));

        ResponseEntity<?> response = authController.confirmPasswordReset(passwordResetRequest);

        assertEquals(200, response.getStatusCodeValue());
        verify(userAuthService, times(1)).confirmPasswordReset(any(PasswordResetRequest.class));
    }
}