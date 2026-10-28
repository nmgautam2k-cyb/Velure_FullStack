package com.ecommerce.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddCartItemRequest(
        @NotNull(message = "productId is required") Long productId,

        @NotNull(message = "quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 100, message = "Quantity cannot exceed 100")
        Integer quantity) {
}
