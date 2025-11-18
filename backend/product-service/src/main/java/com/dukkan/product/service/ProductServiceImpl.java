package com.dukkan.product.service;

import com.dukkan.product.dto.ProductDTO;
import com.dukkan.product.exception.DuplicateResourceException;
import com.dukkan.product.exception.InvalidOperationException;
import com.dukkan.product.exception.ResourceNotFoundException;
import com.dukkan.product.mapper.ProductMapper;
import com.dukkan.product.model.Product;
import com.dukkan.product.repository.CategoryRepository;
import com.dukkan.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of ProductService.
 * Handles product business logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final ImageStorageService imageStorageService;

    @Override
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.debug("Creating new product: {}", productDTO.getName());

        // Validate SKU uniqueness
        if (productRepository.existsBySku(productDTO.getSku())) {
            throw new DuplicateResourceException("Product", "sku", productDTO.getSku());
        }

        // Validate category exists
        categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", productDTO.getCategoryId()));

        Product product = productMapper.toEntity(productDTO);
        Product savedProduct = productRepository.save(product);

        log.info("Created product with id: {} and SKU: {}", savedProduct.getId(), savedProduct.getSku());
        return productMapper.toDTO(savedProduct);
    }

    @Override
    public ProductDTO getProductById(UUID id) {
        log.debug("Fetching product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        return productMapper.toDTO(product);
    }

    @Override
    public ProductDTO getProductBySku(String sku) {
        log.debug("Fetching product with SKU: {}", sku);

        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "sku", sku));

        return productMapper.toDTO(product);
    }

    @Override
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        log.debug("Fetching all products, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        return productRepository.findAll(pageable)
                .map(productMapper::toDTO);
    }

    @Override
    public Page<ProductDTO> getActiveProducts(Pageable pageable) {
        log.debug("Fetching active products, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        return productRepository.findByIsActive(true, pageable)
                .map(productMapper::toDTO);
    }

    @Override
    public Page<ProductDTO> getProductsByCategory(UUID categoryId, Pageable pageable) {
        log.debug("Fetching products for category: {}", categoryId);

        // Verify category exists
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId));

        return productRepository.findByCategoryIdAndIsActive(categoryId, true, pageable)
                .map(productMapper::toDTO);
    }

    @Override
    public Page<ProductDTO> searchProducts(String searchTerm, Pageable pageable) {
        log.debug("Searching products with term: {}", searchTerm);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getActiveProducts(pageable);
        }

        return productRepository.searchByNameOrDescription(searchTerm.trim(), pageable)
                .map(productMapper::toDTO);
    }

    @Override
    public List<ProductDTO> getLowStockProducts(Integer threshold) {
        log.debug("Fetching low stock products with threshold: {}", threshold);

        int stockThreshold = threshold != null ? threshold : 10;

        return productRepository.findLowStockProducts(stockThreshold)
                .stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getOutOfStockProducts() {
        log.debug("Fetching out of stock products");

        return productRepository.findOutOfStockProducts()
                .stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(UUID id, ProductDTO productDTO) {
        log.debug("Updating product with id: {}", id);

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        // Validate SKU uniqueness (excluding current product)
        if (!existingProduct.getSku().equals(productDTO.getSku()) &&
                productRepository.existsBySkuAndIdNot(productDTO.getSku(), id)) {
            throw new DuplicateResourceException("Product", "sku", productDTO.getSku());
        }

        // Validate category exists if changed
        if (!existingProduct.getCategoryId().equals(productDTO.getCategoryId())) {
            categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", productDTO.getCategoryId()));
        }

        productMapper.updateEntityFromDTO(productDTO, existingProduct);
        Product updatedProduct = productRepository.save(existingProduct);

        log.info("Updated product with id: {}", id);
        return productMapper.toDTO(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(UUID id) {
        log.debug("Deleting product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        productRepository.delete(product);
        log.info("Deleted product with id: {}", id);
    }

    @Override
    @Transactional
    public ProductDTO updateStock(UUID id, Integer quantity) {
        log.debug("Updating stock for product: {} to quantity: {}", id, quantity);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (quantity < 0) {
            throw new InvalidOperationException("Stock quantity cannot be negative");
        }

        product.setStockQuantity(quantity);
        Product updatedProduct = productRepository.save(product);

        log.info("Updated stock for product: {} to {}", id, quantity);
        return productMapper.toDTO(updatedProduct);
    }

    @Override
    @Transactional
    public ProductDTO reduceStock(UUID id, Integer quantity) {
        log.debug("Reducing stock for product: {} by quantity: {}", id, quantity);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        try {
            product.reduceStock(quantity);
            Product updatedProduct = productRepository.save(product);

            log.info("Reduced stock for product: {} by {}", id, quantity);
            return productMapper.toDTO(updatedProduct);
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException(e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public ProductDTO increaseStock(UUID id, Integer quantity) {
        log.debug("Increasing stock for product: {} by quantity: {}", id, quantity);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        try {
            product.increaseStock(quantity);
            Product updatedProduct = productRepository.save(product);

            log.info("Increased stock for product: {} by {}", id, quantity);
            return productMapper.toDTO(updatedProduct);
        } catch (IllegalArgumentException e) {
            throw new InvalidOperationException(e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public ProductDTO activateProduct(UUID id) {
        log.debug("Activating product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.activate();
        Product savedProduct = productRepository.save(product);

        log.info("Activated product with id: {}", id);
        return productMapper.toDTO(savedProduct);
    }

    @Override
    @Transactional
    public ProductDTO deactivateProduct(UUID id) {
        log.debug("Deactivating product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        product.deactivate();
        Product savedProduct = productRepository.save(product);

        log.info("Deactivated product with id: {}", id);
        return productMapper.toDTO(savedProduct);
    }

    @Override
    @Transactional
    public ProductDTO uploadProductImage(UUID productId, MultipartFile file) throws IOException {
        log.debug("Uploading image for product: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // Check image limit (max 5 images)
        if (product.getImageUrls() != null && product.getImageUrls().size() >= 5) {
            throw new InvalidOperationException("Product already has maximum number of images (5)");
        }

        // Store image and get URL
        String imageUrl = imageStorageService.storeImage(file, productId);

        // Add image URL to product using domain logic
        product.addImageUrl(imageUrl);
        Product updatedProduct = productRepository.save(product);

        log.info("Uploaded image for product: {} (URL: {})", productId, imageUrl);
        return productMapper.toDTO(updatedProduct);
    }

    @Override
    @Transactional
    public ProductDTO removeProductImage(UUID productId, String imageUrl) throws IOException {
        log.debug("Removing image from product: {} (URL: {})", productId, imageUrl);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // Check if image exists in product
        if (product.getImageUrls() == null || !product.getImageUrls().contains(imageUrl)) {
            throw new ResourceNotFoundException("Image", "url", imageUrl);
        }

        // Remove image URL from product using domain logic
        product.removeImageUrl(imageUrl);
        Product updatedProduct = productRepository.save(product);

        // Delete physical file
        imageStorageService.deleteImage(imageUrl);

        log.info("Removed image from product: {} (URL: {})", productId, imageUrl);
        return productMapper.toDTO(updatedProduct);
    }
}
