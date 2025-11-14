package com.dukkan.product.controller;

import com.dukkan.product.dto.ApiResponse;
import com.dukkan.product.dto.CategoryDTO;
import com.dukkan.product.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for Category operations.
 * Handles all category-related API endpoints.
 */
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categories", description = "Category management APIs")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Create a new category", description = "Create a new product category")
    public ResponseEntity<ApiResponse<CategoryDTO>> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        log.info("REST request to create category: {}", categoryDTO.getName());
        CategoryDTO created = categoryService.createCategory(categoryDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Category created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieve a category by its ID")
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategoryById(@PathVariable UUID id) {
        log.info("REST request to get category: {}", id);
        CategoryDTO category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(category));
    }

    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieve all categories")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getAllCategories(
            @RequestParam(required = false) Boolean active) {
        log.info("REST request to get all categories, active: {}", active);

        List<CategoryDTO> categories;
        if (active != null && active) {
            categories = categoryService.getActiveCategories();
        } else {
            categories = categoryService.getAllCategories();
        }

        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/root")
    @Operation(summary = "Get root categories", description = "Retrieve all root categories (categories with no parent)")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getRootCategories() {
        log.info("REST request to get root categories");
        List<CategoryDTO> categories = categoryService.getRootCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/{id}/children")
    @Operation(summary = "Get child categories", description = "Retrieve all child categories of a parent category")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> getChildCategories(@PathVariable UUID id) {
        log.info("REST request to get child categories for parent: {}", id);
        List<CategoryDTO> categories = categoryService.getChildCategories(id);
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category", description = "Update an existing category")
    public ResponseEntity<ApiResponse<CategoryDTO>> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryDTO categoryDTO) {
        log.info("REST request to update category: {}", id);
        CategoryDTO updated = categoryService.updateCategory(id, categoryDTO);
        return ResponseEntity.ok(ApiResponse.success(updated, "Category updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category", description = "Delete a category by ID")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable UUID id) {
        log.info("REST request to delete category: {}", id);
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Category deleted successfully"));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate category", description = "Activate a category")
    public ResponseEntity<ApiResponse<CategoryDTO>> activateCategory(@PathVariable UUID id) {
        log.info("REST request to activate category: {}", id);
        CategoryDTO activated = categoryService.activateCategory(id);
        return ResponseEntity.ok(ApiResponse.success(activated, "Category activated successfully"));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate category", description = "Deactivate a category")
    public ResponseEntity<ApiResponse<CategoryDTO>> deactivateCategory(@PathVariable UUID id) {
        log.info("REST request to deactivate category: {}", id);
        CategoryDTO deactivated = categoryService.deactivateCategory(id);
        return ResponseEntity.ok(ApiResponse.success(deactivated, "Category deactivated successfully"));
    }
}
