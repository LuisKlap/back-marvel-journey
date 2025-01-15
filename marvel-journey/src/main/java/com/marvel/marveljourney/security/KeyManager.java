package com.marvel.marveljourney.security;

import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

@Component
public class KeyManager {

    private static final String SECRET_KEY_1 = "mysecretkeymysecretkeymysecretkeymysecretkey1";
    private static final String SECRET_KEY_2 = "mysecretkeymysecretkeymysecretkeymysecretkey2";
    private static final String SECRET_KEY_3 = "mysecretkeymysecretkeymysecretkeymysecretkey3";

    private List<Key> keys;
    private int activeKeyIndex;

    @PostConstruct
    public void init() {
        keys = new ArrayList<>();
        keys.add(Keys.hmacShaKeyFor(SECRET_KEY_1.getBytes()));
        keys.add(Keys.hmacShaKeyFor(SECRET_KEY_2.getBytes()));
        keys.add(Keys.hmacShaKeyFor(SECRET_KEY_3.getBytes()));
        activeKeyIndex = 0;
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