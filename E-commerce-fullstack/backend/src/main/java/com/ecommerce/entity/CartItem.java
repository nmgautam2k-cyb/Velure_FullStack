package com.ecommerce.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "cart_items",
        uniqueConstraints = @UniqueConstraint(name = "uk_cart_items_cart_product", columnNames = {"cart_id", "product_id"}),
        indexes = @Index(name = "idx_cart_items_product", columnList = "product_id"))
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cart_items_cart"))
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cart_items_product"))
    private Product product;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    /** Last-seen price; refreshed from the product on every cart read. Totals always use the live product price. */
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    public Long getId() { return id; }
    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}
