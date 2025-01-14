package com.marvel.marveljourney.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class KeyRotationScheduler {

    @Autowired
    private KeyManager keyManager;

    @Scheduled(fixedRate = 86400000)
    public void rotateKeys() {
        keyManager.rotateKey();
    }
}