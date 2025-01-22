package com.marvel.marveljourney.controller;

import com.marvel.marveljourney.config.TestConfig;
import com.marvel.marveljourney.dto.LoginRequest;
import com.marvel.marveljourney.dto.RegisterRequest;
import com.marvel.marveljourney.dto.MfaRequest;
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
import static org.mockito.Mockito.*;

@SpringBootTest
@Import(TestConfig.class)
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
    void testLoginUser_Success() {
        LoginRequest loginRequest = new LoginRequest(null, null, null, null);
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("StrongPassword123");
    
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("encodedPassword");
        user.setRoles(List.of("ROLE_USER"));
        user.setMfaEnabled(false);
    
        when(userService.findByEmail(anyString())).thenReturn(user);
        when(userService.validatePassword(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyLong(), anyString(), anyString(), anyList())).thenReturn("jwtToken");
    
        ResponseEntity<?> response = authController.loginUser(loginRequest);
    
        assertEquals(200, response.getStatusCode().value());
        assertEquals("jwtToken", response.getBody());
        verify(jwtUtil).generateToken(eq("test@example.com"), anyLong(), eq("seu-servidor"), eq("seu-aplicativo"), eq(List.of("ROLE_USER")));
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

    @Test
    void testEnableMfa_Success() {
        MfaRequest mfaRequest = new MfaRequest(null);
        mfaRequest.setEmail("test@example.com");

        User user = new User();
        user.setEmail("test@example.com");

        when(userService.findByEmail(anyString())).thenReturn(user);
        when(userService.enableMfa(any(User.class))).thenReturn("secret");

        ResponseEntity<?> response = authController.enableMfa(mfaRequest);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("secret", response.getBody());
    }

    @Test
    void testVerifyMfa_Success() {
        MfaRequest mfaRequest = new MfaRequest(null);
        mfaRequest.setEmail("test@example.com");
    
        User user = new User();
        user.setEmail("test@example.com");
        user.setRoles(List.of("ROLE_USER"));
    
        when(userService.findByEmail(anyString())).thenReturn(user);
        when(userService.verifyMfa(any(User.class), anyInt())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyLong(), anyString(), anyString(), anyList())).thenReturn("jwtToken");
    
        ResponseEntity<?> response = authController.verifyMfa(mfaRequest, 123456);
    
        assertEquals(200, response.getStatusCode().value());
        assertEquals("jwtToken", response.getBody());
        verify(jwtUtil).generateToken(eq("test@example.com"), anyLong(), eq("seu-servidor"), eq("seu-aplicativo"), eq(List.of("ROLE_USER")));
    }

    @Test
    void testVerifyMfa_InvalidCode() {
        MfaRequest mfaRequest = new MfaRequest(null);
        mfaRequest.setEmail("test@example.com");

        User user = new User();
        user.setEmail("test@example.com");

        when(userService.findByEmail(anyString())).thenReturn(user);
        when(userService.verifyMfa(any(User.class), anyInt())).thenReturn(false);

        ResponseEntity<?> response = authController.verifyMfa(mfaRequest, 123456);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Código MFA inválido.", response.getBody());
    }
}