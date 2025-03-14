package com.marvel.marveljourney.service;

import com.marvel.marveljourney.model.User;
import com.marvel.marveljourney.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public User saveUser(User user) {
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        return userRepository.save(user);
    }

    public User updateUser(User user) {
        user.setUpdatedAt(Instant.now());
        return userRepository.save(user);
    }

    public boolean checkEmailExists(String email) {
        return findByEmail(email) != null;
    }
}