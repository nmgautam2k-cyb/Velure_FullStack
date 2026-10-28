package com.ecommerce.entity;

import com.ecommerce.exception.BadRequestException;

public enum OrderStatus {
    PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED;

    /** Lower-case name used by the frontend ("pending", "shipped", ...). */
    public String apiName() {
        return name().toLowerCase();
    }

    public static OrderStatus from(String value) {
        if (value != null) {
            for (OrderStatus s : values()) {
                if (s.name().equalsIgnoreCase(value.trim())) {
                    return s;
                }
            }
        }
        throw new BadRequestException("Status must be one of: pending, processing, shipped, delivered, cancelled");
    }
}
