package com.marvel.marveljourney.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class KeyRotationScheduler {

    private static final long ONE_DAY_IN_MILLISECONDS = 86400000;

    private final KeyManager keyManager;

    public KeyRotationScheduler(KeyManager keyManager) {
        this.keyManager = keyManager;
    }

    @Scheduled(fixedRate = ONE_DAY_IN_MILLISECONDS)
    public void rotateKeys() {
        keyManager.rotateKey();
    }
}