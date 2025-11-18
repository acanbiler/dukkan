package com.dukkan.product.service;

import com.dukkan.product.exception.InvalidFileException;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Local filesystem implementation of ImageStorageService.
 * Stores product images with automatic resizing and thumbnail generation.
 */
@Service
@Slf4j
public class ImageStorageServiceImpl implements ImageStorageService {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp");
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/webp"
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final int ORIGINAL_WIDTH = 1000;
    private static final int THUMBNAIL_WIDTH = 300;

    @Value("${dukkan.image.storage.directory:/var/dukkan/images/products}")
    private String storageDirectory;

    @Override
    public String storeImage(MultipartFile file, UUID productId) throws IOException {
        // Validate file
        validateImageFile(file);

        // Create storage directory if not exists
        Path storagePath = Paths.get(storageDirectory);
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
            log.info("Created image storage directory: {}", storageDirectory);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalFilename);
        String filename = String.format("%s-%s.%s", productId, UUID.randomUUID(), extension);

        // Save original (resized to 1000px width)
        Path originalPath = storagePath.resolve(filename);
        Thumbnails.of(file.getInputStream())
                .width(ORIGINAL_WIDTH)
                .toFile(originalPath.toFile());

        log.info("Stored product image: {} (size: {} bytes)", filename, file.getSize());

        // Generate thumbnail (300px width)
        String thumbnailFilename = "thumb_" + filename;
        Path thumbnailPath = storagePath.resolve(thumbnailFilename);
        Thumbnails.of(originalPath.toFile())
                .width(THUMBNAIL_WIDTH)
                .toFile(thumbnailPath.toFile());

        log.info("Generated thumbnail: {}", thumbnailFilename);

        // Return URL path (not filesystem path)
        return "/api/v1/products/images/" + filename;
    }

    @Override
    public void deleteImage(String imageUrl) throws IOException {
        // Extract filename from URL path
        String filename = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);

        // Delete original image
        Path imagePath = Paths.get(storageDirectory, filename);
        if (Files.exists(imagePath)) {
            Files.delete(imagePath);
            log.info("Deleted image: {}", filename);
        }

        // Delete thumbnail
        String thumbnailFilename = "thumb_" + filename;
        Path thumbnailPath = Paths.get(storageDirectory, thumbnailFilename);
        if (Files.exists(thumbnailPath)) {
            Files.delete(thumbnailPath);
            log.info("Deleted thumbnail: {}", thumbnailFilename);
        }
    }

    @Override
    public String getImagePath(String filename) {
        return Paths.get(storageDirectory, filename).toString();
    }

    @Override
    public boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            return false;
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            return false;
        }

        // Check file extension
        String filename = file.getOriginalFilename();
        if (filename == null) {
            return false;
        }

        String extension = FilenameUtils.getExtension(filename).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }

    /**
     * Validate image file or throw InvalidFileException
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is empty or null");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileException(
                    String.format("File size exceeds maximum allowed size of %d MB", MAX_FILE_SIZE / (1024 * 1024))
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidFileException(
                    String.format("Invalid file type: %s. Allowed types: %s", contentType, ALLOWED_CONTENT_TYPES)
            );
        }

        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new InvalidFileException("Filename is null");
        }

        String extension = FilenameUtils.getExtension(filename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidFileException(
                    String.format("Invalid file extension: %s. Allowed extensions: %s", extension, ALLOWED_EXTENSIONS)
            );
        }
    }
}
