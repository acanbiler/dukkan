package com.dukkan.order.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Order item entity
 */
@Entity
@Table(name = "order_items")
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false, length = 255)
    private String productName;

    @Column(length = 50)
    private String productSku;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal priceAtPurchase;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal;

    @PrePersist
    @PreUpdate
    protected void calculateSubtotal() {
        if (quantity != null && priceAtPurchase != null) {
            this.subtotal = priceAtPurchase.multiply(BigDecimal.valueOf(quantity));
        }
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductSku() {
        return productSku;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getPriceAtPurchase() {
        return priceAtPurchase;
    }

    /**
     * Get subtotal - calculates on-the-fly if not yet persisted
     */
    public BigDecimal getSubtotal() {
        if (subtotal == null && quantity != null && priceAtPurchase != null) {
            return priceAtPurchase.multiply(BigDecimal.valueOf(quantity));
        }
        return subtotal;
    }
}
