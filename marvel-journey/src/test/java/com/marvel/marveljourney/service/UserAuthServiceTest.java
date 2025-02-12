package com.marvel.marveljourney.service;

import com.marvel.marveljourney.dto.LoginRequest;
import com.marvel.marveljourney.dto.RegisterRequest;
import com.marvel.marveljourney.exception.ErrorCode;
import com.marvel.marveljourney.model.User;
import com.marvel.marveljourney.repository.UserRepository;
import com.marvel.marveljourney.util.JwtUtil;
import com.marvel.marveljourney.util.PasswordValidatorUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordValidatorUtil passwordValidatorUtil;

    @Mock
    private LoginAttemptService loginAttemptService;

    @Mock
    private UserMetadataService userMetadataService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private UserAuthService userAuthService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoginUser_Success() {
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password", "", "");

        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedPassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hashedPassword")).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyLong(), anyString(), anyString(), anyList())).thenReturn("jwtToken");
        when(refreshTokenService.generateRefreshToken(any(User.class), anyLong())).thenReturn("refreshToken");
        when(userRepository.emailIsVerified("test@example.com")).thenReturn(true);

        Map<String, String> tokens = userAuthService.loginUser(loginRequest, 3600000, "issuer", "audience", 7200000);

        assertNotNull(tokens);
        assertEquals("jwtToken", tokens.get("token"));
        assertEquals("refreshToken", tokens.get("refreshToken"));
    }

    @Test
    void testLoginUser_InvalidCredentials() {
        LoginRequest loginRequest = new LoginRequest("test@example.com", "password", "", "");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userAuthService.loginUser(loginRequest, 3600000, "issuer", "audience", 7200000);
        });

        assertEquals(ErrorCode.INVALID_CREDENTIALS.getMessage(), exception.getMessage());
    }

    @Test
    void testRegisterUser_Success() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordValidatorUtil.validate("password")).thenReturn(true);
        when(passwordEncoder.encode("password")).thenReturn("hashedPassword");

        userAuthService.registerUser(registerRequest);

        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendVerificationEmail(eq("test@example.com"), anyString());
    }

    @Test
    void testRegisterUser_EmailAlreadyRegistered() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password");

        User existingUser = new User();
        existingUser.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userAuthService.registerUser(registerRequest);
        });

        assertEquals(ErrorCode.EMAIL_ALREADY_REGISTERED.getMessage(), exception.getMessage());
    }

    @Test
    void testLogoutUser_Success() {
        String refreshToken = "validRefreshToken";
        User user = new User();
        user.setEmail("test@example.com");

        when(refreshTokenService.findByRefreshToken(refreshToken)).thenReturn(user);

        userAuthService.logoutUser(refreshToken);

        verify(refreshTokenService, times(1)).deleteRefreshToken(user, refreshToken);
    }

    @Test
    void testLogoutUser_InvalidRefreshToken() {
        String refreshToken = "invalidRefreshToken";

        when(refreshTokenService.findByRefreshToken(refreshToken)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userAuthService.logoutUser(refreshToken);
        });

        assertEquals("Invalid or expired refresh token.", exception.getMessage());
    }
}
