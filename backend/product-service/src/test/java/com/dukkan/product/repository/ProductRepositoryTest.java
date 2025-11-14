package com.dukkan.product.repository;

import com.dukkan.product.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
    }

    @Test
    void findBySku_ReturnsProduct_WhenExists() {
        // Given
        Product product = Product.builder()
                .sku("TEST-SKU-001")
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .stockQuantity(10)
                .categoryId(categoryId)
                .isActive(true)
                .build();
        entityManager.persist(product);
        entityManager.flush();

        // When
        Optional<Product> found = productRepository.findBySku("TEST-SKU-001");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Product");
        assertThat(found.get().getSku()).isEqualTo("TEST-SKU-001");
    }

    @Test
    void findBySku_ReturnsEmpty_WhenNotExists() {
        // When
        Optional<Product> found = productRepository.findBySku("NON-EXISTENT");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    void existsBySku_ReturnsTrue_WhenExists() {
        // Given
        Product product = Product.builder()
                .sku("TEST-SKU-002")
                .name("Test Product 2")
                .price(new BigDecimal("49.99"))
                .stockQuantity(5)
                .categoryId(categoryId)
                .build();
        entityManager.persist(product);
        entityManager.flush();

        // When
        boolean exists = productRepository.existsBySku("TEST-SKU-002");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void findByIsActive_ReturnsOnlyActiveProducts() {
        // Given
        Product activeProduct = Product.builder()
                .sku("ACTIVE-SKU")
                .name("Active Product")
                .price(new BigDecimal("99.99"))
                .stockQuantity(10)
                .categoryId(categoryId)
                .isActive(true)
                .build();

        Product inactiveProduct = Product.builder()
                .sku("INACTIVE-SKU")
                .name("Inactive Product")
                .price(new BigDecimal("79.99"))
                .stockQuantity(5)
                .categoryId(categoryId)
                .isActive(false)
                .build();

        entityManager.persist(activeProduct);
        entityManager.persist(inactiveProduct);
        entityManager.flush();

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Product> activePage = productRepository.findByIsActive(true, pageable);

        // Then
        assertThat(activePage.getContent()).hasSize(1);
        assertThat(activePage.getContent().get(0).getSku()).isEqualTo("ACTIVE-SKU");
    }

    @Test
    void findLowStockProducts_ReturnsProductsBelowThreshold() {
        // Given
        Product lowStock = Product.builder()
                .sku("LOW-STOCK")
                .name("Low Stock Product")
                .price(new BigDecimal("29.99"))
                .stockQuantity(5)
                .categoryId(categoryId)
                .isActive(true)
                .build();

        Product normalStock = Product.builder()
                .sku("NORMAL-STOCK")
                .name("Normal Stock Product")
                .price(new BigDecimal("39.99"))
                .stockQuantity(20)
                .categoryId(categoryId)
                .isActive(true)
                .build();

        entityManager.persist(lowStock);
        entityManager.persist(normalStock);
        entityManager.flush();

        // When
        var lowStockProducts = productRepository.findLowStockProducts(10);

        // Then
        assertThat(lowStockProducts).hasSize(1);
        assertThat(lowStockProducts.get(0).getSku()).isEqualTo("LOW-STOCK");
    }
}
