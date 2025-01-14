package com.marvel.marveljourney.util;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.stereotype.Component;

@Component
public class MfaUtil {

    private final GoogleAuthenticator gAuth = new GoogleAuthenticator();

    public String generateSecretKey() {
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        return key.getKey();
    }

    public boolean validateCode(String secret, int code) {
        return gAuth.authorize(secret, code);
    }
}