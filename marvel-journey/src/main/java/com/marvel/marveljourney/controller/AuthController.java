package com.marvel.marveljourney.controller;

import com.marvel.marveljourney.model.User;
import com.marvel.marveljourney.service.UserService;
import com.marvel.marveljourney.util.JwtUtil;
import com.marvel.marveljourney.util.PasswordValidatorUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@Tag(name = "Auth", description = "APIs de autenticação")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordValidatorUtil passwordValidatorUtil;

    @Value("${jwt.expiration.time}")
    private long jwtExpirationTime;

    private static final String ISSUER = "seu-servidor";
    private static final String AUDIENCE = "seu-aplicativo";

    @Operation(summary = "Registrar um novo usuário")
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (userService.findByEmail(user.getEmail()).isPresent()) {
            logger.warn("Tentativa de registro com email já existente: {}", user.getEmail());
            return ResponseEntity.badRequest().body("Email já registrado.");
        }

        if (!passwordValidatorUtil.validate(user.getPasswordHash())) {
            logger.warn("Tentativa de registro com senha fraca: {}", user.getEmail());
            return ResponseEntity.badRequest().body(
                    "Senha fraca. " + String.join(", ", passwordValidatorUtil.getMessages(user.getPasswordHash())));
        }

        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        user.setStatus("active");
        user.setRoles(List.of("user"));
        user.setLoginAttempts(new User.LoginAttempts());
        user.setMetadata(new User.Metadata());

        userService.saveUser(user);
        logger.info("Novo usuário registrado: {}", user.getEmail());

        // Gerar token JWT
        String token = jwtUtil.generateToken(user.getEmail(), jwtExpirationTime, ISSUER, AUDIENCE);

        return ResponseEntity.ok(token);
    }

    @Operation(summary = "Login de um usuário")
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginRequest) {
        var userOptional = userService.findByEmail(loginRequest.getEmail());

        if (userOptional.isEmpty()) {
            logger.warn("Tentativa de login com email não registrado: {}", loginRequest.getEmail());
            return ResponseEntity.status(401).body("Credenciais inválidas.");
        }

        var user = userOptional.get();

        if (userService.isAccountLocked(user)) {
            logger.warn("Tentativa de login com conta bloqueada: {}", loginRequest.getEmail());
            return ResponseEntity.status(403).body("Conta bloqueada. Tente novamente mais tarde.");
        }

        if (!userService.validatePassword(loginRequest.getPasswordHash(), user.getPasswordHash())) {
            userService.increaseFailedAttempts(user);
            logger.warn("Tentativa de login com senha inválida para o email: {}", loginRequest.getEmail());
            return ResponseEntity.status(401).body("Credenciais inválidas.");
        }

        userService.resetFailedAttempts(user);

        if (user.getMetadata() == null) {
            user.setMetadata(new User.Metadata());
        }

        user.getMetadata().setLastLoginAt(Instant.now());
        userService.updateUser(user);

        if (user.isMfaEnabled()) {
            logger.info("Login bem-sucedido com MFA requerido para o usuário: {}", user.getEmail());
            return ResponseEntity.ok("MFA_REQUIRED");
        }

        String token = jwtUtil.generateToken(user.getEmail(), jwtExpirationTime, ISSUER, AUDIENCE);
        logger.info("Login bem-sucedido para o usuário: {}", user.getEmail());
        return ResponseEntity.ok(token);
    }

    @Operation(summary = "Habilitar MFA para um usuário")
    @PostMapping("/enable-mfa")
    public ResponseEntity<?> enableMfa(@RequestBody User user) {
        String secret = userService.enableMfa(user);
        logger.info("MFA habilitado para o usuário: {}", user.getEmail());
        return ResponseEntity.ok(secret);
    }

    @Operation(summary = "Verificar MFA para um usuário")
    @PostMapping("/verify-mfa")
    public ResponseEntity<?> verifyMfa(@RequestBody User user, @RequestParam int code) {
        if (userService.verifyMfa(user, code)) {
            String token = jwtUtil.generateToken(user.getEmail(), jwtExpirationTime, ISSUER, AUDIENCE);
            logger.info("MFA verificado com sucesso para o usuário: {}", user.getEmail());
            return ResponseEntity.ok(token);
        } else {
            logger.warn("Tentativa de verificação MFA falhou para o usuário: {}", user.getEmail());
            return ResponseEntity.status(401).body("Código MFA inválido.");
        }
    }
}