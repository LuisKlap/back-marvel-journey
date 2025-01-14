package com.marvel.marveljourney.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.warrenstrange.googleauth.GoogleAuthenticator;

import static org.junit.jupiter.api.Assertions.*;

class MfaUtilTest {

    private MfaUtil mfaUtil;

    @BeforeEach
    void setUp() {
        mfaUtil = new MfaUtil();
    }

    @Test
    void testGenerateSecretKey() {
        String secretKey = mfaUtil.generateSecretKey();
        assertNotNull(secretKey);
    }

    @Test
    void testValidateCode() {
        String secretKey = mfaUtil.generateSecretKey();
        int code = new GoogleAuthenticator().getTotpPassword(secretKey);
        assertTrue(mfaUtil.validateCode(secretKey, code));
    }
}