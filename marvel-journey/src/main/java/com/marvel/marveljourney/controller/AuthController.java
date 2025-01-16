package com.marvel.marveljourney.controller;

import com.marvel.marveljourney.dto.LoginRequest;
import com.marvel.marveljourney.dto.RegisterRequest;
import com.marvel.marveljourney.dto.MfaRequest;
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
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            if (userService.findByEmail(registerRequest.getEmail()).isPresent()) {
                logger.warn("Tentativa de registro com email já existente: {}", registerRequest.getEmail());
                return ResponseEntity.badRequest().body("Email já registrado.");
            }

            if (!passwordValidatorUtil.validate(registerRequest.getPassword())) {
                logger.warn("Tentativa de registro com senha fraca: {}", registerRequest.getEmail());
                return ResponseEntity.badRequest().body(
                        "Senha fraca. " + String.join(", ", passwordValidatorUtil.getMessages(registerRequest.getPassword())));
            }

            User user = new User();
            user.setEmail(registerRequest.getEmail());
            user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
            user.setCreatedAt(Instant.now());
            user.setUpdatedAt(Instant.now());
            user.setTermsAcceptedAt(Instant.now()); // Adicionando a propriedade termsAcceptedAt
            user.setStatus("active");
            user.setRoles(List.of("user"));
            user.setLoginAttempts(new User.LoginAttempts());
            user.setMetadata(new User.Metadata());
            user.setIsTest(false);

            userService.saveUser(user);
            logger.info("Novo usuário registrado: {}", user.getEmail());

            String token = jwtUtil.generateToken(user.getEmail(), jwtExpirationTime, ISSUER, AUDIENCE);
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            logger.error("Erro ao registrar usuário", e);
            return ResponseEntity.status(500).body("Erro interno do servidor");
        }
    }

    @Operation(summary = "Login de um usuário")
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
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

            if (!userService.validatePassword(loginRequest.getPassword(), user.getPasswordHash())) {
                userService.increaseFailedAttempts(user);
                logger.warn("Tentativa de login com senha inválida para o email: {}", loginRequest.getEmail());
                return ResponseEntity.status(401).body("Credenciais inválidas.");
            }

            userService.resetFailedAttempts(user);
            userService.updateMetadata(user, loginRequest.getIpAddress(), loginRequest.getUserAgent());

            if (user.isMfaEnabled()) {
                logger.info("Login bem-sucedido com MFA requerido para o usuário: {}", user.getEmail());
                return ResponseEntity.ok("MFA_REQUIRED");
            }

            String token = jwtUtil.generateToken(user.getEmail(), jwtExpirationTime, ISSUER, AUDIENCE);
            logger.info("Login bem-sucedido para o usuário: {}", user.getEmail());
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            logger.error("Erro ao fazer login", e);
            return ResponseEntity.status(500).body("Erro interno do servidor");
        }
    }

    @Operation(summary = "Habilitar MFA para um usuário")
    @PostMapping("/enable-mfa")
    public ResponseEntity<?> enableMfa(@RequestBody MfaRequest mfaRequest) {
        try {
            var userOptional = userService.findByEmail(mfaRequest.getEmail());
            if (userOptional.isEmpty()) {
                logger.warn("Tentativa de habilitar MFA para email não registrado: {}", mfaRequest.getEmail());
                return ResponseEntity.status(404).body("Usuário não encontrado.");
            }

            var user = userOptional.get();
            String secret = userService.enableMfa(user);
            logger.info("MFA habilitado para o usuário: {}", user.getEmail());
            return ResponseEntity.ok(secret);
        } catch (Exception e) {
            logger.error("Erro ao habilitar MFA", e);
            return ResponseEntity.status(500).body("Erro interno do servidor");
        }
    }

    @Operation(summary = "Verificar MFA para um usuário")
    @PostMapping("/verify-mfa")
    public ResponseEntity<?> verifyMfa(@RequestBody MfaRequest mfaRequest, @RequestParam int code) {
        try {
            var userOptional = userService.findByEmail(mfaRequest.getEmail());
            if (userOptional.isEmpty()) {
                logger.warn("Tentativa de verificação MFA para email não registrado: {}", mfaRequest.getEmail());
                return ResponseEntity.status(404).body("Usuário não encontrado.");
            }

            var user = userOptional.get();
            if (userService.verifyMfa(user, code)) {
                String token = jwtUtil.generateToken(user.getEmail(), jwtExpirationTime, ISSUER, AUDIENCE);
                logger.info("MFA verificado com sucesso para o usuário: {}", user.getEmail());
                return ResponseEntity.ok(token);
            } else {
                logger.warn("Tentativa de verificação MFA falhou para o usuário: {}", user.getEmail());
                return ResponseEntity.status(401).body("Código MFA inválido.");
            }
        } catch (Exception e) {
            logger.error("Erro ao verificar MFA", e);
            return ResponseEntity.status(500).body("Erro interno do servidor");
        }
    }
}