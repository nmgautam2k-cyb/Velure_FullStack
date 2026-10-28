package com.ecommerce.dto;

import com.ecommerce.entity.OrderItem;
import java.math.BigDecimal;

public record OrderItemResponse(
        Long id,
        Long productId,
        String title,
        String image,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal) {

    public static OrderItemResponse from(OrderItem i) {
        String image = (i.getProductImage() == null || i.getProductImage().isBlank())
                ? ProductResponse.PLACEHOLDER_IMAGE : i.getProductImage();
        return new OrderItemResponse(i.getId(), i.getProduct().getId(), i.getProductName(), image, i.getQuantity(),
                i.getUnitPrice(), i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())));
    }
}
