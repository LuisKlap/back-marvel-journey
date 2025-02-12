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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private LoginAttemptService loginAttemptService;

    @Autowired
    private UserMetadataService userMetadataService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    public Map<String, String> loginUser(LoginRequest loginRequest, long jwtExpirationTime, String issuer,
                                         String audience, long refreshTokenDurationMs) {
        User userOptional = userRepository.findByEmail(loginRequest.getEmail()).orElse(null);

        if (userOptional == null) {
            throw new IllegalArgumentException(ErrorCode.INVALID_CREDENTIALS.getMessage());
        }

        if (loginAttemptService.isAccountLocked(userOptional)) {
            throw new IllegalStateException(ErrorCode.ACCOUNT_LOCKED.getMessage());
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), userOptional.getPasswordHash())) {
            loginAttemptService.increaseFailedAttempts(userOptional);
            throw new IllegalArgumentException(ErrorCode.INVALID_CREDENTIALS.getMessage());
        }

        if (!userRepository.emailIsVerified(userOptional.getEmail())) {
            throw new IllegalStateException(ErrorCode.EMAIL_NOT_VERIFIED.getMessage());
        }

        loginAttemptService.resetFailedAttempts(userOptional);
        userMetadataService.updateMetadata(userOptional, loginRequest.getIpAddress(), loginRequest.getUserAgent());

        String token = jwtUtil.generateToken(userOptional.getEmail(), jwtExpirationTime, issuer, audience,
                userOptional.getRoles());
        String refreshToken = refreshTokenService.generateRefreshToken(userOptional, refreshTokenDurationMs);

        return Map.of("token", token, "refreshToken", refreshToken);
    }

    public void registerUser(RegisterRequest registerRequest) {
        User userExist = userRepository.findByEmail(registerRequest.getEmail()).orElse(null);

        if (userExist != null) {
            throw new IllegalArgumentException(ErrorCode.EMAIL_ALREADY_REGISTERED.getMessage());
        }

        if (!passwordValidatorUtil.validate(registerRequest.getPassword())) {
            throw new IllegalArgumentException(ErrorCode.WEAK_PASSWORD.getMessage() + " "
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
        User.Metadata metadata = new User.Metadata();
        metadata.setDevice(registerRequest.getDevice());
        metadata.setIpAddress(registerRequest.getIpAddress());
        metadata.setUserAgent(registerRequest.getUserAgent());
        user.getMetadata().add(metadata);

        User.MfaData mfaData = new User.MfaData();
        mfaData.setSecret(null);
        mfaData.setEnabled(false);
        user.setMfa(mfaData);

        String verificationCode = VerificationCodeUtil.generateCode();
        emailService.sendVerificationEmail(user.getEmail(), verificationCode);

        User.VerificationCode verification = new User.VerificationCode();
        verification.setEmailIsVerified(false);
        verification.setCode(verificationCode);
        verification.setCreatedAt(Instant.now());
        user.setVerificationCode(verification);

        userRepository.save(user);
    }

    public void logoutUser(String refreshToken) {
        User user = refreshTokenService.findByRefreshToken(refreshToken);
        if (user == null) {
            throw new IllegalArgumentException("Invalid or expired refresh token.");
        }

        refreshTokenService.deleteRefreshToken(user, refreshToken);
    }
}
