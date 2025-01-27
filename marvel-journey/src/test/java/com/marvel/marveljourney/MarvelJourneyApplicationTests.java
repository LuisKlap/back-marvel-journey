package com.marvel.marveljourney;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
class MarvelJourneyApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        // Verifica se o contexto da aplicação carrega corretamente
        assertNotNull(applicationContext, "O contexto da aplicação não deveria ser nulo");
    }

    @Test
    void main() {
        // Executa o método main da aplicação para garantir que ele não lança exceções
        MarvelJourneyApplication.main(new String[] {});
    }
}