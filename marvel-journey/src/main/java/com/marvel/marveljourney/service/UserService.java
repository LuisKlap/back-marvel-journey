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
import java.util.Optional;

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

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
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
        return mfaUtil.validateCode(user.getMfaSecret(), code);
    }

    public void increaseFailedAttempts(User user) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
            user.setLockoutEndTime(Instant.now().plusMillis(LOCKOUT_DURATION));
        }
        updateUser(user);
    }

    public void resetFailedAttempts(User user) {
        user.setFailedLoginAttempts(0);
        user.setLockoutEndTime(null);
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
        return true;
    }

    public void updateLoginAttempts(User user) {
        if (user.getLoginAttempts() == null) {
            user.setLoginAttempts(new User.LoginAttempts());
        }
        user.getLoginAttempts().setCount(user.getLoginAttempts().getCount() + 1);
        user.getLoginAttempts().setLastAttemptAt(Instant.now());
        updateUser(user);
    }

    public void resetLoginAttempts(User user) {
        if (user.getLoginAttempts() != null) {
            user.getLoginAttempts().setCount(0);
            user.getLoginAttempts().setLastAttemptAt(null);
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
        updateUser(user);
    }
}