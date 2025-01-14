package com.marvel.marveljourney.security;

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
        // Adicione várias chaves
        keys.add(Keys.hmacShaKeyFor("mysecretkeymysecretkeymysecretkeymysecretkey1".getBytes()));
        keys.add(Keys.hmacShaKeyFor("mysecretkeymysecretkeymysecretkeymysecretkey2".getBytes()));
        keys.add(Keys.hmacShaKeyFor("mysecretkeymysecretkeymysecretkeymysecretkey3".getBytes()));
        activeKeyIndex = 0; // Inicialmente, use a primeira chave
    }

    public Key getActiveKey() {
        return keys.get(activeKeyIndex);
    }

    public List<Key> getAllKeys() {
        return keys;
    }

    public void rotateKey() {
        activeKeyIndex = (activeKeyIndex + 1) % keys.size();
    }
}