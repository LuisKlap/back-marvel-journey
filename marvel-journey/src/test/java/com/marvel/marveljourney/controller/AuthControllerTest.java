package com.marvel.marveljourney.controller;

import com.marvel.marveljourney.config.EmailConfig;
import com.marvel.marveljourney.dto.LoginRequest;
import com.marvel.marveljourney.dto.RegisterRequest;
import com.marvel.marveljourney.model.User;
import com.marvel.marveljourney.service.EmailService;
import com.marvel.marveljourney.service.UserService;
import com.marvel.marveljourney.util.JwtUtil;
import com.marvel.marveljourney.util.PasswordValidatorUtil;

import jakarta.mail.MessagingException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(EmailConfig.class)
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private PasswordValidatorUtil passwordValidatorUtil;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser_Success() throws MessagingException {
        RegisterRequest registerRequest = new RegisterRequest(null, null);
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("Strong@Password123");

        when(userService.findByEmail(anyString())).thenReturn(null);
        when(passwordValidatorUtil.validate(anyString())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        ResponseEntity<?> response = authController.registerUser(registerRequest);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Registro bem-sucedido. Verifique seu email para o código de verificação.", response.getBody());
        verify(userService, times(1)).saveUser(any(User.class));
        verify(emailService, times(1)).sendVerificationEmail(anyString(), anyString());
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        RegisterRequest registerRequest = new RegisterRequest(null, null);
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("StrongPassword123");

        when(userService.findByEmail(anyString())).thenReturn(new User());

        ResponseEntity<?> response = authController.registerUser(registerRequest);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Email já registrado.", response.getBody());
    }

    @Test
    void testRegisterUser_WeakPassword() {
        RegisterRequest registerRequest = new RegisterRequest(null, null);
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("weak");

        when(userService.findByEmail(anyString())).thenReturn(null);
        when(passwordValidatorUtil.validate(anyString())).thenReturn(false);
        when(passwordValidatorUtil.getMessages(anyString())).thenReturn(List.of("Password is too weak"));

        ResponseEntity<?> response = authController.registerUser(registerRequest);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Senha fraca. Password is too weak", response.getBody());
    }

    @Test
    void testLoginUser_InvalidCredentials() {
        LoginRequest loginRequest = new LoginRequest(null, null, null, null);
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("WrongPassword");

        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("encodedPassword");

        when(userService.findByEmail(anyString())).thenReturn(user);
        when(userService.validatePassword(anyString(), anyString())).thenReturn(false);

        ResponseEntity<?> response = authController.loginUser(loginRequest);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Credenciais inválidas.", response.getBody());
    }
}