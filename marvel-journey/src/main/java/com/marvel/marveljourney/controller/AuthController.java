package com.marvel.marveljourney.controller;

import com.marvel.marveljourney.dto.LoginRequest;
import com.marvel.marveljourney.dto.RegisterRequest;
import com.marvel.marveljourney.dto.VerificationRequest;
import com.marvel.marveljourney.dto.MfaRequest;
import com.marvel.marveljourney.dto.PasswordResetRequest;
import com.marvel.marveljourney.exception.ErrorCode;
import com.marvel.marveljourney.exception.UserNotFoundException;
import com.marvel.marveljourney.service.UserAuthService;
import com.marvel.marveljourney.service.UserService;
import com.marvel.marveljourney.service.EmailVerificationService;
import com.marvel.marveljourney.service.TwoFactorAuthService;
import com.marvel.marveljourney.service.RefreshTokenService;

import dev.samstevens.totp.exceptions.QrGenerationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Auth", description = "APIs de autenticação")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserAuthService userAuthService;
    private final EmailVerificationService emailVerificationService;
    private final TwoFactorAuthService twoFactorAuthService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    @Value("${jwt.refresh.expiration.time}")
    private long refreshTokenDurationMs;

    @Value("${jwt.expiration.time}")
    private long jwtExpirationTime;

    private static final String ISSUER = "seu-servidor";
    private static final String AUDIENCE = "seu-aplicativo";

    public AuthController(UserAuthService userAuthService, EmailVerificationService emailVerificationService,
                          TwoFactorAuthService twoFactorAuthService, RefreshTokenService refreshTokenService, UserService userService) {
        this.userAuthService = userAuthService;
        this.emailVerificationService = emailVerificationService;
        this.twoFactorAuthService = twoFactorAuthService;
        this.refreshTokenService = refreshTokenService;
        this.userService = userService;
    }

    @Operation(summary = "Registrar um novo usuário")
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            userAuthService.registerUser(registerRequest);
            return ResponseEntity.ok(Map.of("message", "Registration successful. Please check your email for the verification code."));
        } catch (Exception e) {
            logger.error("Erro ao registrar usuário", e);
            return ResponseEntity.status(500).body(Map.of("error", ErrorCode.INTERNAL_SERVER_ERROR.name(), "message",
                    ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
        }
    }

    @Operation(summary = "Verificar código de verificação de email")
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody VerificationRequest verificationRequest) {
        try {
            Map<String, String> response = emailVerificationService.verifyEmail(verificationRequest);
            return ResponseEntity.ok(response);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(404).body(Map.of("error", ErrorCode.USER_NOT_FOUND.name(), "message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("error", ErrorCode.INVALID_VERIFICATION_CODE.name(), "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro ao verificar email", e);
            return ResponseEntity.status(500).body(Map.of("error", ErrorCode.INTERNAL_SERVER_ERROR.name(), "message", ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
        }
    }

    @Operation(summary = "Login de um usuário")
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            Map<String, String> response = userAuthService.loginUser(loginRequest, jwtExpirationTime, ISSUER, AUDIENCE, refreshTokenDurationMs);
            logger.info("Login bem-sucedido para o usuário: {}", loginRequest.getEmail());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Tentativa de login com email ou senha inválidos: {}", loginRequest.getEmail());
            return ResponseEntity.status(401).body(Map.of("error", ErrorCode.INVALID_CREDENTIALS.name(), "message", e.getMessage()));
        } catch (IllegalStateException e) {
            logger.warn("Tentativa de login com conta bloqueada ou email não verificado: {}", loginRequest.getEmail());
            return ResponseEntity.status(403).body(Map.of("error", ErrorCode.ACCOUNT_LOCKED.name(), "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro ao fazer login", e);
            return ResponseEntity.status(500).body(Map.of("error", ErrorCode.INTERNAL_SERVER_ERROR.name(), "message", ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
        }
    }

    @Operation(summary = "Renovar o JWT com base no refresh token")
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        try {
            Map<String, String> response = refreshTokenService.refreshToken(refreshToken, jwtExpirationTime, ISSUER, AUDIENCE, refreshTokenDurationMs);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("error", ErrorCode.INVALID_VERIFICATION_CODE.name(), "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error renewing token", e);
            return ResponseEntity.status(500).body(Map.of("error", ErrorCode.INTERNAL_SERVER_ERROR.name(), "message", ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
        }
    }

    @Operation(summary = "Configurar autenticação de dois fatores")
    @PostMapping("/setup-2fa")
    public ResponseEntity<byte[]> setup2FA(@RequestBody MfaRequest mfaRequest) {
        try {
            byte[] qrCodeImage = twoFactorAuthService.setup2FA(mfaRequest.getEmail());
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
        try {
            boolean isValid = twoFactorAuthService.verify2FA(verificationRequest.getEmail(), verificationRequest.getCode());
            if (isValid) {
                return ResponseEntity.ok("Code successfully verified");
            } else {
                return ResponseEntity.status(400).body(Map.of("error", ErrorCode.INVALID_VERIFICATION_CODE.name(),
                        "message", ErrorCode.INVALID_VERIFICATION_CODE.getMessage()));
            }
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(404).body(
                    Map.of("error", ErrorCode.USER_NOT_FOUND.name(), "message", ErrorCode.USER_NOT_FOUND.getMessage()));
        } catch (Exception e) {
            logger.error("Erro ao verificar código de autenticação de dois fatores", e);
            return ResponseEntity.status(500).body(Map.of("error", ErrorCode.INTERNAL_SERVER_ERROR.name(), "message",
                    ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
        }
    }

    @Operation(summary = "Logout do usuário")
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestBody Map<String, String> request) {
        try {
            userAuthService.logoutUser(request.get("refreshToken"));
            return ResponseEntity.ok("Logout bem-sucedido.");
        } catch (Exception e) {
            logger.error("Erro ao fazer logout", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    @Operation(summary = "Enviar código de verificação para o email")
    @PostMapping("/send-verification-code")
    public ResponseEntity<?> sendVerificationCode(@RequestBody VerificationRequest verificationRequest) {
        try {
            emailVerificationService.sendVerificationCode(verificationRequest.getEmail());
            return ResponseEntity.ok(Map.of("message", "Verification code sent successfully."));
        } catch (Exception e) {
            logger.error("Erro ao enviar código de verificação", e);
            return ResponseEntity.status(500).body(Map.of("error", "INTERNAL_SERVER_ERROR", "message", "Erro interno do servidor"));
        }
    }

    @Operation(summary = "Verifica se o email existe na base de dados")
    @PostMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (userService.checkEmailExists(email)) {
            return ResponseEntity.ok(Map.of("message", "User found"));
        } else {
            return ResponseEntity.status(404).body(Map.of("error", ErrorCode.USER_NOT_FOUND.name(), "message",
                    ErrorCode.USER_NOT_FOUND.getMessage()));
        }
    }

    @Operation(summary = "Confirmar reset de senha")
    @PostMapping("/password-reset")
    public ResponseEntity<?> confirmPasswordReset(@RequestBody PasswordResetRequest passwordResetRequest) {
        userAuthService.confirmPasswordReset(passwordResetRequest);
        return ResponseEntity.ok("Senha resetada com sucesso.");
    }
}