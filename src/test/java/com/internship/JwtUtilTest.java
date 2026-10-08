package com.internship;

import com.internship.model.User;
import com.internship.security.JwtUtil;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {
    private final JwtUtil jwt = new JwtUtil("test-secret-that-is-at-least-32-bytes-long!", 60_000);

    private User user() {
        User u = new User();
        u.setId(7L);
        u.setEmail("a@b.com");
        u.setRole(User.Role.STUDENT);
        return u;
    }

    @Test
    void tokenRoundTrips() {
        String token = jwt.generateToken(user());
        assertTrue(jwt.validateToken(token));
        assertEquals("a@b.com", jwt.extractClaims(token).getSubject());
        assertEquals("STUDENT", jwt.extractClaims(token).get("role", String.class));
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = jwt.generateToken(user());
        assertFalse(jwt.validateToken(token + "x"));
        assertFalse(jwt.validateToken("not-a-token"));
    }
}
