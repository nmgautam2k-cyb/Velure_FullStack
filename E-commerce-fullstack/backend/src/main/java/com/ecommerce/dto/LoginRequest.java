package com.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;

/** {@code role} is optional; when sent ("admin" / "customer") it must match the account's role. */
public record LoginRequest(
        @NotBlank(message = "Email is required") String email,
        @NotBlank(message = "Password is required") String password,
        String role) {
}
