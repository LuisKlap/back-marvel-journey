package com.marvel.marveljourney.repository;

import com.marvel.marveljourney.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepositoryCustom {
    Optional<User> findByEmail(String email);
    List<User> findByStatus(String status);
    List<User> findByRole(String role);
    Optional<User> findPasswordHashByEmail(String email);
    void deleteAllByIsTest(boolean isTest);
    void verifyEmail(String email);
    boolean emailIsVerified(String email);
}