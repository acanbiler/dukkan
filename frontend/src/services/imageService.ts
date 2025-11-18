import api from './api';
import { Product } from '../types/product';

/**
 * Service for product image management.
 */

/**
 * Upload an image for a product
 * @param productId Product ID
 * @param file Image file to upload
 * @returns Updated product with new image
 */
export const uploadImage = async (productId: string, file: File): Promise<Product> => {
  const formData = new FormData();
  formData.append('file', file);

  const response = await api.post<{ data: Product }>(
    `/products/${productId}/images`,
    formData,
    {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    }
  );

  return response.data.data;
};

/**
 * Delete an image from a product
 * @param productId Product ID
 * @param imageUrl Image URL to delete
 * @returns Updated product without the deleted image
 */
export const deleteImage = async (productId: string, imageUrl: string): Promise<Product> => {
  const response = await api.delete<{ data: Product }>(
    `/products/${productId}/images`,
    {
      params: { url: imageUrl },
    }
  );

  return response.data.data;
};

/**
 * Get image URL for display
 * @param imageUrl Relative image URL from backend (e.g., "/api/v1/products/images/filename.jpg")
 * @returns Full image URL
 */
export const getImageUrl = (imageUrl: string | undefined): string => {
  if (!imageUrl) {
    return 'https://via.placeholder.com/400x400?text=No+Image';
  }

  // If already a full URL, return as is
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
    return imageUrl;
  }

  // Build full URL using API base URL
  const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

  // Remove /api/v1 prefix from imageUrl if present (backend returns it)
  const cleanPath = imageUrl.replace('/api/v1', '');

  return `${baseURL}${cleanPath}`;
};

/**
 * Get thumbnail URL for display in listings
 * @param imageUrl Relative image URL from backend
 * @returns Full thumbnail URL
 */
export const getThumbnailUrl = (imageUrl: string | undefined): string => {
  if (!imageUrl) {
    return 'https://via.placeholder.com/300x300?text=No+Image';
  }

  // Extract filename from URL
  const filename = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
  const thumbnailFilename = 'thumb_' + filename;

  const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';
  return `${baseURL}/products/images/${thumbnailFilename}`;
};
