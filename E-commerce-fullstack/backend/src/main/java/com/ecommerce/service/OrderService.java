package com.ecommerce.service;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.dto.PageResponse;

public interface OrderService {

    /** Turns the user's server-side cart into an order, atomically. */
    OrderResponse createOrder(Long userId, CreateOrderRequest request);

    PageResponse<OrderResponse> myOrders(Long userId, int page, int size);

    /** 404 (not 403) when the order belongs to someone else, so ids cannot be probed. */
    OrderResponse myOrder(Long userId, Long orderId);

    // ---- admin ----

    PageResponse<OrderResponse> allOrders(String status, int page, int size);

    OrderResponse getOrder(Long orderId);

    OrderResponse updateStatus(Long orderId, String status);

    void deleteOrder(Long orderId);
}
