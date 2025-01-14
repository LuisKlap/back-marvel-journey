package com.marvel.marveljourney.controller;

import com.marvel.marveljourney.model.User;
import com.marvel.marveljourney.service.UserService;
import com.marvel.marveljourney.util.JwtUtil;
import com.marvel.marveljourney.util.PasswordValidatorUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private UserService userService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private PasswordValidatorUtil passwordValidatorUtil;

    @Mock
    private JwtUtil jwtUtil;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedPassword");
        user.setTermsAcceptedAt(Instant.now());
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user.setStatus("active");
    }

    @Test
    void testRegisterUser() {
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordValidatorUtil.validate(anyString())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(jwtUtil.generateToken(anyString(), anyLong(), anyString(), anyString())).thenReturn("jwtToken");
        doAnswer(invocation -> null).when(userService).saveUser(any(User.class));

        ResponseEntity<?> response = authController.registerUser(user);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("jwtToken", response.getBody());
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        ResponseEntity<?> response = authController.registerUser(user);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Email já registrado.", response.getBody());
    }

    @Test
    void testRegisterUser_WeakPassword() {
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordValidatorUtil.validate(anyString())).thenReturn(false);
        when(passwordValidatorUtil.getMessages(anyString())).thenReturn(List.of("Senha deve ter pelo menos 8 caracteres."));

        ResponseEntity<?> response = authController.registerUser(user);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Senha fraca. Senha deve ter pelo menos 8 caracteres.", response.getBody());
    }

    @Test
    void testLoginUser() {
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userService.validatePassword(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyLong(), anyString(), anyString())).thenReturn("jwtToken");

        ResponseEntity<?> response = authController.loginUser(user);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("jwtToken", response.getBody());
    }

    @Test
    void testLoginUser_InvalidCredentials() {
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userService.validatePassword(anyString(), anyString())).thenReturn(false);

        ResponseEntity<?> response = authController.loginUser(user);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Credenciais inválidas.", response.getBody());
    }

    @Test
    void testLoginUser_UserNotFound() {
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.empty());

        ResponseEntity<?> response = authController.loginUser(user);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Credenciais inválidas.", response.getBody());
    }

    @Test
    void testLoginUser_AccountLocked() {
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(userService.isAccountLocked(any(User.class))).thenReturn(true);

        ResponseEntity<?> response = authController.loginUser(user);

        assertEquals(403, response.getStatusCode().value());
        assertEquals("Conta bloqueada. Tente novamente mais tarde.", response.getBody());
    }

    @Test
    void testEnableMfa() {
        when(userService.enableMfa(any(User.class))).thenReturn("secret");

        ResponseEntity<?> response = authController.enableMfa(user);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("secret", response.getBody());
    }

    @Test
    void testVerifyMfa_Success() {
        when(userService.verifyMfa(any(User.class), anyInt())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyLong(), anyString(), anyString())).thenReturn("jwtToken");

        ResponseEntity<?> response = authController.verifyMfa(user, 123456);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("jwtToken", response.getBody());
    }

    @Test
    void testVerifyMfa_Failure() {
        when(userService.verifyMfa(any(User.class), anyInt())).thenReturn(false);

        ResponseEntity<?> response = authController.verifyMfa(user, 123456);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Código MFA inválido.", response.getBody());
    }
}