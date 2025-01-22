package com.marvel.marveljourney.security;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

@Component
public class VerificationCodeUtil {
    private static final SecureRandom random = new SecureRandom();

    public static String generateCode() {
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
