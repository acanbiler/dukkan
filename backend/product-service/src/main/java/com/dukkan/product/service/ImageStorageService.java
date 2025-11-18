package com.dukkan.product.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * Service interface for storing and managing product images.
 * Abstraction allows switching between local filesystem, S3, or other storage implementations.
 */
public interface ImageStorageService {

    /**
     * Store an image file for a product.
     * Creates both original (resized to 1000px width) and thumbnail (300px width).
     *
     * @param file Uploaded image file
     * @param productId Product ID for organizing images
     * @return URL path to the stored image (e.g., "/api/v1/products/images/product-uuid-filename.jpg")
     * @throws IOException if file storage fails
     * @throws com.dukkan.product.exception.InvalidFileException if file validation fails
     */
    String storeImage(MultipartFile file, UUID productId) throws IOException;

    /**
     * Delete an image file by its filename.
     * Removes both original and thumbnail versions.
     *
     * @param imageUrl URL path of the image to delete
     * @throws IOException if file deletion fails
     */
    void deleteImage(String imageUrl) throws IOException;

    /**
     * Get the full filesystem path for an image filename.
     *
     * @param filename Image filename
     * @return Full filesystem path
     */
    String getImagePath(String filename);

    /**
     * Validate if a file is an acceptable image format.
     *
     * @param file Uploaded file to validate
     * @return true if valid image format
     */
    boolean isValidImageFile(MultipartFile file);
}
