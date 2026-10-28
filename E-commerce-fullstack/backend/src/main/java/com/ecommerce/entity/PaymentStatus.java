package com.ecommerce.entity;

/**
 * PENDING        - nothing has been paid yet (e.g. cash on delivery).
 * TEST_SIMULATED - the storefront's UPI buttons are a TEST flow: no gateway was contacted and no money moved.
 * PAID           - reserved for a confirmed payment (a verified gateway callback, or cash collected on delivery).
 */
public enum PaymentStatus {
    PENDING, TEST_SIMULATED, PAID
}
