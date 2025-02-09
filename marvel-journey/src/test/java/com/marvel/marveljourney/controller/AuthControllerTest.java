package com.marvel.marveljourney.controller;

import com.marvel.marveljourney.dto.LoginRequest;
import com.marvel.marveljourney.dto.RegisterRequest;
import com.marvel.marveljourney.dto.VerificationRequest;
import com.marvel.marveljourney.dto.MfaRequest;
import com.marvel.marveljourney.exception.UserNotFoundException;
import com.marvel.marveljourney.model.User;
import com.marvel.marveljourney.service.EmailService;
import com.marvel.marveljourney.service.UserService;
import com.marvel.marveljourney.service.TwoFactorAuthService;
import com.marvel.marveljourney.service.RefreshTokenService;
import com.marvel.marveljourney.util.JwtUtil;
import com.marvel.marveljourney.util.PasswordValidatorUtil;

import dev.samstevens.totp.exceptions.QrGenerationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordValidatorUtil passwordValidatorUtil;

    @Mock
    private TwoFactorAuthService twoFactorAuthService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser() {
        RegisterRequest registerRequest = new RegisterRequest(
                "test@example.com",
                "StrongPassword123",
                true,
                true,
                "device",
                "127.0.0.1",
                "userAgent");

        when(userService.findByEmail(registerRequest.getEmail())).thenReturn(null);
        when(passwordValidatorUtil.validate(registerRequest.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");

        ResponseEntity<?> response = authController.registerUser(registerRequest);

        assertEquals(200, response.getStatusCode().value());
        verify(userService, times(1)).saveUser(any(User.class));
        verify(emailService, times(1)).sendVerificationEmail(eq(registerRequest.getEmail()), anyString());
    }

    @Test
    void testVerifyEmail() {
        VerificationRequest verificationRequest = new VerificationRequest("test@example.com", "123456");

        User user = new User();
        User.VerificationCode verificationCode = new User.VerificationCode();
        verificationCode.setCode("123456");
        verificationCode.setCreatedAt(Instant.now());
        user.setVerificationCode(verificationCode);

        when(userService.findByEmail(verificationRequest.getEmail())).thenReturn(user);

        ResponseEntity<?> response = authController.verifyEmail(verificationRequest);

        assertEquals(200, response.getStatusCode().value());
        verify(userService, times(1)).verifyEmail(verificationRequest.getEmail());
    }

    @Test
    void testVerifyEmail_UserNotFound() {
        VerificationRequest verificationRequest = new VerificationRequest("test@example.com", "123456");

        when(userService.findByEmail(verificationRequest.getEmail())).thenReturn(null);

        ResponseEntity<?> response = authController.verifyEmail(verificationRequest);

        assertEquals(404, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("USER_NOT_FOUND", responseBody.get("error"));
    }

    @Test
    void testVerifyEmail_CodeExpired() {
        VerificationRequest verificationRequest = new VerificationRequest("test@example.com", "123456");

        User user = new User();
        User.VerificationCode verificationCode = new User.VerificationCode();
        verificationCode.setCode("123456");
        verificationCode.setCreatedAt(Instant.now().minusSeconds(3600));
        user.setVerificationCode(verificationCode);

        when(userService.findByEmail(verificationRequest.getEmail())).thenReturn(user);

        ResponseEntity<?> response = authController.verifyEmail(verificationRequest);

        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("EXPIRED_VERIFICATION_CODE", responseBody.get("error"));
    }

    @Test
    void testLoginUser() {
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password", null, null);

        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("encodedPassword");
        user.setRoles(List.of("ROLE_USER"));

        when(userService.findByEmail(loginRequest.getEmail())).thenReturn(user);
        when(userService.validatePassword(loginRequest.getPassword(), user.getPasswordHash())).thenReturn(true);
        when(userService.emailIsVerified(user.getEmail())).thenReturn(true);
        when(jwtUtil.generateToken(eq(user.getEmail()), anyLong(), anyString(), anyString(), eq(user.getRoles())))
                .thenReturn("jwtToken");
        when(refreshTokenService.generateRefreshToken(eq(user), anyLong())).thenReturn("refreshToken");

        ResponseEntity<?> response = authController.loginUser(loginRequest);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("jwtToken", responseBody.get("token"));
        assertEquals("refreshToken", responseBody.get("refreshToken"));
    }

    @Test
    void testRefreshToken() {
        Map<String, String> request = Map.of("refreshToken", "validRefreshToken");

        User user = new User();
        user.setEmail("test@example.com");
        user.setRoles(List.of("ROLE_USER"));
        User.Metadata metadata = new User.Metadata();
        metadata.setRefreshTokenHash("hashedRefreshToken");
        metadata.setRefreshTokenExpiryDate(Instant.now().plusSeconds(3600));
        user.setMetadata(List.of(metadata));

        when(userService.findByRefreshToken(request.get("refreshToken"))).thenReturn(user);
        when(passwordEncoder.matches(eq(request.get("refreshToken")), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(eq(user.getEmail()), anyLong(), anyString(), anyString(), eq(user.getRoles())))
                .thenReturn("newJwtToken");
        when(refreshTokenService.generateRefreshToken(eq(user), anyLong())).thenReturn("newRefreshToken");

        ResponseEntity<?> response = authController.refreshToken(request);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("newJwtToken", responseBody.get("token"));
        assertEquals("newRefreshToken", responseBody.get("refreshToken"));
    }

    @Test
    void testSetup2FA() throws QrGenerationException {
        MfaRequest mfaRequest = new MfaRequest("test@example.com");

        when(twoFactorAuthService.generateSecret()).thenReturn("secret");
        when(twoFactorAuthService.generateQrCodeImage(eq("secret"), eq(mfaRequest.getEmail()))).thenReturn(new byte[0]);

        ResponseEntity<byte[]> response = authController.setup2FA(mfaRequest);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(MediaType.IMAGE_PNG, response.getHeaders().getContentType());
    }

    @Test
    void testVerify2FA() {
        VerificationRequest verificationRequest = new VerificationRequest("test@example.com", "123456");

        when(userService.getUserSecret(verificationRequest.getEmail())).thenReturn("secret");
        when(twoFactorAuthService.verifyCode(eq("secret"), eq(verificationRequest.getCode()))).thenReturn(true);

        ResponseEntity<?> response = authController.verify2FA(verificationRequest);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Code successfully verified", response.getBody());
    }

    @Test
    void testVerify2FA_UserNotFound() {
        VerificationRequest verificationRequest = new VerificationRequest("test@example.com", "123456");

        when(userService.getUserSecret(verificationRequest.getEmail()))
                .thenThrow(new UserNotFoundException("Usuário não encontrado"));

        ResponseEntity<?> response = authController.verify2FA(verificationRequest);

        assertEquals(404, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof Map);
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("USER_NOT_FOUND", responseBody.get("error"));
    }

    @Test
    void testLogoutUser() {
        Map<String, String> request = Map.of("refreshToken", "validRefreshToken");

        doNothing().when(userService).logoutUser(request.get("refreshToken"));

        ResponseEntity<?> response = authController.logoutUser(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Logout bem-sucedido.", response.getBody());
    }

    @Test
    void testSendVerificationCode() {
        VerificationRequest verificationRequest = new VerificationRequest("test@example.com", null);

        doNothing().when(userService).updateVerificationCode(anyString(), anyString(), any(Instant.class));
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString());

        ResponseEntity<?> response = authController.sendVerificationCode(verificationRequest);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Verification code sent successfully.", ((Map<String, String>) response.getBody()).get("message"));

        verify(userService, times(1)).updateVerificationCode(eq("test@example.com"), anyString(), any(Instant.class));
        verify(emailService, times(1)).sendVerificationEmail(eq("test@example.com"), anyString());
    }
}