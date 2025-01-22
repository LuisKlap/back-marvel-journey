package com.marvel.marveljourney.controller;

import com.marvel.marveljourney.model.User;
import com.marvel.marveljourney.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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
    void testGetUserProfile_UserFound() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setRoles(List.of("ROLE_USER"));

        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userService.findByEmail(anyString())).thenReturn(user);

        ResponseEntity<?> response = userController.getUserProfile(userDetails);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(user, response.getBody());
        assertEquals(List.of("ROLE_USER"), ((User) response.getBody()).getRoles());
    }

    @Test
    void testGetUserProfile_UserNotFound() {
        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userService.findByEmail(anyString())).thenReturn(null);

        ResponseEntity<?> response = userController.getUserProfile(userDetails);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Usuário não encontrado.", response.getBody());
    }

    @Test
    void testGetUserProfile_Exception() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer test-token");

        when(userDetails.getUsername()).thenReturn("test@example.com");
        when(userService.findByEmail(anyString())).thenThrow(new RuntimeException("Erro ao obter perfil"));

        ResponseEntity<?> response = userController.getUserProfile(userDetails);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Erro interno do servidor", response.getBody());
    }
}