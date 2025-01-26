package com.marvel.marveljourney.controller;

import com.marvel.marveljourney.dto.LoginRequest;
import com.marvel.marveljourney.dto.RegisterRequest;
import com.marvel.marveljourney.dto.VerificationRequest;
import com.marvel.marveljourney.dto.MfaRequest;
import com.marvel.marveljourney.model.User;
import com.marvel.marveljourney.model.User.MfaData;
import com.marvel.marveljourney.model.User.VerificationCode;
import com.marvel.marveljourney.security.VerificationCodeUtil;
import com.marvel.marveljourney.service.EmailService;
import com.marvel.marveljourney.service.UserService;
import com.marvel.marveljourney.service.TwoFactorAuthService;
import com.marvel.marveljourney.service.RefreshTokenService;
import com.marvel.marveljourney.util.JwtUtil;
import com.marvel.marveljourney.util.PasswordValidatorUtil;

import dev.samstevens.totp.exceptions.QrGenerationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    EmailService emailService;

    @Autowired
    private PasswordValidatorUtil passwordValidatorUtil;

    @Autowired
    private TwoFactorAuthService twoFactorAuthService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Value("${jwt.refresh.expiration.time}")
    private long refreshTokenDurationMs;

    @Value("${jwt.expiration.time}")
    private long jwtExpirationTime;

    private static final String ISSUER = "seu-servidor";
    private static final String AUDIENCE = "seu-aplicativo";

    @Operation(summary = "Registrar um novo usuário")
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            User userExist = userService.findByEmail(registerRequest.getEmail());

            if (userExist != null) {
                logger.warn("Tentativa de registro com email já existente: {}", registerRequest.getEmail());
                return ResponseEntity.badRequest().body("Email já registrado.");
            }

            if (!passwordValidatorUtil.validate(registerRequest.getPassword())) {
                logger.warn("Tentativa de registro com senha fraca: {}", registerRequest.getEmail());
                return ResponseEntity.badRequest().body(
                        "Senha fraca. "
                                + String.join(", ", passwordValidatorUtil.getMessages(registerRequest.getPassword())));
            }

            User user = new User();
            user.setEmail(registerRequest.getEmail());
            user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
            user.setCreatedAt(Instant.now());
            user.setUpdatedAt(Instant.now());
            user.setTermsAcceptedAt(Instant.now());
            user.setStatus("active");
            user.setRoles(List.of("ROLE_USER"));
            user.setLoginAttempts(new User.LoginAttempts());
            user.setMetadata(new ArrayList<>());
            user.getMetadata().add(new User.Metadata());

            MfaData mfaData = new MfaData();
            mfaData.setSecret(null);
            mfaData.setEnabled(false);
            user.setMfa(mfaData);

            String verificationCode = VerificationCodeUtil.generateCode();
            emailService.sendVerificationEmail(user.getEmail(), verificationCode);

            VerificationCode verification = new User.VerificationCode();
            verification.setEmailIsVerified(false);
            verification.setCode(verificationCode);
            verification.setCreatedAt(Instant.now());
            user.setVerificationCode(verification);

            userService.saveUser(user);
            return ResponseEntity.ok("Registro bem-sucedido. Verifique seu email para o código de verificação.");
        } catch (Exception e) {
            logger.error("Erro ao registrar usuário", e);
            return ResponseEntity.status(500).body("Erro interno do servidor");
        }
    }

    @Operation(summary = "Verificar código de verificação de email")
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody VerificationRequest verificationRequest) {
        User userExist = userService.findByEmail(verificationRequest.getEmail());

        if (userExist == null) {
            return ResponseEntity.status(404).body("Usuário não encontrado.");
        }

        VerificationCode verificationCode = userExist.getVerificationCode();

        if (verificationCode == null || !verificationCode.getCode().equals(verificationRequest.getCode())) {
            return ResponseEntity.status(400).body("Código de verificação inválido.");
        }

        userService.verifyEmail(verificationRequest.getEmail());
        return ResponseEntity.ok("Email verificado com sucesso.");
    }

    @Operation(summary = "Login de um usuário")
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            User userOptional = userService.findByEmail(loginRequest.getEmail());

            if (userOptional == null) {
                logger.warn("Tentativa de login com email não registrado: {}", loginRequest.getEmail());
                return ResponseEntity.status(401).body("Credenciais inválidas.");
            }

            if (userService.isAccountLocked(userOptional)) {
                logger.warn("Tentativa de login com conta bloqueada: {}", loginRequest.getEmail());
                return ResponseEntity.status(403).body("Conta bloqueada. Tente novamente mais tarde.");
            }

            if (!userService.validatePassword(loginRequest.getPassword(), userOptional.getPasswordHash())) {
                userService.increaseFailedAttempts(userOptional);
                logger.warn("Tentativa de login com senha inválida para o email: {}", loginRequest.getEmail());
                return ResponseEntity.status(401).body("Credenciais inválidas.");
            }

            if (userService.emailIsVerified(userOptional.getEmail()) == false) {
                logger.warn("Tentativa de login com email não verificado: {}", loginRequest.getEmail());
                return ResponseEntity.status(403)
                        .body("Email não verificado. Verifique seu email antes de fazer login.");
            }

            userService.resetFailedAttempts(userOptional);
            userService.updateMetadata(userOptional, loginRequest.getIpAddress(), loginRequest.getUserAgent());

            String token = jwtUtil.generateToken(userOptional.getEmail(), jwtExpirationTime, ISSUER, AUDIENCE,
                    userOptional.getRoles());
            String refreshToken = refreshTokenService.generateRefreshToken(userOptional, refreshTokenDurationMs);
            logger.info("Login bem-sucedido para o usuário: {}", userOptional.getEmail());

            return ResponseEntity.ok(Map.of("token", token, "refreshToken", refreshToken));
        } catch (Exception e) {
            logger.error("Erro ao fazer login", e);
            return ResponseEntity.status(500).body("Erro interno do servidor");
        }
    }

    @Operation(summary = "Renovar o JWT com base no refresh token")
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(400).body("Refresh token é obrigatório.");
        }

        try {
            User user = userService.findByRefreshToken(refreshToken);
            if (user == null) {
                return ResponseEntity.status(401).body("Refresh token inválido ou expirado.");
            }

            boolean isTokenExpired = true;
            for (User.Metadata metadata : user.getMetadata()) {
                if (passwordEncoder.matches(refreshToken, metadata.getRefreshTokenHash()) &&
                    metadata.getRefreshTokenExpiryDate().isAfter(Instant.now())) {
                    isTokenExpired = false;
                    break;
                }
            }

            if (isTokenExpired) {
                return ResponseEntity.status(401).body("Refresh token inválido ou expirado.");
            }

            String token = jwtUtil.generateToken(user.getEmail(), jwtExpirationTime, ISSUER, AUDIENCE, user.getRoles());
            String newRefreshToken = refreshTokenService.generateRefreshToken(user, refreshTokenDurationMs);
            return ResponseEntity.ok(Map.of("token", token, "refreshToken", newRefreshToken));
        } catch (Exception e) {
            logger.error("Erro ao renovar o token", e);
            return ResponseEntity.status(500).body("Erro interno do servidor");
        }
    }

    @Operation(summary = "Configurar autenticação de dois fatores")
    @PostMapping("/setup-2fa")
    public ResponseEntity<byte[]> setup2FA(@RequestBody MfaRequest mfaRequest) {
        String email = mfaRequest.getEmail();
        String secret = twoFactorAuthService.generateSecret();
        userService.saveUserSecret(email, secret);
        try {
            byte[] qrCodeImage = twoFactorAuthService.generateQrCodeImage(secret, email);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            return ResponseEntity.ok().headers(headers).body(qrCodeImage);
        } catch (QrGenerationException e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @Operation(summary = "Verificar código de autenticação de dois fatores")
    @PostMapping("/verify-2fa")
    public ResponseEntity<?> verify2FA(@RequestBody VerificationRequest verificationRequest) {
        String email = verificationRequest.getEmail();
        String code = verificationRequest.getCode();
        String secret = userService.getUserSecret(email);
        boolean isValid = twoFactorAuthService.verifyCode(secret, code);
        if (isValid) {
            return ResponseEntity.ok("Código verificado com sucesso");
        } else {
            return ResponseEntity.status(400).body("Código inválido");
        }
    }

    @Operation(summary = "Logout do usuário")
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            userService.logoutUser(refreshToken);
            return ResponseEntity.ok("Logout bem-sucedido.");
        } catch (Exception e) {
            logger.error("Erro ao fazer logout", e);
            return ResponseEntity.status(500).body("Erro interno do servidor");
        }
    }
}