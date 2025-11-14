package com.dukkan.product.repository;

import com.dukkan.product.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Product entity.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    /**
     * Find product by SKU
     */
    Optional<Product> findBySku(String sku);

    /**
     * Find all active products (paginated)
     */
    Page<Product> findByIsActive(Boolean isActive, Pageable pageable);

    /**
     * Find products by category (paginated)
     */
    Page<Product> findByCategoryIdAndIsActive(UUID categoryId, Boolean isActive, Pageable pageable);

    /**
     * Find products by category (including inactive)
     */
    Page<Product> findByCategoryId(UUID categoryId, Pageable pageable);

    /**
     * Search products by name (case-insensitive, paginated)
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND p.isActive = true")
    Page<Product> searchByName(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Search products by name or description (case-insensitive, paginated)
     */
    @Query("SELECT p FROM Product p WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND p.isActive = true")
    Page<Product> searchByNameOrDescription(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find products with low stock (stock quantity <= threshold)
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= :threshold AND p.isActive = true")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);

    /**
     * Find out of stock products
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity = 0 AND p.isActive = true")
    List<Product> findOutOfStockProducts();

    /**
     * Check if SKU already exists (for validation)
     */
    boolean existsBySku(String sku);

    /**
     * Check if SKU exists excluding a specific product (for updates)
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Product p WHERE p.sku = :sku AND p.id != :excludeId")
    boolean existsBySkuAndIdNot(@Param("sku") String sku, @Param("excludeId") UUID excludeId);

    /**
     * Count products by category
     */
    long countByCategoryId(UUID categoryId);

    /**
     * Count active products by category
     */
    long countByCategoryIdAndIsActive(UUID categoryId, Boolean isActive);
}
