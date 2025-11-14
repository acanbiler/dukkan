package com.dukkan.product.mapper;

import com.dukkan.product.dto.ProductDTO;
import com.dukkan.product.model.Product;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * Mapper for converting between Product entity and ProductDTO.
 * Following Single Responsibility Principle.
 */
@Component
public class ProductMapper {

    /**
     * Convert Product entity to ProductDTO
     */
    public ProductDTO toDTO(Product product) {
        if (product == null) {
            return null;
        }

        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .sku(product.getSku())
                .categoryId(product.getCategoryId())
                .imageUrls(product.getImageUrls() != null ? new ArrayList<>(product.getImageUrls()) : new ArrayList<>())
                .isActive(product.getIsActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .inStock(product.isInStock())
                .lowStock(product.isLowStock())
                .build();
    }

    /**
     * Convert ProductDTO to Product entity
     */
    public Product toEntity(ProductDTO dto) {
        if (dto == null) {
            return null;
        }

        return Product.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity())
                .sku(dto.getSku())
                .categoryId(dto.getCategoryId())
                .imageUrls(dto.getImageUrls() != null ? new ArrayList<>(dto.getImageUrls()) : new ArrayList<>())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();
    }

    /**
     * Update existing Product entity from ProductDTO
     */
    public void updateEntityFromDTO(ProductDTO dto, Product product) {
        if (dto == null || product == null) {
            return;
        }

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setSku(dto.getSku());
        product.setCategoryId(dto.getCategoryId());

        if (dto.getImageUrls() != null) {
            product.setImageUrls(new ArrayList<>(dto.getImageUrls()));
        }

        if (dto.getIsActive() != null) {
            product.setIsActive(dto.getIsActive());
        }
    }
}
