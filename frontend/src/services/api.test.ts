import { describe, it, expect } from 'vitest';
import { extractData } from './api';
import type { ApiResponse } from '../types/api';

describe('api.ts', () => {
  describe('extractData', () => {
    it('should extract data from API response', () => {
      const apiResponse: ApiResponse<{ name: string }> = {
        success: true,
        data: { name: 'Test' },
        message: 'Success',
        timestamp: '2025-01-01T00:00:00Z',
      };

      const result = extractData(apiResponse);
      expect(result).toEqual({ name: 'Test' });
    });

    it('should extract array data from API response', () => {
      const apiResponse: ApiResponse<string[]> = {
        success: true,
        data: ['item1', 'item2', 'item3'],
        message: 'Success',
        timestamp: '2025-01-01T00:00:00Z',
      };

      const result = extractData(apiResponse);
      expect(result).toEqual(['item1', 'item2', 'item3']);
    });

    it('should extract null data from API response', () => {
      const apiResponse: ApiResponse<null> = {
        success: true,
        data: null,
        message: 'Success',
        timestamp: '2025-01-01T00:00:00Z',
      };

      const result = extractData(apiResponse);
      expect(result).toBeNull();
    });

    it('should extract complex nested data from API response', () => {
      const complexData = {
        user: {
          id: '123',
          name: 'John',
          settings: {
            theme: 'dark',
            notifications: true,
          },
        },
      };

      const apiResponse: ApiResponse<typeof complexData> = {
        success: true,
        data: complexData,
        message: 'Success',
        timestamp: '2025-01-01T00:00:00Z',
      };

      const result = extractData(apiResponse);
      expect(result).toEqual(complexData);
      expect(result.user.settings.theme).toBe('dark');
    });
  });
});
