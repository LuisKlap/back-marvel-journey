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

    @Test
    void testFindByEmail() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedPassword");
        user.setTermsAcceptedAt(Instant.now());
        user.setStatus("active");
        user.setIsTest(true);
        mongoTemplate.save(user);

        // Act
        Query query = new Query(Criteria.where("email").is("test@example.com"));
        Optional<User> foundUser = Optional.ofNullable(mongoTemplate.findOne(query, User.class));

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void testFindByStatus() {
        // Arrange
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setPasswordHash("hashedPassword1");
        user1.setTermsAcceptedAt(Instant.now());
        user1.setStatus("active");
        user1.setIsTest(true);
        mongoTemplate.save(user1);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setPasswordHash("hashedPassword2");
        user2.setTermsAcceptedAt(Instant.now());
        user2.setStatus("inactive");
        user2.setIsTest(true);
        mongoTemplate.save(user2);

        // Act
        Query query = new Query(Criteria.where("status").is("active"));
        List<User> activeUsers = mongoTemplate.find(query, User.class);

        // Assert
        assertThat(activeUsers).hasSize(1);
        assertThat(activeUsers.get(0).getEmail()).isEqualTo("user1@example.com");
    }

    @Test
    void testFindByRole() {
        // Arrange
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setPasswordHash("hashedPassword1");
        user1.setTermsAcceptedAt(Instant.now());
        user1.setStatus("active");
        user1.setRoles(List.of("ROLE_ADMIN"));
        user1.setIsTest(true);
        mongoTemplate.save(user1);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setPasswordHash("hashedPassword2");
        user2.setTermsAcceptedAt(Instant.now());
        user2.setStatus("active");
        user2.setRoles(List.of("ROLE_USER"));
        user2.setIsTest(true);
        mongoTemplate.save(user2);

        // Act
        Query query = new Query(Criteria.where("roles").in("ROLE_ADMIN"));
        List<User> adminUsers = mongoTemplate.find(query, User.class);

        // Assert
        assertThat(adminUsers).hasSize(1);
        assertThat(adminUsers.get(0).getEmail()).isEqualTo("user1@example.com");
    }

    @Test
    void testFindPasswordHashByEmail() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordHash("hashedPassword");
        user.setTermsAcceptedAt(Instant.now());
        user.setStatus("active");
        user.setIsTest(true);
        mongoTemplate.save(user);

        // Act
        Query query = new Query(Criteria.where("email").is("test@example.com"));
        Optional<User> foundUser = Optional.ofNullable(mongoTemplate.findOne(query, User.class));

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getPasswordHash()).isEqualTo("hashedPassword");
    }
}