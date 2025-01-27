package com.marvel.marveljourney.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PasswordValidatorUtilTest {

    private PasswordValidatorUtil passwordValidatorUtil;

    @BeforeEach
    void setUp() throws FileNotFoundException, IOException {
        passwordValidatorUtil = new PasswordValidatorUtil();
    }

    @Test
    void testValidPassword() {
        String validPassword = "Valid1Password!";
        assertTrue(passwordValidatorUtil.validate(validPassword));
    }

    @Test
    void testInvalidPasswordTooShort() {
        String invalidPassword = "Short1!";
        assertFalse(passwordValidatorUtil.validate(invalidPassword));
    }

    @Test
    void testInvalidPasswordNoUpperCase() {
        String invalidPassword = "nouppercase1!";
        assertFalse(passwordValidatorUtil.validate(invalidPassword));
    }

    @Test
    void testInvalidPasswordNoLowerCase() {
        String invalidPassword = "NOLOWERCASE1!";
        assertFalse(passwordValidatorUtil.validate(invalidPassword));
    }

    @Test
    void testInvalidPasswordNoDigit() {
        String invalidPassword = "NoDigitPassword!";
        assertFalse(passwordValidatorUtil.validate(invalidPassword));
    }

    @Test
    void testInvalidPasswordNoSpecialCharacter() {
        String invalidPassword = "NoSpecialCharacter1";
        assertFalse(passwordValidatorUtil.validate(invalidPassword));
    }

    @Test
    void testInvalidPasswordWithWhitespace() {
        String invalidPassword = "Invalid Password1!";
        assertFalse(passwordValidatorUtil.validate(invalidPassword));
    }

    @Test
    void testGetMessagesForInvalidPassword() {
        String invalidPassword = "short";
        List<String> messages = passwordValidatorUtil.getMessages(invalidPassword);
        assertFalse(messages.isEmpty());
    }

    @Test
    void testGetMessagesForValidPassword() {
        String validPassword = "Valid1Password!";
        List<String> messages = passwordValidatorUtil.getMessages(validPassword);
        assertTrue(messages.isEmpty());
    }
}