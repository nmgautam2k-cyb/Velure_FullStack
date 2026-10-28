package com.ecommerce.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "carts",
        uniqueConstraints = @UniqueConstraint(name = "uk_carts_user", columnNames = "user_id"))
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_carts_user"))
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<CartItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Optional<CartItem> findItem(Long itemId) {
        return items.stream().filter(i -> i.getId() != null && i.getId().equals(itemId)).findFirst();
    }

    public Optional<CartItem> findItemByProduct(Long productId) {
        return items.stream().filter(i -> i.getProduct().getId().equals(productId)).findFirst();
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public List<CartItem> getItems() { return items; }
    public Instant getCreatedAt() { return createdAt; }
}
