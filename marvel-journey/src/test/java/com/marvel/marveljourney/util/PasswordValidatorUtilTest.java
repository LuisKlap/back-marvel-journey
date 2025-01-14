package com.marvel.marveljourney.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class PasswordValidatorUtilTest {

    private PasswordValidatorUtil passwordValidatorUtil;

    @BeforeEach
    void setUp() throws FileNotFoundException, IOException {
        passwordValidatorUtil = new PasswordValidatorUtil();
    }

    @Test
    void testValidateValidPassword() {
        assertTrue(passwordValidatorUtil.validate("ValidPassword1!"));
    }

    @Test
    void testValidateInvalidPassword() {
        assertFalse(passwordValidatorUtil.validate("short"));
    }

    @Test
    void testGetMessagesForInvalidPassword() {
        assertFalse(passwordValidatorUtil.getMessages("short").isEmpty());
    }
}