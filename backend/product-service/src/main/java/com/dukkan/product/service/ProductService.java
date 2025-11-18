package com.dukkan.product.service;

import com.dukkan.product.dto.ProductDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for Product business logic.
 * Following Interface Segregation and Dependency Inversion Principles.
 */
public interface ProductService {

    /**
     * Create a new product
     */
    ProductDTO createProduct(ProductDTO productDTO);

    /**
     * Get product by ID
     */
    ProductDTO getProductById(UUID id);

    /**
     * Get product by SKU
     */
    ProductDTO getProductBySku(String sku);

    /**
     * Get all products (paginated)
     */
    Page<ProductDTO> getAllProducts(Pageable pageable);

    /**
     * Get all active products (paginated)
     */
    Page<ProductDTO> getActiveProducts(Pageable pageable);

    /**
     * Get products by category (paginated)
     */
    Page<ProductDTO> getProductsByCategory(UUID categoryId, Pageable pageable);

    /**
     * Search products by name or description
     */
    Page<ProductDTO> searchProducts(String searchTerm, Pageable pageable);

    /**
     * Get low stock products
     */
    List<ProductDTO> getLowStockProducts(Integer threshold);

    /**
     * Get out of stock products
     */
    List<ProductDTO> getOutOfStockProducts();

    /**
     * Update an existing product
     */
    ProductDTO updateProduct(UUID id, ProductDTO productDTO);

    /**
     * Delete a product
     */
    void deleteProduct(UUID id);

    /**
     * Update product stock
     */
    ProductDTO updateStock(UUID id, Integer quantity);

    /**
     * Reduce product stock (for orders)
     */
    ProductDTO reduceStock(UUID id, Integer quantity);

    /**
     * Increase product stock (for restocking)
     */
    ProductDTO increaseStock(UUID id, Integer quantity);

    /**
     * Activate a product
     */
    ProductDTO activateProduct(UUID id);

    /**
     * Deactivate a product
     */
    ProductDTO deactivateProduct(UUID id);

    /**
     * Upload an image for a product
     *
     * @param productId Product ID
     * @param file Image file to upload
     * @return Updated product DTO with new image URL
     * @throws IOException if file upload fails
     */
    ProductDTO uploadProductImage(UUID productId, MultipartFile file) throws IOException;

    /**
     * Remove an image from a product
     *
     * @param productId Product ID
     * @param imageUrl Image URL to remove
     * @return Updated product DTO without the removed image
     * @throws IOException if file deletion fails
     */
    ProductDTO removeProductImage(UUID productId, String imageUrl) throws IOException;
}
