package com.ecommerce.dto;

import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long productId,
        String title,
        String image,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal,
        int stockQuantity) {
}
