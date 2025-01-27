package com.marvel.marveljourney.controller;

import com.marvel.marveljourney.model.User;
import com.marvel.marveljourney.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUserProfile_UserDetailsNull() {
        ResponseEntity<?> response = userController.getUserProfile(null);
        assertEquals(401, response.getStatusCode().value());
        assertEquals("Usuário não autenticado.", response.getBody());
    }

    @Test
    void testGetUserProfile_UserNotFound() {
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(null);

        ResponseEntity<?> response = userController.getUserProfile(userDetails);
        assertEquals(404, response.getStatusCode().value());
        assertEquals("Usuário não encontrado.", response.getBody());
    }

    @Test
    void testGetUserProfile_Success() {
        User user = new User();
        user.setEmail("test@example.com");

        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(user);

        ResponseEntity<?> response = userController.getUserProfile(userDetails);
        assertEquals(200, response.getStatusCode().value());
        assertEquals(user, response.getBody());
    }

    @Test
    void testGetUserProfile_InternalServerError() {
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userService.findByEmail("test@example.com")).thenThrow(new RuntimeException("Erro interno"));

        ResponseEntity<?> response = userController.getUserProfile(userDetails);
        assertEquals(500, response.getStatusCode().value());
        assertEquals("Erro interno do servidor", response.getBody());
    }
}