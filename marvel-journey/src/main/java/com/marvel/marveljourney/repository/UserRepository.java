package com.marvel.marveljourney.repository;

import com.marvel.marveljourney.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    
    Optional<User> findByEmail(String email);

    List<User> findByStatus(String status);

    @Query("{ 'roles': ?0 }")
    List<User> findByRole(String role);

    @Query(value = "{ 'email': ?0 }", fields = "{ 'passwordHash': 1 }")
    Optional<User> findPasswordHashByEmail(String email);

    void deleteAllByIsTest(boolean isTest); // Novo método para excluir registros de teste
}