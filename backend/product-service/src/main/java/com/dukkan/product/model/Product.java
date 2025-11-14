package com.dukkan.product.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Product entity representing a product in the catalog.
 * Contains business logic for inventory management and pricing.
 */
@Entity
@Table(name = "products", uniqueConstraints = {
    @UniqueConstraint(name = "uk_product_sku", columnNames = "sku")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Product name is required")
    @Size(max = 255, message = "Product name must not exceed 255 characters")
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have up to 10 digits and 2 decimal places")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Column(nullable = false)
    private Integer stockQuantity;

    @NotBlank(message = "SKU is required")
    @Size(max = 100, message = "SKU must not exceed 100 characters")
    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @NotNull(message = "Category is required")
    @Column(nullable = false)
    private UUID categoryId;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Business logic: Check if product is in stock
     */
    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }

    /**
     * Business logic: Check if stock is low (threshold: 10)
     */
    public boolean isLowStock() {
        return stockQuantity != null && stockQuantity > 0 && stockQuantity <= 10;
    }

    /**
     * Business logic: Reduce stock by given quantity
     * @throws IllegalArgumentException if insufficient stock
     */
    public void reduceStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        if (stockQuantity < quantity) {
            throw new IllegalArgumentException(
                String.format("Insufficient stock. Available: %d, Requested: %d", stockQuantity, quantity)
            );
        }
        this.stockQuantity -= quantity;
    }

    /**
     * Business logic: Increase stock by given quantity
     */
    public void increaseStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        this.stockQuantity += quantity;
    }

    /**
     * Business logic: Update product price
     * @throws IllegalArgumentException if price is invalid
     */
    public void updatePrice(BigDecimal newPrice) {
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        this.price = newPrice;
    }

    /**
     * Business logic: Activate the product
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Business logic: Deactivate the product
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Business logic: Add image URL
     */
    public void addImageUrl(String imageUrl) {
        if (imageUrl != null && !imageUrl.isBlank()) {
            if (this.imageUrls == null) {
                this.imageUrls = new ArrayList<>();
            }
            this.imageUrls.add(imageUrl);
        }
    }

    /**
     * Business logic: Remove image URL
     */
    public void removeImageUrl(String imageUrl) {
        if (this.imageUrls != null) {
            this.imageUrls.remove(imageUrl);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product product)) return false;
        return id != null && id.equals(product.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sku='" + sku + '\'' +
                ", price=" + price +
                ", stockQuantity=" + stockQuantity +
                ", isActive=" + isActive +
                '}';
    }
}
