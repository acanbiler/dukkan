import { apiClient, extractData } from './api';
import { ApiResponse, PageResponse } from '@/types/api';
import { Product, CreateProductRequest, UpdateProductRequest } from '@/types/product';

/**
 * Product service for API calls
 */
export const productService = {
  /**
   * Get all products (paginated)
   */
  getAll: async (page = 0, size = 20, active = true): Promise<PageResponse<Product>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<Product>>>('/products', {
      params: { page, size, active },
    });
    return extractData(response.data);
  },

  /**
   * Get product by ID
   */
  getById: async (id: string): Promise<Product> => {
    const response = await apiClient.get<ApiResponse<Product>>(`/products/${id}`);
    return extractData(response.data);
  },

  /**
   * Get product by SKU
   */
  getBySku: async (sku: string): Promise<Product> => {
    const response = await apiClient.get<ApiResponse<Product>>(`/products/sku/${sku}`);
    return extractData(response.data);
  },

  /**
   * Get products by category
   */
  getByCategory: async (categoryId: string, page = 0, size = 20): Promise<PageResponse<Product>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<Product>>>(
      `/products/category/${categoryId}`,
      { params: { page, size } }
    );
    return extractData(response.data);
  },

  /**
   * Search products
   */
  search: async (query: string, page = 0, size = 20): Promise<PageResponse<Product>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<Product>>>('/products/search', {
      params: { query, page, size },
    });
    return extractData(response.data);
  },

  /**
   * Create new product
   */
  create: async (product: CreateProductRequest): Promise<Product> => {
    const response = await apiClient.post<ApiResponse<Product>>('/products', product);
    return extractData(response.data);
  },

  /**
   * Update product
   */
  update: async (id: string, product: UpdateProductRequest): Promise<Product> => {
    const response = await apiClient.put<ApiResponse<Product>>(`/products/${id}`, product);
    return extractData(response.data);
  },

  /**
   * Delete product
   */
  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/products/${id}`);
  },

  /**
   * Update product stock
   */
  updateStock: async (id: string, quantity: number): Promise<Product> => {
    const response = await apiClient.patch<ApiResponse<Product>>(`/products/${id}/stock`, null, {
      params: { quantity },
    });
    return extractData(response.data);
  },

  /**
   * Activate product
   */
  activate: async (id: string): Promise<Product> => {
    const response = await apiClient.patch<ApiResponse<Product>>(`/products/${id}/activate`);
    return extractData(response.data);
  },

  /**
   * Deactivate product
   */
  deactivate: async (id: string): Promise<Product> => {
    const response = await apiClient.patch<ApiResponse<Product>>(`/products/${id}/deactivate`);
    return extractData(response.data);
  },
};
