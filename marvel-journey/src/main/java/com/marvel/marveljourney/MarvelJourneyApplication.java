package com.marvel.marveljourney;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MarvelJourneyApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(MarvelJourneyApplication.class);
        app.addListeners((applicationEvent) -> {
            if (applicationEvent instanceof org.springframework.context.event.ContextClosedEvent) {
                System.out.println("Encerrando a aplicação...");
            }
        });
        app.run(args);
    }
}