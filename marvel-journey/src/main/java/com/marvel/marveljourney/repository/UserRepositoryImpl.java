package com.marvel.marveljourney.repository;

import com.marvel.marveljourney.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Optional;

public class UserRepositoryImpl implements UserRepositoryCustom {

    private static final Logger logger = LoggerFactory.getLogger(UserRepositoryImpl.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Optional<User> findByEmail(String email) {
        logger.debug("Procurando usuário por email: {}", email);
        Query query = new Query(Criteria.where("email").is(email));
        return Optional.ofNullable(mongoTemplate.findOne(query, User.class));
    }

    @Override
    public List<User> findByStatus(String status) {
        logger.debug("Procurando usuários por status: {}", status);
        Query query = new Query(Criteria.where("status").is(status));
        return mongoTemplate.find(query, User.class);
    }

    @Override
    public List<User> findByRole(String role) {
        logger.debug("Procurando usuários por role: {}", role);
        Query query = new Query(Criteria.where("roles").is(role));
        return mongoTemplate.find(query, User.class);
    }

    @Override
    public Optional<User> findPasswordHashByEmail(String email) {
        logger.debug("Procurando hash da senha por email: {}", email);
        Query query = new Query(Criteria.where("email").is(email));
        query.fields().include("passwordHash");
        return Optional.ofNullable(mongoTemplate.findOne(query, User.class));
    }

    @Override
    public void deleteAllByIsTest(boolean isTest) {
        logger.debug("Deletando todos os usuários de teste: {}", isTest);
        Query query = new Query(Criteria.where("isTest").is(isTest));
        mongoTemplate.remove(query, User.class);
    }

    @Override
    public void verifyEmail(String email) {
        logger.debug("Verificando email do usuário: {}", email);
        Query query = new Query(Criteria.where("email").is(email));
        User user = mongoTemplate.findOne(query, User.class);
        if (user != null) {
            if (user.getVerificationCode().isEmailIsVerified() == true) {
                throw new RuntimeException("Email is verified already");
            } else {
                user.getVerificationCode().setEmailIsVerified(true);
                mongoTemplate.save(user);
            }
        }
    }

    @Override
    public boolean emailIsVerified(String email) {
        logger.debug("Verificando se o email do usuário está verificado: {}", email);
        Query query = new Query(Criteria.where("email").is(email));
        User user = mongoTemplate.findOne(query, User.class);
        if (user != null) {
            return user.getVerificationCode().isEmailIsVerified();
        }
        return false;
    }
}