package com.ecommerce.service;

import com.ecommerce.dto.AddCartItemRequest;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.dto.UpdateCartItemRequest;

/** Every method works on the cart of the given (authenticated) user only. */
public interface CartService {

    CartResponse getCart(Long userId);

    CartResponse addItem(Long userId, AddCartItemRequest request);

    CartResponse updateItem(Long userId, Long itemId, UpdateCartItemRequest request);

    CartResponse removeItem(Long userId, Long itemId);

    CartResponse clear(Long userId);
}
