package com.marvel.marveljourney.util;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MfaUtil {

    private static final Logger logger = LoggerFactory.getLogger(MfaUtil.class);
    private final GoogleAuthenticator gAuth;

    public MfaUtil(GoogleAuthenticator gAuth) {
        this.gAuth = gAuth;
    }

    public String generateSecretKey() {
        try {
            GoogleAuthenticatorKey key = gAuth.createCredentials();
            logger.info("Chave secreta MFA gerada com sucesso.");
            return key.getKey();
        } catch (Exception e) {
            logger.error("Erro ao gerar chave secreta MFA", e);
            throw new RuntimeException("Erro ao gerar chave secreta MFA", e);
        }
    }

    public boolean validateCode(String secret, int code) {
        try {
            boolean isValid = gAuth.authorize(secret, code);
            if (isValid) {
                logger.info("Código MFA validado com sucesso.");
            } else {
                logger.warn("Código MFA inválido.");
            }
            return isValid;
        } catch (Exception e) {
            logger.error("Erro ao validar código MFA", e);
            throw new RuntimeException("Erro ao validar código MFA", e);
        }
    }
}