package com.marvel.marveljourney.service;

import com.marvel.marveljourney.model.User;
import com.marvel.marveljourney.repository.UserRepository;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TwoFactorAuthService {
    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final CodeVerifier verifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());

    @Autowired
    private UserRepository userRepository;

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

    public byte[] setup2FA(String email) throws QrGenerationException {
        String secret = generateSecret();
        saveUserSecret(email, secret);
        return generateQrCodeImage(secret, email);
    }

    public boolean verify2FA(String email, String code) {
        String secret = getUserSecret(email);
        return verifyCode(secret, code);
    }

    public void saveUserSecret(String email, String secret) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        user.getMfa().setSecret(secret);
        userRepository.save(user);
    }

    public String getUserSecret(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return user.getMfa().getSecret();
    }
}
