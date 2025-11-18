import { describe, it, expect, vi, beforeEach } from 'vitest';
import { categoryService } from './categoryService';
import { apiClient } from './api';
import type { Category, CreateCategoryRequest, UpdateCategoryRequest } from '@/types/category';

// Mock api client
vi.mock('./api', () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
    patch: vi.fn(),
  },
  extractData: (data: any) => data.data,
}));

const mockCategory: Category = {
  categoryId: '123',
  name: 'Test Category',
  description: 'Test Description',
  active: true,
  productCount: 5,
  createdAt: '2025-01-01T00:00:00Z',
  updatedAt: '2025-01-01T00:00:00Z',
};

const mockRootCategory: Category = {
  ...mockCategory,
  categoryId: 'root-1',
  name: 'Root Category',
  parentCategoryId: undefined,
};

const mockChildCategory: Category = {
  ...mockCategory,
  categoryId: 'child-1',
  name: 'Child Category',
  parentCategoryId: 'root-1',
};

describe('categoryService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('getAll', () => {
    it('should fetch all categories without filter', async () => {
      const mockResponse = {
        data: {
          success: true,
          data: [mockCategory],
          message: 'Categories retrieved',
        },
      };

      vi.mocked(apiClient.get).mockResolvedValue(mockResponse);

      const result = await categoryService.getAll();

      expect(result).toEqual([mockCategory]);
      expect(apiClient.get).toHaveBeenCalledWith('/categories', {
        params: {},
      });
    });

    it('should fetch active categories only', async () => {
      const mockResponse = {
        data: {
          success: true,
          data: [mockCategory],
        },
      };

      vi.mocked(apiClient.get).mockResolvedValue(mockResponse);

      const result = await categoryService.getAll(true);

      expect(result).toEqual([mockCategory]);
      expect(apiClient.get).toHaveBeenCalledWith('/categories', {
        params: { active: true },
      });
    });

    it('should fetch inactive categories', async () => {
      const inactiveCategory = { ...mockCategory, active: false };
      const mockResponse = {
        data: {
          success: true,
          data: [inactiveCategory],
        },
      };

      vi.mocked(apiClient.get).mockResolvedValue(mockResponse);

      const result = await categoryService.getAll(false);

      expect(result).toEqual([inactiveCategory]);
      expect(apiClient.get).toHaveBeenCalledWith('/categories', {
        params: { active: false },
      });
    });

    it('should handle get all categories error', async () => {
      const error = new Error('Failed to fetch categories');
      vi.mocked(apiClient.get).mockRejectedValue(error);

      await expect(categoryService.getAll()).rejects.toThrow('Failed to fetch categories');
    });
  });

  describe('getById', () => {
    it('should fetch category by ID', async () => {
      const mockResponse = {
        data: {
          success: true,
          data: mockCategory,
          message: 'Category retrieved',
        },
      };

      vi.mocked(apiClient.get).mockResolvedValue(mockResponse);

      const result = await categoryService.getById('123');

      expect(result).toEqual(mockCategory);
      expect(apiClient.get).toHaveBeenCalledWith('/categories/123');
    });

    it('should handle get by ID error', async () => {
      const error = new Error('Category not found');
      vi.mocked(apiClient.get).mockRejectedValue(error);

      await expect(categoryService.getById('123')).rejects.toThrow('Category not found');
    });
  });

  describe('getRootCategories', () => {
    it('should fetch root categories', async () => {
      const mockResponse = {
        data: {
          success: true,
          data: [mockRootCategory],
        },
      };

      vi.mocked(apiClient.get).mockResolvedValue(mockResponse);

      const result = await categoryService.getRootCategories();

      expect(result).toEqual([mockRootCategory]);
      expect(apiClient.get).toHaveBeenCalledWith('/categories/root');
    });

    it('should handle get root categories error', async () => {
      const error = new Error('Failed to fetch root categories');
      vi.mocked(apiClient.get).mockRejectedValue(error);

      await expect(categoryService.getRootCategories()).rejects.toThrow(
        'Failed to fetch root categories'
      );
    });
  });

  describe('getChildCategories', () => {
    it('should fetch child categories', async () => {
      const mockResponse = {
        data: {
          success: true,
          data: [mockChildCategory],
        },
      };

      vi.mocked(apiClient.get).mockResolvedValue(mockResponse);

      const result = await categoryService.getChildCategories('root-1');

      expect(result).toEqual([mockChildCategory]);
      expect(apiClient.get).toHaveBeenCalledWith('/categories/root-1/children');
    });

    it('should handle get child categories error', async () => {
      const error = new Error('Parent category not found');
      vi.mocked(apiClient.get).mockRejectedValue(error);

      await expect(categoryService.getChildCategories('root-1')).rejects.toThrow(
        'Parent category not found'
      );
    });
  });

  describe('create', () => {
    it('should create new category', async () => {
      const createRequest: CreateCategoryRequest = {
        name: 'New Category',
        description: 'New Description',
      };

      const newCategory = {
        ...mockCategory,
        ...createRequest,
      };

      const mockResponse = {
        data: {
          success: true,
          data: newCategory,
          message: 'Category created',
        },
      };

      vi.mocked(apiClient.post).mockResolvedValue(mockResponse);

      const result = await categoryService.create(createRequest);

      expect(result).toEqual(newCategory);
      expect(apiClient.post).toHaveBeenCalledWith('/categories', createRequest);
    });

    it('should create category with parent', async () => {
      const createRequest: CreateCategoryRequest = {
        name: 'Child Category',
        description: 'Child Description',
        parentCategoryId: 'root-1',
      };

      const newCategory = {
        ...mockCategory,
        ...createRequest,
      };

      const mockResponse = {
        data: {
          success: true,
          data: newCategory,
        },
      };

      vi.mocked(apiClient.post).mockResolvedValue(mockResponse);

      const result = await categoryService.create(createRequest);

      expect(result).toEqual(newCategory);
    });

    it('should handle create category error', async () => {
      const createRequest: CreateCategoryRequest = {
        name: 'Duplicate Category',
        description: 'Description',
      };

      const error = new Error('Category name already exists');
      vi.mocked(apiClient.post).mockRejectedValue(error);

      await expect(categoryService.create(createRequest)).rejects.toThrow(
        'Category name already exists'
      );
    });
  });

  describe('update', () => {
    it('should update category', async () => {
      const updateRequest: UpdateCategoryRequest = {
        name: 'Updated Category',
        description: 'Updated Description',
      };

      const updatedCategory = {
        ...mockCategory,
        ...updateRequest,
      };

      const mockResponse = {
        data: {
          success: true,
          data: updatedCategory,
          message: 'Category updated',
        },
      };

      vi.mocked(apiClient.put).mockResolvedValue(mockResponse);

      const result = await categoryService.update('123', updateRequest);

      expect(result).toEqual(updatedCategory);
      expect(apiClient.put).toHaveBeenCalledWith('/categories/123', updateRequest);
    });

    it('should handle update category error', async () => {
      const updateRequest: UpdateCategoryRequest = {
        name: 'Updated Category',
      };

      const error = new Error('Category not found');
      vi.mocked(apiClient.put).mockRejectedValue(error);

      await expect(categoryService.update('123', updateRequest)).rejects.toThrow(
        'Category not found'
      );
    });
  });

  describe('delete', () => {
    it('should delete category', async () => {
      vi.mocked(apiClient.delete).mockResolvedValue(undefined);

      await categoryService.delete('123');

      expect(apiClient.delete).toHaveBeenCalledWith('/categories/123');
    });

    it('should handle delete category error', async () => {
      const error = new Error('Cannot delete category with products');
      vi.mocked(apiClient.delete).mockRejectedValue(error);

      await expect(categoryService.delete('123')).rejects.toThrow(
        'Cannot delete category with products'
      );
    });
  });

  describe('activate', () => {
    it('should activate category', async () => {
      const activatedCategory = {
        ...mockCategory,
        active: true,
      };

      const mockResponse = {
        data: {
          success: true,
          data: activatedCategory,
          message: 'Category activated',
        },
      };

      vi.mocked(apiClient.patch).mockResolvedValue(mockResponse);

      const result = await categoryService.activate('123');

      expect(result).toEqual(activatedCategory);
      expect(result.active).toBe(true);
      expect(apiClient.patch).toHaveBeenCalledWith('/categories/123/activate');
    });

    it('should handle activate category error', async () => {
      const error = new Error('Category not found');
      vi.mocked(apiClient.patch).mockRejectedValue(error);

      await expect(categoryService.activate('123')).rejects.toThrow('Category not found');
    });
  });

  describe('deactivate', () => {
    it('should deactivate category', async () => {
      const deactivatedCategory = {
        ...mockCategory,
        active: false,
      };

      const mockResponse = {
        data: {
          success: true,
          data: deactivatedCategory,
          message: 'Category deactivated',
        },
      };

      vi.mocked(apiClient.patch).mockResolvedValue(mockResponse);

      const result = await categoryService.deactivate('123');

      expect(result).toEqual(deactivatedCategory);
      expect(result.active).toBe(false);
      expect(apiClient.patch).toHaveBeenCalledWith('/categories/123/deactivate');
    });

    it('should handle deactivate category error', async () => {
      const error = new Error('Category not found');
      vi.mocked(apiClient.patch).mockRejectedValue(error);

      await expect(categoryService.deactivate('123')).rejects.toThrow('Category not found');
    });
  });
});
