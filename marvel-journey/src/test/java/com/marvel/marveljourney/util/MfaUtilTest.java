package com.marvel.marveljourney.util;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MfaUtilTest {

    @Mock
    private GoogleAuthenticator gAuth;

    @InjectMocks
    private MfaUtil mfaUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mfaUtil = new MfaUtil(gAuth); // Use the mock in the constructor
    }

    @Test
    void testGenerateSecretKey() {
        GoogleAuthenticatorKey key = mock(GoogleAuthenticatorKey.class);
        when(gAuth.createCredentials()).thenReturn(key);
        when(key.getKey()).thenReturn("test-secret-key");

        String secretKey = mfaUtil.generateSecretKey();

        assertEquals("test-secret-key", secretKey);
        verify(gAuth, times(1)).createCredentials();
    }

    @Test
    void testValidateCode_Valid() {
        when(gAuth.authorize(anyString(), anyInt())).thenReturn(true);

        boolean isValid = mfaUtil.validateCode("test-secret", 123456);

        assertTrue(isValid);
        verify(gAuth, times(1)).authorize("test-secret", 123456);
    }

    @Test
    void testValidateCode_Invalid() {
        when(gAuth.authorize(anyString(), anyInt())).thenReturn(false);

        boolean isValid = mfaUtil.validateCode("test-secret", 123456);

        assertFalse(isValid);
        verify(gAuth, times(1)).authorize("test-secret", 123456);
    }
}