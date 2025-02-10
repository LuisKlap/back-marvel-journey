package com.marvel.marveljourney.service;

import com.marvel.marveljourney.exception.ErrorCode;
import com.marvel.marveljourney.exception.UserNotFoundException;
import com.marvel.marveljourney.model.User;
import com.marvel.marveljourney.model.User.MfaData;
import com.marvel.marveljourney.model.User.VerificationCode;
import com.marvel.marveljourney.repository.UserRepository;
import com.marvel.marveljourney.dto.RegisterRequest;
import com.marvel.marveljourney.security.VerificationCodeUtil;
import com.marvel.marveljourney.service.EmailService;
import com.marvel.marveljourney.util.PasswordValidatorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION = 15 * 60 * 1000;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordValidatorUtil passwordValidatorUtil;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public User saveUser(User user) {
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        logger.info("Salvando novo usuário: {}", user.getEmail());
        return userRepository.save(user);
    }

    public User updateUser(User user) {
        user.setUpdatedAt(Instant.now());
        logger.info("Atualizando usuário: {}", user.getEmail());
        return userRepository.save(user);
    }

    public boolean validatePassword(String rawPassword, String encodedPassword) {
        logger.debug("Validando senha para o usuário: {}", rawPassword);
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    public void increaseFailedAttempts(User user) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        logger.warn("Aumentando tentativas falhas para o usuário: {}. Tentativas falhas: {}", user.getEmail(),
                user.getFailedLoginAttempts());
        if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
            user.setLockoutEndTime(Instant.now().plusMillis(LOCKOUT_DURATION));
            logger.warn("Usuário bloqueado devido a tentativas falhas: {}", user.getEmail());
        }
        updateUser(user);
    }

    public void resetFailedAttempts(User user) {
        user.setFailedLoginAttempts(0);
        user.setLockoutEndTime(null);
        logger.info("Resetando tentativas falhas para o usuário: {}", user.getEmail());
        updateUser(user);
    }

    public boolean isAccountLocked(User user) {
        if (user.getLockoutEndTime() == null) {
            return false;
        }
        if (user.getLockoutEndTime().isBefore(Instant.now())) {
            resetFailedAttempts(user);
            return false;
        }
        logger.warn("Conta bloqueada para o usuário: {}", user.getEmail());
        return true;
    }

    public void updateLoginAttempts(User user) {
        if (user.getLoginAttempts() == null) {
            user.setLoginAttempts(new User.LoginAttempts());
        }
        user.getLoginAttempts().setCount(user.getLoginAttempts().getCount() + 1);
        user.getLoginAttempts().setLastAttemptAt(Instant.now());
        logger.info("Atualizando tentativas de login para o usuário: {}", user.getEmail());
        updateUser(user);
    }

    public void resetLoginAttempts(User user) {
        if (user.getLoginAttempts() != null) {
            user.getLoginAttempts().setCount(0);
            user.getLoginAttempts().setLastAttemptAt(null);
            logger.info("Resetando tentativas de login para o usuário: {}", user.getEmail());
            updateUser(user);
        }
    }

    public void updateMetadata(User user, String ipAddress, String userAgent) {
        if (user.getMetadata() == null) {
            user.setMetadata(new ArrayList<>());
        }

        boolean metadataExists = false;
        for (User.Metadata metadata : user.getMetadata()) {
            if (metadata.getIpAddress().equals(ipAddress) && metadata.getUserAgent().equals(userAgent)) {
                metadata.setLastLoginAt(Instant.now());
                metadataExists = true;
                break;
            }
        }

        if (!metadataExists) {
            User.Metadata newMetadata = new User.Metadata();
            newMetadata.setLastLoginAt(Instant.now());
            newMetadata.setIpAddress(ipAddress);
            newMetadata.setUserAgent(userAgent);
            user.getMetadata().add(newMetadata);
        }

        logger.info("Atualizando metadata para o usuário: {}", user.getEmail());
        updateUser(user);
    }

    public void verifyEmail(String email) {
        userRepository.verifyEmail(email);
    }

    public boolean emailIsVerified(String email) {
        return userRepository.emailIsVerified(email);
    }

    public void saveUserSecret(String email, String secret) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        user.getMfa().setSecret(secret);
        userRepository.save(user);
    }

    public String getUserSecret(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return user.getMfa().getSecret();
    }

    public User findByRefreshToken(String refreshToken) {
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.getMetadata() != null) {
                for (User.Metadata metadata : user.getMetadata()) {
                    if (passwordEncoder.matches(refreshToken, metadata.getRefreshTokenHash())) {
                        return user;
                    }
                }
            }
        }
        return null;
    }

    public void logoutUser(String token) {
        User user = findByRefreshToken(token);
        if (user != null) {
            if (user.getMetadata() != null) {
                for (User.Metadata metadata : user.getMetadata()) {
                    if (passwordEncoder.matches(token, metadata.getRefreshTokenHash())) {
                        metadata.setRefreshTokenHash(null);
                        metadata.setRefreshTokenExpiryDate(null);
                    }
                }
            }
            userRepository.save(user);
            logger.info("Logout bem-sucedido para o usuário: {}", user.getEmail());
        } else {
            throw new RuntimeException("Usuário não encontrado");
        }
    }

    public void updateVerificationCode(String email, String code, Instant createdAt) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        User.VerificationCode verificationCode = new User.VerificationCode();
        verificationCode.setCode(code);
        verificationCode.setCreatedAt(createdAt);
        user.setVerificationCode(verificationCode);
        userRepository.save(user);
    }

    public void registerUser(RegisterRequest registerRequest) {
        User userExist = findByEmail(registerRequest.getEmail());

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

        saveUser(user);
    }
}