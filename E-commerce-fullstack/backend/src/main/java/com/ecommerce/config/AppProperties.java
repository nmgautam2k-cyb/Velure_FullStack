package com.ecommerce.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/** Typed view of the {@code app.*} settings in application.properties (all values come from env vars). */
@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Cors cors, Upload upload, Admin admin, String publicBaseUrl) {

    public record Jwt(String secret, long expirationMinutes) {
    }

    public record Cors(List<String> allowedOrigins) {
    }

    public record Upload(String dir) {
    }

    public record Admin(String email, String password, String fullName) {
    }
}
