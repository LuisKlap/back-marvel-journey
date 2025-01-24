package com.marvel.marveljourney.service;

import com.marvel.marveljourney.model.User;
import com.marvel.marveljourney.repository.UserRepository;
import com.marvel.marveljourney.util.MfaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

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
    private MfaUtil mfaUtil;

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

    public String enableMfa(User user) {
        String secret = mfaUtil.generateSecretKey();
        user.setMfaSecret(secret);
        user.setMfaEnabled(true);
        updateUser(user);
        logger.info("MFA habilitado para o usuário: {}", user.getEmail());
        return secret;
    }

    public boolean verifyMfa(User user, int code) {
        logger.debug("Verificando MFA para o usuário: {}", user.getEmail());
        return mfaUtil.validateCode(user.getMfaSecret(), code);
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
            user.setMetadata(new User.Metadata());
        }
        user.getMetadata().setLastLoginAt(Instant.now());
        user.getMetadata().setIpAddress(ipAddress);
        user.getMetadata().setUserAgent(userAgent);
        logger.info("Atualizando metadata para o usuário: {}", user.getEmail());
        updateUser(user);
    }

    public void verifyEmail(String email) {
        userRepository.verifyEmail(email);
    }
}