package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
