package com.dukkan.product.service;

import com.dukkan.product.dto.CategoryDTO;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Category business logic.
 * Following Interface Segregation and Dependency Inversion Principles.
 */
public interface CategoryService {

    /**
     * Create a new category
     */
    CategoryDTO createCategory(CategoryDTO categoryDTO);

    /**
     * Get category by ID
     */
    CategoryDTO getCategoryById(UUID id);

    /**
     * Get all categories
     */
    List<CategoryDTO> getAllCategories();

    /**
     * Get all active categories
     */
    List<CategoryDTO> getActiveCategories();

    /**
     * Get all root categories (no parent)
     */
    List<CategoryDTO> getRootCategories();

    /**
     * Get child categories of a parent
     */
    List<CategoryDTO> getChildCategories(UUID parentId);

    /**
     * Update an existing category
     */
    CategoryDTO updateCategory(UUID id, CategoryDTO categoryDTO);

    /**
     * Delete a category
     */
    void deleteCategory(UUID id);

    /**
     * Activate a category
     */
    CategoryDTO activateCategory(UUID id);

    /**
     * Deactivate a category
     */
    CategoryDTO deactivateCategory(UUID id);
}
