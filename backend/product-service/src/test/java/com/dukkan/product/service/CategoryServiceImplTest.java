package com.dukkan.product.service;

import com.dukkan.product.dto.CategoryDTO;
import com.dukkan.product.exception.DuplicateResourceException;
import com.dukkan.product.exception.ResourceNotFoundException;
import com.dukkan.product.mapper.CategoryMapper;
import com.dukkan.product.model.Category;
import com.dukkan.product.repository.CategoryRepository;
import com.dukkan.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private UUID testId;
    private Category testCategory;
    private CategoryDTO testCategoryDTO;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();

        testCategory = Category.builder()
                .id(testId)
                .name("Electronics")
                .description("Electronic products")
                .isActive(true)
                .build();

        testCategoryDTO = CategoryDTO.builder()
                .id(testId)
                .name("Electronics")
                .description("Electronic products")
                .isActive(true)
                .build();
    }

    @Test
    void createCategory_Success() {
        // Given
        when(categoryRepository.existsByName(testCategoryDTO.getName())).thenReturn(false);
        when(categoryMapper.toEntity(testCategoryDTO)).thenReturn(testCategory);
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        when(categoryMapper.toDTO(testCategory)).thenReturn(testCategoryDTO);

        // When
        CategoryDTO result = categoryService.createCategory(testCategoryDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Electronics");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_ThrowsException_WhenNameExists() {
        // Given
        when(categoryRepository.existsByName(testCategoryDTO.getName())).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> categoryService.createCategory(testCategoryDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("name");

        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void getCategoryById_Success() {
        // Given
        when(categoryRepository.findById(testId)).thenReturn(Optional.of(testCategory));
        when(categoryMapper.toDTO(testCategory)).thenReturn(testCategoryDTO);

        // When
        CategoryDTO result = categoryService.getCategoryById(testId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testId);
        assertThat(result.getName()).isEqualTo("Electronics");
    }

    @Test
    void getCategoryById_ThrowsException_WhenNotFound() {
        // Given
        when(categoryRepository.findById(testId)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> categoryService.getCategoryById(testId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category");
    }

    @Test
    void deleteCategory_Success() {
        // Given
        when(categoryRepository.findById(testId)).thenReturn(Optional.of(testCategory));
        when(productRepository.countByCategoryId(testId)).thenReturn(0L);
        when(categoryRepository.countByParentCategoryId(testId)).thenReturn(0L);

        // When
        categoryService.deleteCategory(testId);

        // Then
        verify(categoryRepository).delete(testCategory);
    }
}
