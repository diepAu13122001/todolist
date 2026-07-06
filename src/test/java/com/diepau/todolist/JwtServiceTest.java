package com.diepau.todolist;

import com.diepau.todolist.security.JwtService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Pure unit test (no Spring context, no database): a token must round-trip back to the same username.
class JwtServiceTest {

    private final JwtService jwtService =
            new JwtService("test-secret-key-at-least-32-characters-1234", 3600000L);

    @Test
    void generateThenExtract_returnsSameUsername() {
        String token = jwtService.generateToken("diepau");
        assertEquals("diepau", jwtService.extractUsername(token));
    }

    @Test
    void tamperedToken_throws() {
        String token = jwtService.generateToken("diepau");
        String broken = token + "abc";
        assertThrows(Exception.class, () -> jwtService.extractUsername(broken));
    }
}
