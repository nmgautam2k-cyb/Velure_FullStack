package com.ecommerce.dto;

import com.ecommerce.entity.Product;
import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id,
        String title,
        String description,
        BigDecimal price,
        int stockQuantity,
        String image,
        String category,
        String categoryName,
        Instant createdAt) {

    public static final String PLACEHOLDER_IMAGE = "/placeholder.png";

    public static ProductResponse from(Product p) {
        String image = (p.getImageUrl() == null || p.getImageUrl().isBlank()) ? PLACEHOLDER_IMAGE : p.getImageUrl();
        return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getStockQuantity(),
                image, p.getCategory().getSlug(), p.getCategory().getName(), p.getCreatedAt());
    }
}
