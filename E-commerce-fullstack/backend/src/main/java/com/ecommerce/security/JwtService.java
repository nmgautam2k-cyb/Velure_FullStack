package com.ecommerce.security;

import com.ecommerce.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey key;
    private final Duration lifetime;

    public JwtService(AppProperties props) {
        String secret = props.jwt().secret();
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET must be set and at least 32 bytes long. Generate one with: openssl rand -base64 48");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.lifetime = Duration.ofMinutes(props.jwt().expirationMinutes());
    }

    public String generateToken(UserPrincipal principal) {
        Date now = new Date();
        return Jwts.builder()
                .subject(principal.getUsername())
                .claim("uid", principal.getId())
                .claim("role", principal.getRole().name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + lifetime.toMillis()))
                .signWith(key)
                .compact();
    }

    /**
     * Verifies signature and expiry and returns the subject (email).
     * Throws a {@link JwtException} (or IllegalArgumentException) when the token is invalid.
     */
    public String extractEmail(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return claims.getSubject();
    }

    public long expiresInSeconds() {
        return lifetime.toSeconds();
    }
}
