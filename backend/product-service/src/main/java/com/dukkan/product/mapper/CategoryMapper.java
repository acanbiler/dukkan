package com.dukkan.product.mapper;

import com.dukkan.product.dto.CategoryDTO;
import com.dukkan.product.model.Category;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Category entity and CategoryDTO.
 * Following Single Responsibility Principle.
 */
@Component
public class CategoryMapper {

    /**
     * Convert Category entity to CategoryDTO
     */
    public CategoryDTO toDTO(Category category) {
        if (category == null) {
            return null;
        }

        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentCategoryId(category.getParentCategoryId())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    /**
     * Convert CategoryDTO to Category entity
     */
    public Category toEntity(CategoryDTO dto) {
        if (dto == null) {
            return null;
        }

        return Category.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .parentCategoryId(dto.getParentCategoryId())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();
    }

    /**
     * Update existing Category entity from CategoryDTO
     */
    public void updateEntityFromDTO(CategoryDTO dto, Category category) {
        if (dto == null || category == null) {
            return;
        }

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setParentCategoryId(dto.getParentCategoryId());

        if (dto.getIsActive() != null) {
            category.setIsActive(dto.getIsActive());
        }
    }
}
