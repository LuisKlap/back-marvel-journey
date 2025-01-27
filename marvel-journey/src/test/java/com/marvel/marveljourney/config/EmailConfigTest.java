package com.marvel.marveljourney.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Import(EmailConfig.class)
class EmailConfigTest {
    Logger logger = LoggerFactory.getLogger(EmailConfigTest.class);

    @Autowired
    private JavaMailSenderImpl javaMailSender;

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Test
    void testJavaMailSenderConfiguration() {
        try {
            assertEquals(host, javaMailSender.getHost());
            assertEquals(port, javaMailSender.getPort());
            assertEquals(username, javaMailSender.getUsername());
            assertEquals(password, javaMailSender.getPassword());
        } catch (AssertionError e) {
            logger.error("Erro na configuração do JavaMailSender: ", e);
            throw e;
        }
    }
}
