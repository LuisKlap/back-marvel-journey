package com.marvel.marveljourney.repository;

import com.marvel.marveljourney.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAllByIsTest(true);
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
        userRepository.save(user);

        // Act
        Optional<User> foundUser = userRepository.findByEmail("test@example.com");

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
        userRepository.save(user1);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setPasswordHash("hashedPassword2");
        user2.setTermsAcceptedAt(Instant.now());
        user2.setStatus("inactive");
        user2.setIsTest(true);
        userRepository.save(user2);

        // Act
        List<User> activeUsers = userRepository.findByStatus("active");

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
        user1.setRoles(List.of("admin"));
        user1.setIsTest(true);
        userRepository.save(user1);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setPasswordHash("hashedPassword2");
        user2.setTermsAcceptedAt(Instant.now());
        user2.setStatus("active");
        user2.setRoles(List.of("ROLE_USER"));
        user2.setIsTest(true);
        userRepository.save(user2);

        // Act
        List<User> adminUsers = userRepository.findByRole("admin");

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
        userRepository.save(user);

        // Act
        Optional<User> foundUser = userRepository.findPasswordHashByEmail("test@example.com");

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getPasswordHash()).isEqualTo("hashedPassword");
    }
}