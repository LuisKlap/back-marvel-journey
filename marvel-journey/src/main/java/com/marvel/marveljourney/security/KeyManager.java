package com.marvel.marveljourney.security;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

@Component
public class KeyManager {

    private List<Key> keys;
    private int activeKeyIndex;

    @PostConstruct
    public void init() {
        keys = new ArrayList<>();
        // Gerar chaves fortes o suficiente para HS512
        keys.add(Keys.secretKeyFor(SignatureAlgorithm.HS512));
        keys.add(Keys.secretKeyFor(SignatureAlgorithm.HS512));
        keys.add(Keys.secretKeyFor(SignatureAlgorithm.HS512));
        activeKeyIndex = 0;
    }

    public Key getActiveKey() {
        return keys.get(activeKeyIndex);
    }

    public void rotateKey() {
        activeKeyIndex = (activeKeyIndex + 1) % keys.size();
    }

    public List<Key> getAllKeys() {
        return keys;
    }
}