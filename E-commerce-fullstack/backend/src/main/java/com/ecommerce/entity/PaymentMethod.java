package com.ecommerce.entity;

public enum PaymentMethod {
    COD("Cash on Delivery"),
    GOOGLE_PAY("Google Pay"),
    PHONEPE("PhonePe");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
