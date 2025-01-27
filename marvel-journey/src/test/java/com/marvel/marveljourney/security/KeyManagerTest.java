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
    void testInit() {
        List<Key> keys = keyManager.getAllKeys();
        assertNotNull(keys);
        assertEquals(3, keys.size());
        for (Key key : keys) {
            assertNotNull(key);
        }
    }

    @Test
    void testGetActiveKey() {
        Key activeKey = keyManager.getActiveKey();
        assertNotNull(activeKey);
        assertEquals(keyManager.getAllKeys().get(0), activeKey);
    }

    @Test
    void testRotateKey() {
        Key initialKey = keyManager.getActiveKey();
        keyManager.rotateKey();
        Key newKey = keyManager.getActiveKey();
        assertNotNull(newKey);
        assertNotEquals(initialKey, newKey);
        assertEquals(keyManager.getAllKeys().get(1), newKey);
    }

    @Test
    void testGetAllKeys() {
        List<Key> keys = keyManager.getAllKeys();
        assertNotNull(keys);
        assertEquals(3, keys.size());
    }
}