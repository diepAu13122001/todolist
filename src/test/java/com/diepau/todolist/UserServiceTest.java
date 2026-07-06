package com.diepau.todolist;

import com.diepau.todolist.repository.UserRepository;
import com.diepau.todolist.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

// Tests for UserService using a real repository on an in-memory H2 database.
@DataJpaTest
class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, new BCryptPasswordEncoder());
    }

    @Test
    void register_storesHashedPassword() {
        userService.register("diepau", "secret123");

        var saved = userRepository.findByUsername("diepau").orElseThrow();
        assertNotEquals("secret123", saved.getPassword(), "Password must not be stored in plain text");
        assertTrue(saved.getPassword().startsWith("$2"), "Password should be a bcrypt hash");
    }

    @Test
    void register_duplicateUsername_throws() {
        userService.register("diepau", "secret123");

        assertThrows(IllegalArgumentException.class,
                () -> userService.register("diepau", "another123"));
    }

    @Test
    void checkLogin_correctPassword_returnsTrue() {
        userService.register("diepau", "secret123");
        assertTrue(userService.checkLogin("diepau", "secret123"));
    }

    @Test
    void checkLogin_wrongPassword_returnsFalse() {
        userService.register("diepau", "secret123");
        assertFalse(userService.checkLogin("diepau", "wrongpass"));
    }

    @Test
    void checkLogin_unknownUser_returnsFalse() {
        assertFalse(userService.checkLogin("nobody", "secret123"));
    }
}
