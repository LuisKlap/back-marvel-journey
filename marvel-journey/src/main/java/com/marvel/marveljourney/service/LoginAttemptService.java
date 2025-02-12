package com.marvel.marveljourney.service;

import com.marvel.marveljourney.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class LoginAttemptService {

    private static final Logger logger = LoggerFactory.getLogger(LoginAttemptService.class);
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION = 15 * 60 * 1000;

    public void increaseFailedAttempts(User user) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        logger.warn("Aumentando tentativas falhas para o usuário: {}. Tentativas falhas: {}", user.getEmail(),
                user.getFailedLoginAttempts());
        if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
            user.setLockoutEndTime(Instant.now().plusMillis(LOCKOUT_DURATION));
            logger.warn("Usuário bloqueado devido a tentativas falhas: {}", user.getEmail());
        }
    }

    public void resetFailedAttempts(User user) {
        user.setFailedLoginAttempts(0);
        user.setLockoutEndTime(null);
        logger.info("Resetando tentativas falhas para o usuário: {}", user.getEmail());
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
}
