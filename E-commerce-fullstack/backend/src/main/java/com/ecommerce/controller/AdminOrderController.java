package com.ecommerce.controller;

import com.ecommerce.dto.OrderResponse;
import com.ecommerce.dto.PageResponse;
import com.ecommerce.dto.UpdateOrderStatusRequest;
import com.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public PageResponse<OrderResponse> list(@RequestParam(required = false) String status,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "50") int size) {
        return orderService.allOrders(status, page, size);
    }

    @GetMapping("/{orderId}")
    public OrderResponse get(@PathVariable Long orderId) {
        return orderService.getOrder(orderId);
    }

    @PatchMapping("/{orderId}/status")
    public OrderResponse updateStatus(@PathVariable Long orderId, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateStatus(orderId, request.status());
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> delete(@PathVariable Long orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}
