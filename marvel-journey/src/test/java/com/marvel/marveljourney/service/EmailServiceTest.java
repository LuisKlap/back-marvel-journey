package com.marvel.marveljourney.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;

import static org.mockito.Mockito.*;

class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendVerificationEmailSuccess() {
        String to = "test@example.com";
        String code = "123456";

        emailService.sendVerificationEmail(to, code);

        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendVerificationEmailFailure() {
        String to = "test@example.com";
        String code = "123456";

        doThrow(new MailException("Erro ao enviar email") {}).when(javaMailSender).send(any(SimpleMailMessage.class));

        emailService.sendVerificationEmail(to, code);

        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}