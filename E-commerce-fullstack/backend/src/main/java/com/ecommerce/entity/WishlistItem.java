package com.ecommerce.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "wishlist_items",
        uniqueConstraints = @UniqueConstraint(name = "uk_wishlist_user_product", columnNames = {"user_id", "product_id"}),
        indexes = @Index(name = "idx_wishlist_product", columnList = "product_id"))
public class WishlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_wishlist_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_wishlist_product"))
    private Product product;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Instant getCreatedAt() { return createdAt; }
}
