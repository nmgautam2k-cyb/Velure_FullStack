package com.ecommerce.controller;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.dto.PageResponse;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@AuthenticationPrincipal UserPrincipal me,
                                                @Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(me.getId(), request));
    }

    @GetMapping("/my-orders")
    public PageResponse<OrderResponse> myOrders(@AuthenticationPrincipal UserPrincipal me,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "50") int size) {
        return orderService.myOrders(me.getId(), page, size);
    }

    @GetMapping("/{orderId}")
    public OrderResponse get(@AuthenticationPrincipal UserPrincipal me, @PathVariable Long orderId) {
        return orderService.myOrder(me.getId(), orderId);
    }
}
