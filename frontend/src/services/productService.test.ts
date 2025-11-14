import { describe, it, expect, vi, beforeEach } from 'vitest';
import { productService } from './productService';
import { apiClient } from './api';

vi.mock('./api', () => ({
  apiClient: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
  extractData: (data: any) => data.data,
}));

describe('productService', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('getById', () => {
    it('should fetch product by id', async () => {
      const mockProduct = {
        id: '123',
        sku: 'TEST-SKU',
        name: 'Test Product',
        price: 99.99,
        stockQuantity: 10,
      };

      const mockResponse = {
        data: {
          success: true,
          data: mockProduct,
          message: 'Product retrieved',
        },
      };

      vi.mocked(apiClient.get).mockResolvedValue(mockResponse);

      const result = await productService.getById('123');

      expect(result).toEqual(mockProduct);
      expect(apiClient.get).toHaveBeenCalledWith('/products/123');
    });
  });

  describe('search', () => {
    it('should search products with query', async () => {
      const mockPageResponse = {
        content: [
          { id: '1', name: 'Product 1', price: 50 },
          { id: '2', name: 'Product 2', price: 75 },
        ],
        page: 0,
        size: 20,
        totalElements: 2,
        totalPages: 1,
      };

      const mockResponse = {
        data: {
          success: true,
          data: mockPageResponse,
        },
      };

      vi.mocked(apiClient.get).mockResolvedValue(mockResponse);

      const result = await productService.search('test', 0, 20);

      expect(result.content).toHaveLength(2);
      expect(apiClient.get).toHaveBeenCalledWith('/products/search', {
        params: { query: 'test', page: 0, size: 20 },
      });
    });
  });
});
