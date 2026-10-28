package com.ecommerce.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "Product title is required")
        @Size(max = 150, message = "Title must be at most 150 characters")
        @JsonAlias("name")
        String title,

        @Size(max = 2000, message = "Description must be at most 2000 characters")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
        @Digits(integer = 10, fraction = 2, message = "Price allows at most 2 decimal places")
        BigDecimal price,

        @NotNull(message = "Stock quantity is required")
        @Min(value = 0, message = "Stock quantity cannot be negative")
        Integer stockQuantity,

        /** Category slug, e.g. "shirts". */
        @NotBlank(message = "Category is required")
        String category,

        /** A path or URL (see /api/admin/uploads). Inline base64 data is rejected. */
        @Size(max = 500, message = "Image URL must be at most 500 characters")
        @JsonAlias("imageUrl")
        String image) {
}
