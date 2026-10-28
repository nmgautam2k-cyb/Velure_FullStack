package com.ecommerce.dto;

import java.math.BigDecimal;
import java.util.Map;

public record SummaryResponse(
        long totalProducts,
        long totalOrders,
        long totalUsers,
        long adminUsers,
        long customerUsers,
        Map<String, Long> productsByCategory,
        Map<String, Long> ordersByStatus,
        BigDecimal totalRevenue) {
}
