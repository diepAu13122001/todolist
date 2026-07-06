package com.diepau.todolist.service;

import com.diepau.todolist.model.User;
import com.diepau.todolist.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Register a new account; fails if the username is taken. Password is hashed before saving.
    public User register(String username, String rawPassword) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        String hashed = passwordEncoder.encode(rawPassword);
        return userRepository.save(new User(username, hashed));
    }

    // Return true if the username exists and the password matches.
    public boolean checkLogin(String username, String rawPassword) {
        return userRepository.findByUsername(username)
                .map(u -> passwordEncoder.matches(rawPassword, u.getPassword()))
                .orElse(false);
    }
}
