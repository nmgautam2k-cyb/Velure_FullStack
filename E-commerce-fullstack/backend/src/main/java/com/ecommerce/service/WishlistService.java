package com.ecommerce.service;

import com.ecommerce.dto.ProductResponse;
import java.util.List;

public interface WishlistService {

    List<ProductResponse> list(Long userId);

    /** Idempotent: adding a product that is already saved is not an error. */
    List<ProductResponse> add(Long userId, Long productId);

    List<ProductResponse> remove(Long userId, Long productId);
}
