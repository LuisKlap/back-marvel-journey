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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @Mock
    private UserDetails userDetails;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setEmail("test@example.com");
    }

    @Test
    void testGetUserProfile_UserFound() {
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        ResponseEntity<?> response = userController.getUserProfile(userDetails);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(user, response.getBody());
    }

    @Test
    void testGetUserProfile_UserNotFound() {
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userService.findByEmail("test@example.com")).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.getUserProfile(userDetails);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Usuário não encontrado.", response.getBody());
    }
}