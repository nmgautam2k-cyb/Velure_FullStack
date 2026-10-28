package com.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "Category name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        /** Optional; derived from the name when omitted. */
        @Size(max = 100, message = "Slug must be at most 100 characters")
        @Pattern(regexp = "^[a-z0-9]+(-[a-z0-9]+)*$", message = "Slug may contain lowercase letters, digits and hyphens only")
        String slug,

        @Size(max = 500, message = "Description must be at most 500 characters")
        String description) {
}
