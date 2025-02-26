package com.marvel.marveljourney.service;

import com.marvel.marveljourney.dto.*;
import com.marvel.marveljourney.exception.ErrorCode;
import com.marvel.marveljourney.model.User;
import com.marvel.marveljourney.repository.UserRepository;
import com.marvel.marveljourney.security.VerificationCodeUtil;
import com.marvel.marveljourney.util.JwtUtil;
import com.marvel.marveljourney.util.PasswordValidatorUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@Service
public class UserAuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordValidatorUtil passwordValidatorUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    public Map<String, String> loginUser(LoginRequest loginRequest, long jwtExpirationTime, String issuer, String audience, long refreshTokenDurationMs) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.INVALID_CREDENTIALS.getMessage()));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException(ErrorCode.INVALID_CREDENTIALS.getMessage());
        }

        if (!userRepository.emailIsVerified(user.getEmail())) {
            throw new IllegalStateException("Email not verified. Please verify your email before logging in.");
        }

        String token = jwtUtil.generateToken(user.getEmail(), jwtExpirationTime, issuer, audience, new ArrayList<>());
        String refreshToken = refreshTokenService.generateRefreshToken(user, refreshTokenDurationMs);

        return Map.of("token", token, "refreshToken", refreshToken);
    }

    public void registerUser(RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new IllegalArgumentException(ErrorCode.EMAIL_ALREADY_REGISTERED.getMessage());
        }

        if (!passwordValidatorUtil.validate(registerRequest.getPassword())) {
            throw new IllegalArgumentException(ErrorCode.INVALID_PASSWORD.getMessage());
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        userRepository.save(user);

        emailService.sendVerificationEmail(user.getEmail(), VerificationCodeUtil.generateCode());
    }

    public void logoutUser(String refreshToken) {
        User user = refreshTokenService.findByRefreshToken(refreshToken);
        if (user == null) {
            throw new IllegalArgumentException("Invalid or expired refresh token.");
        }
        refreshTokenService.deleteRefreshToken(user, refreshToken);
    }

    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.USER_NOT_FOUND.getMessage()));

        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        userRepository.save(user);

        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
    }

    public void confirmPasswordReset(PasswordResetRequest passwordResetRequest) {
        User user = userRepository.findByResetToken(passwordResetRequest.getResetToken())
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.INVALID_RESET_TOKEN.getMessage()));

        if (!passwordValidatorUtil.validate(passwordResetRequest.getNewPassword())) {
            throw new IllegalArgumentException(ErrorCode.INVALID_PASSWORD.getMessage());
        }

        user.setPasswordHash(passwordEncoder.encode(passwordResetRequest.getNewPassword()));
        user.setResetToken(null);
        userRepository.save(user);
    }
}