package com.ecommerce.dto;

import com.ecommerce.entity.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** The order contents and prices come from the user's server-side cart, never from this request. */
public record CreateOrderRequest(
        @NotBlank(message = "Shipping address is required")
        @Size(min = 10, max = 1000, message = "Shipping address must be between 10 and 1000 characters")
        String shippingAddress,

        @NotNull(message = "Payment method is required (COD, GOOGLE_PAY or PHONEPE)")
        PaymentMethod paymentMethod) {
}
