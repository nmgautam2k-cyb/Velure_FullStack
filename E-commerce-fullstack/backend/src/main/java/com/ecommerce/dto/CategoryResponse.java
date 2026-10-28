package com.ecommerce.dto;

import com.ecommerce.entity.Category;

public record CategoryResponse(Long id, String name, String slug, String description) {

    public static CategoryResponse from(Category c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getSlug(), c.getDescription());
    }
}
