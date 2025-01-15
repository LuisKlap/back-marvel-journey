package com.marvel.marveljourney.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KeyManagerTest {

    private KeyManager keyManager;

    @BeforeEach
    void setUp() {
        keyManager = new KeyManager();
        keyManager.init();
    }

    @Test
    void testGetActiveKey() {
        Key activeKey = keyManager.getActiveKey();
        assertNotNull(activeKey);
    }

    @Test
    void testGetAllKeys() {
        List<Key> keys = keyManager.getAllKeys();
        assertEquals(3, keys.size());
    }

    @Test
    void testRotateKey() {
        Key initialKey = keyManager.getActiveKey();
        keyManager.rotateKey();
        Key newKey = keyManager.getActiveKey();
        assertNotEquals(initialKey, newKey);
    }
}