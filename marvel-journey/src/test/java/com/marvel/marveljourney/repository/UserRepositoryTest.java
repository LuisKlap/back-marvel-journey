package com.marvel.marveljourney.repository;

import com.marvel.marveljourney.model.User;
import com.marvel.marveljourney.model.User.Metadata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@DataMongoTest
class UserRepositoryImplTest extends EmbeddedMongoDbConfig {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private UserRepositoryImpl userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindByEmail() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        when(mongoTemplate.findOne(new Query(Criteria.where("email").is(email)), User.class)).thenReturn(user);

        Optional<User> foundUser = userRepository.findByEmail(email);

        assertTrue(foundUser.isPresent());
        assertEquals(email, foundUser.get().getEmail());
    }

    @Test
    void testFindByEmailNotFound() {
        String email = "nonexistent@example.com";
        when(mongoTemplate.findOne(new Query(Criteria.where("email").is(email)), User.class)).thenReturn(null);
    
        Optional<User> foundUser = userRepository.findByEmail(email);
    
        assertFalse(foundUser.isPresent());
    }

    @Test
    void testFindByStatus() {
        String status = "active";
        User user = new User();
        user.setStatus(status);
        when(mongoTemplate.find(new Query(Criteria.where("status").is(status)), User.class)).thenReturn(List.of(user));

        List<User> users = userRepository.findByStatus(status);

        assertFalse(users.isEmpty());
        assertEquals(status, users.get(0).getStatus());
    }

    @Test
    void testFindByStatusNotFound() {
        String status = "inactive";
        when(mongoTemplate.find(new Query(Criteria.where("status").is(status)), User.class)).thenReturn(List.of());

        List<User> users = userRepository.findByStatus(status);

        assertTrue(users.isEmpty());
    }

    @Test
    void testFindByRole() {
        String role = "admin";
        User user = new User();
        user.setRoles(List.of(role));
        when(mongoTemplate.find(new Query(Criteria.where("roles").is(role)), User.class)).thenReturn(List.of(user));

        List<User> users = userRepository.findByRole(role);

        assertFalse(users.isEmpty());
        assertTrue(users.get(0).getRoles().contains(role));
    }

    @Test
    void testFindByRoleNotFound() {
        String role = "user";
        when(mongoTemplate.find(new Query(Criteria.where("roles").is(role)), User.class)).thenReturn(List.of());

        List<User> users = userRepository.findByRole(role);

        assertTrue(users.isEmpty());
    }

    @Test
    void testFindPasswordHashByEmail() {
        String email = "test@example.com";
        String passwordHash = "hashedPassword";
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        Query query = new Query(Criteria.where("email").is(email));
        query.fields().include("passwordHash");
        when(mongoTemplate.findOne(query, User.class)).thenReturn(user);

        Optional<User> foundUser = userRepository.findPasswordHashByEmail(email);

        assertTrue(foundUser.isPresent());
        assertEquals(passwordHash, foundUser.get().getPasswordHash());
    }

    @Test
    void testFindPasswordHashByEmailNotFound() {
        String email = "nonexistent@example.com";
        Query query = new Query(Criteria.where("email").is(email));
        query.fields().include("passwordHash");
        when(mongoTemplate.findOne(query, User.class)).thenReturn(null);

        Optional<User> foundUser = userRepository.findPasswordHashByEmail(email);

        assertFalse(foundUser.isPresent());
    }

    @Test
    void testVerifyEmail() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        user.setVerificationCode(new User.VerificationCode());
        when(mongoTemplate.findOne(new Query(Criteria.where("email").is(email)), User.class)).thenReturn(user);

        userRepository.verifyEmail(email);

        assertTrue(user.getVerificationCode().isEmailIsVerified());
        verify(mongoTemplate, times(1)).save(user);
    }

    @Test
    void testEmailIsVerified() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        user.setVerificationCode(new User.VerificationCode());
        user.getVerificationCode().setEmailIsVerified(true);
        when(mongoTemplate.findOne(new Query(Criteria.where("email").is(email)), User.class)).thenReturn(user);

        boolean isVerified = userRepository.emailIsVerified(email);

        assertTrue(isVerified);
    }

    @Test
    void testEmailIsVerifiedNotFound() {
        String email = "nonexistent@example.com";
        when(mongoTemplate.findOne(new Query(Criteria.where("email").is(email)), User.class)).thenReturn(null);

        boolean isVerified = userRepository.emailIsVerified(email);

        assertFalse(isVerified);
    }

    @Test
    void testFindByRefreshTokenHash() {
        String refreshTokenHash = "hashedToken";
        User user = new User();
        Metadata metadata = new Metadata();
        metadata.setRefreshTokenHash(refreshTokenHash);
        user.setMetadata(List.of(metadata));
        when(mongoTemplate.findOne(new Query(Criteria.where("refreshTokenHash").is(refreshTokenHash)), User.class))
                .thenReturn(user);

        User foundUser = userRepository.findByRefreshTokenHash(refreshTokenHash);

        assertNotNull(foundUser);
        assertEquals(refreshTokenHash, foundUser.getMetadata().get(0).getRefreshTokenHash());
    }

    @Test
    void testFindByRefreshTokenHashNotFound() {
        String refreshTokenHash = "nonexistentHash";
        when(mongoTemplate.findOne(new Query(Criteria.where("refreshTokenHash").is(refreshTokenHash)), User.class))
                .thenReturn(null);

        User foundUser = userRepository.findByRefreshTokenHash(refreshTokenHash);

        assertNull(foundUser);
    }
}