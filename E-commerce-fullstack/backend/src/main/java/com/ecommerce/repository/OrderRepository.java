package com.ecommerce.repository;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserId(Long userId, Pageable pageable);

    Optional<Order> findByIdAndUserId(Long id, Long userId);

    Page<Order> findByOrderStatus(OrderStatus status, Pageable pageable);

    boolean existsByUserId(Long userId);

    @Query("select o.orderStatus, count(o) from Order o group by o.orderStatus")
    List<Object[]> countByStatus();

    /** Revenue ignores cancelled orders. Returns null when there are no matching orders. */
    @Query("select sum(o.totalAmount) from Order o where o.orderStatus <> com.ecommerce.entity.OrderStatus.CANCELLED")
    BigDecimal sumRevenue();
}
