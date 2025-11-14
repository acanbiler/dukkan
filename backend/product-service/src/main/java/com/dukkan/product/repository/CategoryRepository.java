package com.dukkan.product.repository;

import com.dukkan.product.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Category entity.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    /**
     * Find category by name
     */
    Optional<Category> findByName(String name);

    /**
     * Find all active categories
     */
    List<Category> findByIsActive(Boolean isActive);

    /**
     * Find all root categories (categories with no parent)
     */
    @Query("SELECT c FROM Category c WHERE c.parentCategoryId IS NULL AND c.isActive = true")
    List<Category> findRootCategories();

    /**
     * Find all child categories of a parent category
     */
    List<Category> findByParentCategoryIdAndIsActive(UUID parentCategoryId, Boolean isActive);

    /**
     * Find all child categories of a parent category (including inactive)
     */
    List<Category> findByParentCategoryId(UUID parentCategoryId);

    /**
     * Check if category name already exists (for validation)
     */
    boolean existsByName(String name);

    /**
     * Check if category name exists excluding a specific category (for updates)
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c WHERE c.name = :name AND c.id != :excludeId")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("excludeId") UUID excludeId);
}
