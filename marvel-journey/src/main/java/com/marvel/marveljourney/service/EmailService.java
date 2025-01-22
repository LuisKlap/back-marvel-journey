package com.marvel.marveljourney.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String remetente;

    public void sendVerificationEmail(String to, String code) {
        String subject = "Marvel Journey - Código de verificação";
        String text = "Seu código de verificação é: " + code;
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(remetente);
        simpleMailMessage.setTo(to);
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setText(text);

        try {
            javaMailSender.send(simpleMailMessage);
            logger.info("Email de verificação enviado para {}", to);
        } catch (MailException e) {
            logger.error("Erro ao enviar email de verificação para {}: {}", to, e.getMessage());
        }
    }
}