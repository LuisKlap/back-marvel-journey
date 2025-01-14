package com.marvel.marveljourney;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MarvelJourneyApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarvelJourneyApplication.class, args);
    }
}