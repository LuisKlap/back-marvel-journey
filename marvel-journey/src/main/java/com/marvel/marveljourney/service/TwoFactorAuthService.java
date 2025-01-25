package com.marvel.marveljourney.service;

import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;

import org.springframework.stereotype.Service;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;

@Service
public class TwoFactorAuthService {
    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final CodeVerifier verifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());

    public String generateSecret() {
        return secretGenerator.generate();
    }

    public byte[] generateQrCodeImage(String secret, String email) throws QrGenerationException {
        QrData data = new QrData.Builder()
            .label(email)
            .secret(secret)
            .issuer("seu-servidor")
            .algorithm(HashingAlgorithm.SHA1)
            .digits(6)
            .period(30)
            .build();
    
        QrGenerator generator = new ZxingPngQrGenerator();
        return generator.generate(data);
    }

    public boolean verifyCode(String secret, String code) {
        return verifier.isValidCode(secret, code);
    }
}
