package com.ecommerce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ecommerce.config.AppProperties;
import com.ecommerce.entity.Role;
import com.ecommerce.security.JwtService;
import com.ecommerce.security.UserPrincipal;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static AppProperties props(String secret, long minutes) {
        return new AppProperties(new AppProperties.Jwt(secret, minutes), new AppProperties.Cors(java.util.List.of()),
                new AppProperties.Upload("target/x"), new AppProperties.Admin("a@b.c", "", "A"), "http://localhost");
    }

    private static final String SECRET = "unit-test-secret-unit-test-secret-1234";

    @Test
    void tokenRoundTripReturnsEmail() {
        JwtService jwt = new JwtService(props(SECRET, 5));
        String token = jwt.generateToken(new UserPrincipal(7L, "x@y.com", "hash", Role.USER));
        assertEquals("x@y.com", jwt.extractEmail(token));
    }

    @Test
    void tamperedTokenIsRejected() {
        JwtService jwt = new JwtService(props(SECRET, 5));
        String token = jwt.generateToken(new UserPrincipal(7L, "x@y.com", "hash", Role.USER));
        assertThrows(JwtException.class, () -> jwt.extractEmail(token.substring(0, token.length() - 2) + "xx"));
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        JwtService a = new JwtService(props(SECRET, 5));
        JwtService b = new JwtService(props("another-secret-another-secret-another-1", 5));
        String token = a.generateToken(new UserPrincipal(1L, "x@y.com", "hash", Role.ADMIN));
        assertThrows(JwtException.class, () -> b.extractEmail(token));
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService jwt = new JwtService(props(SECRET, -1));
        String token = jwt.generateToken(new UserPrincipal(1L, "x@y.com", "hash", Role.USER));
        assertThrows(JwtException.class, () -> jwt.extractEmail(token));
    }

    @Test
    void shortSecretFailsFast() {
        assertThrows(IllegalStateException.class, () -> new JwtService(props("too-short", 5)));
    }
}
