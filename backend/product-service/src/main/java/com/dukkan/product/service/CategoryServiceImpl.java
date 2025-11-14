package com.dukkan.product.service;

import com.dukkan.product.dto.CategoryDTO;
import com.dukkan.product.exception.DuplicateResourceException;
import com.dukkan.product.exception.InvalidOperationException;
import com.dukkan.product.exception.ResourceNotFoundException;
import com.dukkan.product.mapper.CategoryMapper;
import com.dukkan.product.model.Category;
import com.dukkan.product.repository.CategoryRepository;
import com.dukkan.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of CategoryService.
 * Handles category business logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        log.debug("Creating new category: {}", categoryDTO.getName());

        // Validate category name uniqueness
        if (categoryRepository.existsByName(categoryDTO.getName())) {
            throw new DuplicateResourceException("Category", "name", categoryDTO.getName());
        }

        // Validate parent category exists if specified
        if (categoryDTO.getParentCategoryId() != null) {
            categoryRepository.findById(categoryDTO.getParentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryDTO.getParentCategoryId()));
        }

        Category category = categoryMapper.toEntity(categoryDTO);
        Category savedCategory = categoryRepository.save(category);

        log.info("Created category with id: {}", savedCategory.getId());
        return categoryMapper.toDTO(savedCategory);
    }

    @Override
    public CategoryDTO getCategoryById(UUID id) {
        log.debug("Fetching category with id: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        return categoryMapper.toDTO(category);
    }

    @Override
    public List<CategoryDTO> getAllCategories() {
        log.debug("Fetching all categories");

        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryDTO> getActiveCategories() {
        log.debug("Fetching all active categories");

        return categoryRepository.findByIsActive(true)
                .stream()
                .map(categoryMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryDTO> getRootCategories() {
        log.debug("Fetching all root categories");

        return categoryRepository.findRootCategories()
                .stream()
                .map(categoryMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryDTO> getChildCategories(UUID parentId) {
        log.debug("Fetching child categories for parent: {}", parentId);

        // Verify parent exists
        categoryRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", parentId));

        return categoryRepository.findByParentCategoryIdAndIsActive(parentId, true)
                .stream()
                .map(categoryMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(UUID id, CategoryDTO categoryDTO) {
        log.debug("Updating category with id: {}", id);

        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Validate category name uniqueness (excluding current category)
        if (!existingCategory.getName().equals(categoryDTO.getName()) &&
                categoryRepository.existsByNameAndIdNot(categoryDTO.getName(), id)) {
            throw new DuplicateResourceException("Category", "name", categoryDTO.getName());
        }

        // Validate parent category exists if specified
        if (categoryDTO.getParentCategoryId() != null) {
            // Prevent setting self as parent
            if (categoryDTO.getParentCategoryId().equals(id)) {
                throw new InvalidOperationException("Category cannot be its own parent");
            }

            categoryRepository.findById(categoryDTO.getParentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryDTO.getParentCategoryId()));
        }

        categoryMapper.updateEntityFromDTO(categoryDTO, existingCategory);
        Category updatedCategory = categoryRepository.save(existingCategory);

        log.info("Updated category with id: {}", id);
        return categoryMapper.toDTO(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID id) {
        log.debug("Deleting category with id: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Check if category has products
        long productCount = productRepository.countByCategoryId(id);
        if (productCount > 0) {
            throw new InvalidOperationException(
                    String.format("Cannot delete category with %d products. Remove products first.", productCount)
            );
        }

        // Check if category has child categories
        List<Category> childCategories = categoryRepository.findByParentCategoryId(id);
        if (!childCategories.isEmpty()) {
            throw new InvalidOperationException(
                    String.format("Cannot delete category with %d child categories. Remove child categories first.", childCategories.size())
            );
        }

        categoryRepository.delete(category);
        log.info("Deleted category with id: {}", id);
    }

    @Override
    @Transactional
    public CategoryDTO activateCategory(UUID id) {
        log.debug("Activating category with id: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        category.activate();
        Category savedCategory = categoryRepository.save(category);

        log.info("Activated category with id: {}", id);
        return categoryMapper.toDTO(savedCategory);
    }

    @Override
    @Transactional
    public CategoryDTO deactivateCategory(UUID id) {
        log.debug("Deactivating category with id: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        category.deactivate();
        Category savedCategory = categoryRepository.save(category);

        log.info("Deactivated category with id: {}", id);
        return categoryMapper.toDTO(savedCategory);
    }
}
