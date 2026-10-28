package com.ecommerce.dto;

import java.math.BigDecimal;
import java.util.List;

/** Totals are computed on the server from current product prices. */
public record CartResponse(Long id, List<CartItemResponse> items, int itemCount, BigDecimal total) {
}
