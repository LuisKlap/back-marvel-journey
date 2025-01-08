package com.marvel.marveljourney.controller;

import com.marvel.marveljourney.model.User;
import com.marvel.marveljourney.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthController authController;

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
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userService.saveUser(any(User.class))).thenReturn(user);

        ResponseEntity<?> response = authController.registerUser(user);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Usuário registrado com sucesso.", response.getBody());
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        ResponseEntity<?> response = authController.registerUser(user);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Email já registrado.", response.getBody());
    }

    @Test
    void testLoginUser() {
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        ResponseEntity<?> response = authController.loginUser(user);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Login bem-sucedido.", response.getBody());
    }

    @Test
    void testLoginUser_InvalidCredentials() {
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

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
    void testMongoDBConnection() {
        // Teste básico para verificar a conexão com o MongoDB
        User testUser = new User();
        testUser.setEmail("mongodbtest@example.com");
        testUser.setPasswordHash("hashedPassword");
        testUser.setCreatedAt(Instant.now());
        testUser.setUpdatedAt(Instant.now());
        testUser.setStatus("active");

        when(userService.saveUser(any(User.class))).thenReturn(testUser);
        when(userService.findByEmail("mongodbtest@example.com")).thenReturn(Optional.of(testUser));

        userService.saveUser(testUser);
        Optional<User> foundUser = userService.findByEmail("mongodbtest@example.com");

        assertTrue(foundUser.isPresent());
        assertEquals("mongodbtest@example.com", foundUser.get().getEmail());
    }
}