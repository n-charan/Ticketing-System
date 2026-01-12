package com.example.ticketing.service;

import com.example.ticketing.model.User;

import java.util.Optional;

public interface UserService {
    User createUser(User user);
    Optional<User> findByUsername(String username);
    Optional<User> findById(Long id);
    void deleteById(Long id);
}
