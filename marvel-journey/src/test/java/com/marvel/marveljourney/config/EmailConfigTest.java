package com.marvel.marveljourney.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Import(EmailConfig.class)
class EmailConfigTest {

    @Autowired
    private JavaMailSender javaMailSender;

    @Test
    void testJavaMailSenderConfiguration() {
        assertNotNull(javaMailSender, "JavaMailSender não deveria ser nulo");
    }
}
