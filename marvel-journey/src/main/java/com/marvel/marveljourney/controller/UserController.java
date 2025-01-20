package com.marvel.marveljourney.controller;

import com.marvel.marveljourney.model.User;
import com.marvel.marveljourney.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "User", description = "APIs de usuário")
@PreAuthorize("hasAuthority('ROLE_USER')")
@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Operation(summary = "Obter perfil do usuário", description = "Retorna o perfil do usuário logado")
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            logger.warn("UserDetails está nulo");
            return ResponseEntity.status(401).body("Usuário não autenticado.");
        }

        logger.debug("Iniciando getUserProfile para o usuário: {}", userDetails.getUsername());
        try {
            User user = userService.findByEmail(userDetails.getUsername()).orElse(null);
            if (user == null) {
                logger.warn("Usuário não encontrado: {}", userDetails.getUsername());
                return ResponseEntity.status(404).body("Usuário não encontrado.");
            }
            logger.debug("Usuário encontrado: {}", user);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            logger.error("Erro ao obter perfil do usuário", e);
            return ResponseEntity.status(500).body("Erro interno do servidor");
        }
    }
}