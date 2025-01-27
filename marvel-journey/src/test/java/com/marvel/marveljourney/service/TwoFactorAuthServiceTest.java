package com.marvel.marveljourney.service;

import dev.samstevens.totp.exceptions.QrGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

public class TwoFactorAuthServiceTest {

    @InjectMocks
    private TwoFactorAuthService twoFactorAuthService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGenerateSecret() {
        String secret = twoFactorAuthService.generateSecret();
        assertNotNull(secret, "O segredo não deveria ser nulo");
        assertFalse(secret.isEmpty(), "O segredo não deveria ser vazio");
    }

    @Test
    void testGenerateQrCodeImage() {
        String secret = "testSecret";
        String email = "test@example.com";
        try {
            byte[] qrCodeImage = twoFactorAuthService.generateQrCodeImage(secret, email);
            assertNotNull(qrCodeImage, "A imagem do QR Code não deveria ser nula");
            assertTrue(qrCodeImage.length > 0, "A imagem do QR Code não deveria ser vazia");
        } catch (QrGenerationException e) {
            fail("Não deveria lançar exceção ao gerar a imagem do QR Code");
        }
    }

    @Test
    void testVerifyCode() {
        String secret = twoFactorAuthService.generateSecret();
        String validCode = "123456";
        assertFalse(twoFactorAuthService.verifyCode(secret, validCode), "O código não deveria ser válido");
    }
}
