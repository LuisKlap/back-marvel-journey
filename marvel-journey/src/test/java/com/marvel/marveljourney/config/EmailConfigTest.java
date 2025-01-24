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
@Import(EmailConfig.class) // Adicione esta linha para importar a configuração
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

            assertEquals("smtp.gmail.com", javaMailSender.getHost());
            assertEquals(465, javaMailSender.getPort());
            assertEquals("marvel.journey.project@gmail.com", javaMailSender.getUsername());
            assertEquals("jhkh dvwg jcbb kiot", javaMailSender.getPassword());

            // var props = javaMailSender.getJavaMailProperties();
            // assertTrue(Boolean.parseBoolean(props.getProperty("mail.smtp.auth")));
            // assertTrue(Boolean.parseBoolean(props.getProperty("mail.smtp.starttls.enable")));
        } catch (AssertionError e) {
            logger.error("Erro na configuração do JavaMailSender: ", e);
            throw e;
        }
    }
}
