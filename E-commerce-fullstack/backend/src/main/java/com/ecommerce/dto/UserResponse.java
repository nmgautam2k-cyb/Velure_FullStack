package com.ecommerce.dto;

import com.ecommerce.entity.User;
import java.time.Instant;

/** Never contains the password or its hash. {@code role} is "admin" or "customer". */
public record UserResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String role,
        Instant createdAt) {

    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getPhone(),
                u.getRole().apiName(), u.getCreatedAt());
    }
}
