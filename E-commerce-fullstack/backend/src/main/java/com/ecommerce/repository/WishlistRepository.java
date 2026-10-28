package com.ecommerce.repository;

import com.ecommerce.entity.WishlistItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {

    List<WishlistItem> findByUserIdOrderByIdDesc(Long userId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from WishlistItem w where w.user.id = :userId and w.product.id = :productId")
    int deleteByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from WishlistItem w where w.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from WishlistItem w where w.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
