package com.marvel.marveljourney.repository;

import com.marvel.marveljourney.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String>, UserRepositoryCustom {
    Optional<User> findByEmail(String email);
    List<User> findByStatus(String status);
    List<User> findByRole(String role);
    Optional<User> findPasswordHashByEmail(String email);
    void verifyEmail(String email);
    boolean emailIsVerified(String email);
    User findByRefreshTokenHash(String refreshTokenHash);
}