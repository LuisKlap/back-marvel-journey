package com.marvel.marveljourney.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

class KeyRotationSchedulerTest {

    @Mock
    private KeyManager keyManager;

    @InjectMocks
    private KeyRotationScheduler keyRotationScheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRotateKeys() {
        keyRotationScheduler.rotateKeys();
        verify(keyManager, times(1)).rotateKey();
    }
}