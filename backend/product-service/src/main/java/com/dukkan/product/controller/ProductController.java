package com.dukkan.product.controller;

import com.dukkan.product.dto.ApiResponse;
import com.dukkan.product.dto.ProductDTO;
import com.dukkan.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for Product operations.
 * Handles all product-related API endpoints.
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Products", description = "Product management APIs")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Create a new product", description = "Create a new product in the catalog")
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        log.info("REST request to create product: {}", productDTO.getName());
        ProductDTO created = productService.createProduct(productDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Product created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieve a product by its ID")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable UUID id) {
        log.info("REST request to get product: {}", id);
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @GetMapping("/sku/{sku}")
    @Operation(summary = "Get product by SKU", description = "Retrieve a product by its SKU")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductBySku(@PathVariable String sku) {
        log.info("REST request to get product by SKU: {}", sku);
        ProductDTO product = productService.getProductBySku(sku);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieve all products with pagination")
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(required = false) Boolean active) {
        log.info("REST request to get all products, page: {}, size: {}, active: {}", page, size, active);

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<ProductDTO> products;
        if (active != null && active) {
            products = productService.getActiveProducts(pageable);
        } else {
            products = productService.getAllProducts(pageable);
        }

        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category", description = "Retrieve all products in a specific category")
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> getProductsByCategory(
            @PathVariable UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy) {
        log.info("REST request to get products by category: {}", categoryId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<ProductDTO> products = productService.getProductsByCategory(categoryId, pageable);

        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Search products by name or description")
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("REST request to search products with query: {}", query);

        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        Page<ProductDTO> products = productService.searchProducts(query, pageable);

        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock products", description = "Retrieve products with low stock")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getLowStockProducts(
            @RequestParam(defaultValue = "10") Integer threshold) {
        log.info("REST request to get low stock products with threshold: {}", threshold);
        List<ProductDTO> products = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/out-of-stock")
    @Operation(summary = "Get out of stock products", description = "Retrieve products that are out of stock")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> getOutOfStockProducts() {
        log.info("REST request to get out of stock products");
        List<ProductDTO> products = productService.getOutOfStockProducts();
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Update an existing product")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductDTO productDTO) {
        log.info("REST request to update product: {}", id);
        ProductDTO updated = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(ApiResponse.success(updated, "Product updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Delete a product by ID")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable UUID id) {
        log.info("REST request to delete product: {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
    }

    @PatchMapping("/{id}/stock")
    @Operation(summary = "Update product stock", description = "Update the stock quantity of a product")
    public ResponseEntity<ApiResponse<ProductDTO>> updateStock(
            @PathVariable UUID id,
            @RequestParam Integer quantity) {
        log.info("REST request to update stock for product: {} to quantity: {}", id, quantity);
        ProductDTO updated = productService.updateStock(id, quantity);
        return ResponseEntity.ok(ApiResponse.success(updated, "Stock updated successfully"));
    }

    @PatchMapping("/{id}/stock/reduce")
    @Operation(summary = "Reduce product stock", description = "Reduce the stock quantity of a product")
    public ResponseEntity<ApiResponse<ProductDTO>> reduceStock(
            @PathVariable UUID id,
            @RequestParam Integer quantity) {
        log.info("REST request to reduce stock for product: {} by quantity: {}", id, quantity);
        ProductDTO updated = productService.reduceStock(id, quantity);
        return ResponseEntity.ok(ApiResponse.success(updated, "Stock reduced successfully"));
    }

    @PatchMapping("/{id}/stock/increase")
    @Operation(summary = "Increase product stock", description = "Increase the stock quantity of a product")
    public ResponseEntity<ApiResponse<ProductDTO>> increaseStock(
            @PathVariable UUID id,
            @RequestParam Integer quantity) {
        log.info("REST request to increase stock for product: {} by quantity: {}", id, quantity);
        ProductDTO updated = productService.increaseStock(id, quantity);
        return ResponseEntity.ok(ApiResponse.success(updated, "Stock increased successfully"));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate product", description = "Activate a product")
    public ResponseEntity<ApiResponse<ProductDTO>> activateProduct(@PathVariable UUID id) {
        log.info("REST request to activate product: {}", id);
        ProductDTO activated = productService.activateProduct(id);
        return ResponseEntity.ok(ApiResponse.success(activated, "Product activated successfully"));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate product", description = "Deactivate a product")
    public ResponseEntity<ApiResponse<ProductDTO>> deactivateProduct(@PathVariable UUID id) {
        log.info("REST request to deactivate product: {}", id);
        ProductDTO deactivated = productService.deactivateProduct(id);
        return ResponseEntity.ok(ApiResponse.success(deactivated, "Product deactivated successfully"));
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload product image", description = "Upload an image file for a product (max 5 images, 5MB each, JPEG/PNG/WebP)")
    public ResponseEntity<ApiResponse<ProductDTO>> uploadProductImage(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) throws IOException {
        log.info("REST request to upload image for product: {} (file: {}, size: {} bytes)",
                id, file.getOriginalFilename(), file.getSize());
        ProductDTO updated = productService.uploadProductImage(id, file);
        return ResponseEntity.ok(ApiResponse.success(updated, "Image uploaded successfully"));
    }

    @DeleteMapping("/{id}/images")
    @Operation(summary = "Remove product image", description = "Remove an image from a product")
    public ResponseEntity<ApiResponse<ProductDTO>> removeProductImage(
            @PathVariable UUID id,
            @RequestParam("url") String imageUrl) throws IOException {
        log.info("REST request to remove image from product: {} (URL: {})", id, imageUrl);
        ProductDTO updated = productService.removeProductImage(id, imageUrl);
        return ResponseEntity.ok(ApiResponse.success(updated, "Image removed successfully"));
    }

    @GetMapping("/images/{filename}")
    @Operation(summary = "Get product image", description = "Retrieve a product image file")
    public ResponseEntity<Resource> getProductImage(@PathVariable String filename) throws IOException {
        log.info("REST request to get product image: {}", filename);

        // Get image path from storage directory
        String storageDirectory = "/var/dukkan/images/products";
        Path imagePath = Paths.get(storageDirectory, filename);

        if (!Files.exists(imagePath)) {
            log.warn("Image not found: {}", filename);
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(imagePath);

        // Determine content type
        String contentType = Files.probeContentType(imagePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}
