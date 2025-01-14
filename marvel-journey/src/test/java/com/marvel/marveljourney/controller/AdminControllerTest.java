package com.marvel.marveljourney.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class AdminControllerTest {

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAdminDashboard() {
        ResponseEntity<?> response = adminController.getAdminDashboard();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Dashboard do administrador", response.getBody());
    }
}