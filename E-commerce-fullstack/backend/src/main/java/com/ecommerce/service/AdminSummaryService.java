package com.ecommerce.service;

import com.ecommerce.dto.SummaryResponse;
import com.ecommerce.entity.OrderStatus;
import com.ecommerce.entity.Role;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Numbers for the admin dashboard, computed with SQL aggregates instead of downloading every row. */
@Service
public class AdminSummaryService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public AdminSummaryService(ProductRepository productRepository, OrderRepository orderRepository,
                               UserRepository userRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public SummaryResponse summary() {
        Map<String, Long> byCategory = new LinkedHashMap<>();
        for (Object[] row : productRepository.countByCategorySlug()) {
            byCategory.put((String) row[0], ((Number) row[1]).longValue());
        }

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (OrderStatus s : OrderStatus.values()) {
            byStatus.put(s.apiName(), 0L);
        }
        for (Object[] row : orderRepository.countByStatus()) {
            byStatus.put(((OrderStatus) row[0]).apiName(), ((Number) row[1]).longValue());
        }

        BigDecimal revenue = orderRepository.sumRevenue();
        return new SummaryResponse(
                productRepository.count(),
                orderRepository.count(),
                userRepository.count(),
                userRepository.countByRole(Role.ADMIN),
                userRepository.countByRole(Role.USER),
                byCategory,
                byStatus,
                revenue == null ? BigDecimal.ZERO : revenue);
    }
}
