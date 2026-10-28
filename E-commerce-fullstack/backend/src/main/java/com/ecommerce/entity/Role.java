package com.ecommerce.entity;

import com.ecommerce.exception.BadRequestException;

public enum Role {
    ADMIN("admin"),
    USER("customer");

    private final String apiName;

    Role(String apiName) {
        this.apiName = apiName;
    }

    /** Name used by the frontend ("admin" / "customer"). */
    public String apiName() {
        return apiName;
    }

    public static Role fromApiName(String value) {
        if (value != null) {
            String v = value.trim();
            for (Role r : values()) {
                if (r.apiName.equalsIgnoreCase(v) || r.name().equalsIgnoreCase(v)) {
                    return r;
                }
            }
        }
        throw new BadRequestException("Role must be 'admin' or 'customer'");
    }
}
