package com.dukkan.product.service;

import com.dukkan.product.dto.ProductDTO;
import com.dukkan.product.exception.DuplicateResourceException;
import com.dukkan.product.exception.InvalidOperationException;
import com.dukkan.product.exception.ResourceNotFoundException;
import com.dukkan.product.mapper.ProductMapper;
import com.dukkan.product.model.Product;
import com.dukkan.product.model.Category;
import com.dukkan.product.repository.CategoryRepository;
import com.dukkan.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private UUID testId;
    private UUID categoryId;
    private Product testProduct;
    private ProductDTO testProductDTO;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        testCategory = Category.builder()
                .id(categoryId)
                .name("Electronics")
                .build();

        testProduct = Product.builder()
                .id(testId)
                .sku("TEST-SKU-001")
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .stockQuantity(10)
                .categoryId(categoryId)
                .isActive(true)
                .build();

        testProductDTO = ProductDTO.builder()
                .id(testId)
                .sku("TEST-SKU-001")
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .stockQuantity(10)
                .categoryId(categoryId)
                .isActive(true)
                .build();
    }

    @Test
    void createProduct_Success() {
        // Given
        when(productRepository.existsBySku(testProductDTO.getSku())).thenReturn(false);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(testCategory));
        when(productMapper.toEntity(testProductDTO)).thenReturn(testProduct);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

        // When
        ProductDTO result = productService.createProduct(testProductDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSku()).isEqualTo("TEST-SKU-001");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_ThrowsException_WhenSkuExists() {
        // Given
        when(productRepository.existsBySku(testProductDTO.getSku())).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> productService.createProduct(testProductDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("sku");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void createProduct_ThrowsException_WhenCategoryNotFound() {
        // Given
        when(productRepository.existsBySku(testProductDTO.getSku())).thenReturn(false);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> productService.createProduct(testProductDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getProductById_Success() {
        // Given
        when(productRepository.findById(testId)).thenReturn(Optional.of(testProduct));
        when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

        // When
        ProductDTO result = productService.getProductById(testId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testId);
        assertThat(result.getName()).isEqualTo("Test Product");
    }

    @Test
    void getProductById_ThrowsException_WhenNotFound() {
        // Given
        when(productRepository.findById(testId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> productService.getProductById(testId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product");
    }

    @Test
    void updateProduct_Success() {
        // Given
        ProductDTO updateDTO = ProductDTO.builder()
                .sku("TEST-SKU-001")
                .name("Updated Product")
                .price(new BigDecimal("149.99"))
                .stockQuantity(20)
                .categoryId(categoryId)
                .build();

        when(productRepository.findById(testId)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(testProduct)).thenReturn(testProduct);
        when(productMapper.toDTO(testProduct)).thenReturn(updateDTO);

        // When
        ProductDTO result = productService.updateProduct(testId, updateDTO);

        // Then
        assertThat(result).isNotNull();
        verify(productMapper).updateEntityFromDTO(updateDTO, testProduct);
        verify(productRepository).save(testProduct);
    }

    @Test
    void deleteProduct_Success() {
        // Given
        when(productRepository.findById(testId)).thenReturn(Optional.of(testProduct));

        // When
        productService.deleteProduct(testId);

        // Then
        verify(productRepository).delete(testProduct);
    }

    @Test
    void deleteProduct_ThrowsException_WhenNotFound() {
        // Given
        when(productRepository.findById(testId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> productService.deleteProduct(testId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void reduceStock_Success() {
        // Given
        int reduceBy = 5;
        when(productRepository.findById(testId)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(testProduct)).thenReturn(testProduct);
        when(productMapper.toDTO(testProduct)).thenReturn(testProductDTO);

        // When
        ProductDTO result = productService.reduceStock(testId, reduceBy);

        // Then
        assertThat(result).isNotNull();
        verify(testProduct).reduceStock(reduceBy);
        verify(productRepository).save(testProduct);
    }

    @Test
    void updateStock_ThrowsException_WhenNegative() {
        // Given
        when(productRepository.findById(testId)).thenReturn(Optional.of(testProduct));

        // When/Then
        assertThatThrownBy(() -> productService.updateStock(testId, -5))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("negative");

        verify(productRepository, never()).save(any(Product.class));
    }
}
