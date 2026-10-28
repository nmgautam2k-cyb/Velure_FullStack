package com.ecommerce.service.impl;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderResponse;
import com.ecommerce.dto.PageResponse;
import com.ecommerce.entity.Cart;
import com.ecommerce.entity.CartItem;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.OrderStatus;
import com.ecommerce.entity.PaymentMethod;
import com.ecommerce.entity.PaymentStatus;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CartRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.service.OrderService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderServiceImpl implements OrderService {

    private static final int MAX_PAGE_SIZE = 100;

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderServiceImpl(OrderRepository orderRepository, CartRepository cartRepository,
                            ProductRepository productRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    /**
     * One transaction: lock the products, verify stock, decrement stock, write the order, empty the cart.
     * If anything throws, everything rolls back, so stock is never decremented without an order and the
     * cart is cleared only after the order exists.
     */
    @Override
    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .filter(c -> !c.getItems().isEmpty())
                .orElseThrow(() -> new BadRequestException("Your cart is empty"));

        List<Long> productIds = cart.getItems().stream().map(i -> i.getProduct().getId()).sorted().toList();
        Map<Long, Product> locked = productRepository.lockAllByIdIn(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        Order order = new Order();
        order.setUser(userRepository.getReferenceById(userId));
        order.setShippingAddress(request.shippingAddress().trim());
        order.setPaymentMethod(request.paymentMethod());
        order.setOrderStatus(OrderStatus.PENDING);
        // No payment gateway is integrated. COD stays PENDING; the UPI buttons are an explicit TEST flow.
        order.setPaymentStatus(request.paymentMethod() == PaymentMethod.COD
                ? PaymentStatus.PENDING : PaymentStatus.TEST_SIMULATED);

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getItems()) {
            Product product = locked.get(cartItem.getProduct().getId());
            if (product == null) {
                throw new BadRequestException("'" + cartItem.getProduct().getName()
                        + "' is no longer available. Remove it from your cart");
            }
            int qty = cartItem.getQuantity();
            if (product.getStockQuantity() < qty) {
                throw new BadRequestException("Not enough stock for '" + product.getName() + "' (requested "
                        + qty + ", available " + product.getStockQuantity() + ")");
            }
            product.setStockQuantity(product.getStockQuantity() - qty);

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setProductName(product.getName());
            item.setProductImage(product.getImageUrl());
            item.setQuantity(qty);
            item.setUnitPrice(product.getPrice()); // price from the database, at this moment
            order.addItem(item);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(qty)));
        }
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);
        cart.getItems().clear(); // orphanRemoval deletes the cart rows when the transaction commits
        return OrderResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> myOrders(Long userId, int page, int size) {
        Page<Order> result = orderRepository.findByUserId(userId, pageable(page, size));
        return PageResponse.from(result, OrderResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse myOrder(Long userId, Long orderId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .map(OrderResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Order " + orderId + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> allOrders(String status, int page, int size) {
        Page<Order> result = (status == null || status.isBlank())
                ? orderRepository.findAll(pageable(page, size))
                : orderRepository.findByOrderStatus(OrderStatus.from(status), pageable(page, size));
        return PageResponse.from(result, OrderResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        return OrderResponse.from(getOrThrow(orderId));
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long orderId, String statusValue) {
        Order order = getOrThrow(orderId);
        OrderStatus next = OrderStatus.from(statusValue);
        OrderStatus current = order.getOrderStatus();

        if (next == current) {
            return OrderResponse.from(order);
        }
        if (current == OrderStatus.CANCELLED) {
            throw new BadRequestException("A cancelled order cannot be reopened");
        }
        if (next == OrderStatus.CANCELLED) {
            if (current == OrderStatus.DELIVERED) {
                throw new BadRequestException("A delivered order cannot be cancelled");
            }
            restock(order);
        }
        order.setOrderStatus(next);
        // Cash collected on delivery is the one case where the server can record a real payment.
        if (next == OrderStatus.DELIVERED && order.getPaymentMethod() == PaymentMethod.COD) {
            order.setPaymentStatus(PaymentStatus.PAID);
        }
        return OrderResponse.from(orderRepository.save(order));
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = getOrThrow(orderId);
        // Stock is still reserved by orders that were neither cancelled (already restocked) nor delivered/shipped.
        if (order.getOrderStatus() == OrderStatus.PENDING || order.getOrderStatus() == OrderStatus.PROCESSING) {
            restock(order);
        }
        orderRepository.delete(order);
    }

    private void restock(Order order) {
        List<Long> ids = order.getItems().stream().map(i -> i.getProduct().getId()).sorted().toList();
        if (ids.isEmpty()) {
            return;
        }
        Map<Long, Product> locked = productRepository.lockAllByIdIn(ids).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        for (OrderItem item : order.getItems()) {
            Product product = locked.get(item.getProduct().getId());
            if (product != null) {
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            }
        }
    }

    private Order getOrThrow(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order " + id + " not found"));
    }

    private static PageRequest pageable(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt").and(Sort.by(Sort.Direction.DESC, "id")));
    }
}
