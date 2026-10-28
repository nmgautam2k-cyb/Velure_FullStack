package com.ecommerce.dto;

import com.ecommerce.entity.Order;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** {@code status} is lower-case ("pending", "shipped", ...) to match the storefront. */
public record OrderResponse(
        Long id,
        Long userId,
        String customerName,
        String customerEmail,
        String status,
        String paymentMethod,
        String paymentMethodLabel,
        String paymentStatus,
        String shippingAddress,
        BigDecimal totalAmount,
        Instant createdAt,
        List<OrderItemResponse> items) {

    public static OrderResponse from(Order o) {
        return new OrderResponse(
                o.getId(),
                o.getUser().getId(),
                o.getUser().getFullName(),
                o.getUser().getEmail(),
                o.getOrderStatus().apiName(),
                o.getPaymentMethod().name(),
                o.getPaymentMethod().label(),
                o.getPaymentStatus().name(),
                o.getShippingAddress(),
                o.getTotalAmount(),
                o.getCreatedAt(),
                o.getItems().stream().map(OrderItemResponse::from).toList());
    }
}
