package com.ecommerce.controller;

import com.ecommerce.dto.ProductResponse;
import com.ecommerce.security.UserPrincipal;
import com.ecommerce.service.WishlistService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public List<ProductResponse> list(@AuthenticationPrincipal UserPrincipal me) {
        return wishlistService.list(me.getId());
    }

    @PostMapping("/{productId}")
    public ResponseEntity<List<ProductResponse>> add(@AuthenticationPrincipal UserPrincipal me,
                                                     @PathVariable Long productId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(wishlistService.add(me.getId(), productId));
    }

    @DeleteMapping("/{productId}")
    public List<ProductResponse> remove(@AuthenticationPrincipal UserPrincipal me, @PathVariable Long productId) {
        return wishlistService.remove(me.getId(), productId);
    }
}
