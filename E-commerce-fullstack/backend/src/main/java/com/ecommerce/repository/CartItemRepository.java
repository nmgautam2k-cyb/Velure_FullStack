package com.ecommerce.repository;

import com.ecommerce.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from CartItem ci where ci.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);
}
