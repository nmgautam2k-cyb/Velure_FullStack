package com.ecommerce.service.impl;

import com.ecommerce.dto.AddCartItemRequest;
import com.ecommerce.dto.CartItemResponse;
import com.ecommerce.dto.CartResponse;
import com.ecommerce.dto.ProductResponse;
import com.ecommerce.dto.UpdateCartItemRequest;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.CartService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartServiceImpl(CartRepository cartRepository, ProductRepository productRepository,
                           UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public CartResponse getCart(Long userId) {
        return toResponse(getOrCreateCart(userId));
    }

    @Override
    @Transactional
    public CartResponse addItem(Long userId, AddCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product " + request.productId() + " not found"));

        int existing = cart.findItemByProduct(product.getId()).map(CartItem::getQuantity).orElse(0);
        int wanted = existing + request.quantity();
        ensureStock(product, wanted);

        CartItem item = cart.findItemByProduct(product.getId()).orElse(null);
        if (item == null) {
            item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            cart.getItems().add(item);
        }
        item.setQuantity(wanted);
        item.setUnitPrice(product.getPrice());
        cartRepository.saveAndFlush(cart);
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateItem(Long userId, Long itemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        // Looked up inside THIS user's cart only: another user's item id simply yields 404.
        CartItem item = cart.findItem(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item " + itemId + " not found"));
        ensureStock(item.getProduct(), request.quantity());
        item.setQuantity(request.quantity());
        item.setUnitPrice(item.getProduct().getPrice());
        cartRepository.saveAndFlush(cart);
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse removeItem(Long userId, Long itemId) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = cart.findItem(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item " + itemId + " not found"));
        cart.getItems().remove(item); // orphanRemoval deletes the row
        cartRepository.saveAndFlush(cart);
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse clear(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        cartRepository.saveAndFlush(cart);
        return toResponse(cart);
    }

    private Cart getOrCreateCart(Long userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUser(userRepository.getReferenceById(userId));
            return cartRepository.save(cart);
        });
    }

    private static void ensureStock(Product product, int wantedQuantity) {
        if (product.getStockQuantity() <= 0) {
            throw new BadRequestException("'" + product.getName() + "' is out of stock");
        }
        if (wantedQuantity > product.getStockQuantity()) {
            throw new BadRequestException("Only " + product.getStockQuantity() + " unit(s) of '"
                    + product.getName() + "' are available");
        }
    }

    /** Builds the response from CURRENT product prices; nothing the client sends is trusted. */
    private CartResponse toResponse(Cart cart) {
        BigDecimal total = BigDecimal.ZERO;
        int count = 0;
        List<CartItemResponse> items = new java.util.ArrayList<>();
        for (CartItem item : cart.getItems()) {
            Product p = item.getProduct();
            BigDecimal line = p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            if (item.getUnitPrice() == null || item.getUnitPrice().compareTo(p.getPrice()) != 0) {
                item.setUnitPrice(p.getPrice()); // keep the stored snapshot in sync (flushed with the transaction)
            }
            total = total.add(line);
            count += item.getQuantity();
            String image = (p.getImageUrl() == null || p.getImageUrl().isBlank())
                    ? ProductResponse.PLACEHOLDER_IMAGE : p.getImageUrl();
            items.add(new CartItemResponse(item.getId(), p.getId(), p.getName(), image, p.getPrice(),
                    item.getQuantity(), line, p.getStockQuantity()));
        }
        return new CartResponse(cart.getId(), items, count, total);
    }
}
