package com.marvel.marveljourney.repository;

import com.marvel.marveljourney.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataMongoTest
class UserRepositoryTest extends EmbeddedMongoDbConfig {

    @BeforeEach
    void setUp() {
        Query query = new Query();
        mongoTemplate.remove(query, "users");
    }

    @AfterEach
    void tearDown() {
        Query query = new Query();
        mongoTemplate.remove(query, "users");
    }

}