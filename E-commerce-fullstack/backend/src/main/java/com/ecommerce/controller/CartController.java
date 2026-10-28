package com.ecommerce.controller;

import com.ecommerce.dto.AddCartItemRequest;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.dto.UpdateCartItemRequest;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The cart is always the authenticated user's own: there is no user id in any URL to tamper with. */
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartResponse get(@AuthenticationPrincipal UserPrincipal me) {
        return cartService.getCart(me.getId());
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> add(@AuthenticationPrincipal UserPrincipal me,
                                            @Valid @RequestBody AddCartItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItem(me.getId(), request));
    }

    @PutMapping("/items/{itemId}")
    public CartResponse update(@AuthenticationPrincipal UserPrincipal me, @PathVariable Long itemId,
                               @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateItem(me.getId(), itemId, request);
    }

    @DeleteMapping("/items/{itemId}")
    public CartResponse remove(@AuthenticationPrincipal UserPrincipal me, @PathVariable Long itemId) {
        return cartService.removeItem(me.getId(), itemId);
    }

    @DeleteMapping
    public CartResponse clear(@AuthenticationPrincipal UserPrincipal me) {
        return cartService.clear(me.getId());
    }
}
