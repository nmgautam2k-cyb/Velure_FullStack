package com.ecommerce.service.impl;

import com.ecommerce.dto.ProductResponse;
import com.ecommerce.entity.WishlistItem;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.repository.WishlistRepository;
import com.ecommerce.service.WishlistService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public WishlistServiceImpl(WishlistRepository wishlistRepository, ProductRepository productRepository,
                               UserRepository userRepository) {
        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> list(Long userId) {
        return wishlistRepository.findByUserIdOrderByIdDesc(userId).stream()
                .map(w -> ProductResponse.from(w.getProduct())).toList();
    }

    @Override
    @Transactional
    public List<ProductResponse> add(Long userId, Long productId) {
        if (!wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            WishlistItem item = new WishlistItem();
            item.setUser(userRepository.getReferenceById(userId));
            item.setProduct(productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product " + productId + " not found")));
            wishlistRepository.save(item);
        }
        return list(userId);
    }

    @Override
    @Transactional
    public List<ProductResponse> remove(Long userId, Long productId) {
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
        return list(userId);
    }
}
