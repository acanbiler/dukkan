import { apiClient, extractData } from './api';
import { ApiResponse } from '@/types/api';
import { Category, CreateCategoryRequest, UpdateCategoryRequest } from '@/types/category';

/**
 * Category service for API calls
 */
export const categoryService = {
  /**
   * Get all categories
   */
  getAll: async (active?: boolean): Promise<Category[]> => {
    const response = await apiClient.get<ApiResponse<Category[]>>('/categories', {
      params: active !== undefined ? { active } : {},
    });
    return extractData(response.data);
  },

  /**
   * Get category by ID
   */
  getById: async (id: string): Promise<Category> => {
    const response = await apiClient.get<ApiResponse<Category>>(`/categories/${id}`);
    return extractData(response.data);
  },

  /**
   * Get root categories
   */
  getRootCategories: async (): Promise<Category[]> => {
    const response = await apiClient.get<ApiResponse<Category[]>>('/categories/root');
    return extractData(response.data);
  },

  /**
   * Get child categories
   */
  getChildCategories: async (parentId: string): Promise<Category[]> => {
    const response = await apiClient.get<ApiResponse<Category[]>>(`/categories/${parentId}/children`);
    return extractData(response.data);
  },

  /**
   * Create new category
   */
  create: async (category: CreateCategoryRequest): Promise<Category> => {
    const response = await apiClient.post<ApiResponse<Category>>('/categories', category);
    return extractData(response.data);
  },

  /**
   * Update category
   */
  update: async (id: string, category: UpdateCategoryRequest): Promise<Category> => {
    const response = await apiClient.put<ApiResponse<Category>>(`/categories/${id}`, category);
    return extractData(response.data);
  },

  /**
   * Delete category
   */
  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/categories/${id}`);
  },

  /**
   * Activate category
   */
  activate: async (id: string): Promise<Category> => {
    const response = await apiClient.patch<ApiResponse<Category>>(`/categories/${id}/activate`);
    return extractData(response.data);
  },

  /**
   * Deactivate category
   */
  deactivate: async (id: string): Promise<Category> => {
    const response = await apiClient.patch<ApiResponse<Category>>(`/categories/${id}/deactivate`);
    return extractData(response.data);
  },
};
