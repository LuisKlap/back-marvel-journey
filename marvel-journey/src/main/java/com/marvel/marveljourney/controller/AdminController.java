package com.marvel.marveljourney.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin", description = "APIs de administrador")
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Operation(summary = "Obter dashboard do administrador")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public ResponseEntity<?> getAdminDashboard() {
        // Implementar lógica do dashboard do administrador
        return ResponseEntity.ok("Dashboard do administrador");
    }
}