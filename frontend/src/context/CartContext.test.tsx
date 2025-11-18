import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, renderHook, act, waitFor } from '@testing-library/react';
import { CartProvider, useCart } from './CartContext';
import { notifications } from '@mantine/notifications';
import type { Product } from '@/types/product';

// Mock Mantine notifications
vi.mock('@mantine/notifications', () => ({
  notifications: {
    show: vi.fn(),
  },
}));

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {};

  return {
    getItem: (key: string) => store[key] || null,
    setItem: (key: string, value: string) => {
      store[key] = value;
    },
    removeItem: (key: string) => {
      delete store[key];
    },
    clear: () => {
      store = {};
    },
  };
})();

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
  writable: true,
});

const mockProduct: Product = {
  id: '123',
  sku: 'TEST-SKU',
  name: 'Test Product',
  description: 'Test Description',
  price: 99.99,
  stockQuantity: 10,
  categoryId: 'cat-1',
  categoryName: 'Test Category',
  imageUrl: 'https://example.com/image.jpg',
  inStock: true,
  lowStock: false,
  active: true,
  createdAt: '2025-01-01T00:00:00Z',
  updatedAt: '2025-01-01T00:00:00Z',
};

describe('CartContext', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  describe('Provider initialization', () => {
    it('should initialize with empty cart', () => {
      const { result } = renderHook(() => useCart(), {
        wrapper: CartProvider,
      });

      expect(result.current.cart.items).toEqual([]);
      expect(result.current.cart.totalItems).toBe(0);
      expect(result.current.cart.totalPrice).toBe(0);
    });

    it('should load cart from localStorage on mount', () => {
      const savedCart = [
        {
          product: mockProduct,
          quantity: 2,
        },
      ];
      localStorage.setItem('dukkan-cart', JSON.stringify(savedCart));

      const { result } = renderHook(() => useCart(), {
        wrapper: CartProvider,
      });

      expect(result.current.cart.items).toHaveLength(1);
      expect(result.current.cart.totalItems).toBe(2);
      expect(result.current.cart.totalPrice).toBe(199.98);
    });

    it('should handle corrupted localStorage data gracefully', () => {
      localStorage.setItem('dukkan-cart', 'invalid-json');
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

      const { result } = renderHook(() => useCart(), {
        wrapper: CartProvider,
      });

      expect(result.current.cart.items).toEqual([]);
      expect(consoleSpy).toHaveBeenCalled();

      consoleSpy.mockRestore();
    });
  });

  describe('addToCart', () => {
    it('should add product to empty cart', () => {
      const { result } = renderHook(() => useCart(), {
        wrapper: CartProvider,
      });

      act(() => {
        result.current.addToCart(mockProduct, 2);
      });

      expect(result.current.cart.items).toHaveLength(1);
      expect(result.current.cart.items[0].product.id).toBe('123');
      expect(result.current.cart.items[0].quantity).toBe(2);
      expect(result.current.cart.totalItems).toBe(2);
      expect(result.current.cart.totalPrice).toBe(199.98);
      expect(notifications.show).toHaveBeenCalledWith({
        title: 'Added to Cart',
        message: 'Test Product added to cart',
        color: 'green',
      });
    });

    it('should increment quantity if product already in cart', () => {
      const { result } = renderHook(() => useCart(), {
        wrapper: CartProvider,
      });

      act(() => {
        result.current.addToCart(mockProduct, 2);
      });

      act(() => {
        result.current.addToCart(mockProduct, 3);
      });

      expect(result.current.cart.items).toHaveLength(1);
      expect(result.current.cart.items[0].quantity).toBe(5);
      expect(result.current.cart.totalItems).toBe(5);
      expect(notifications.show).toHaveBeenLastCalledWith({
        title: 'Cart Updated',
        message: 'Test Product quantity updated',
        color: 'blue',
      });
    });

    it('should not add out-of-stock product', () => {
      const outOfStockProduct = { ...mockProduct, inStock: false };
      const { result } = renderHook(() => useCart(), {
        wrapper: CartProvider,
      });

      act(() => {
        result.current.addToCart(outOfStockProduct);
      });

      expect(result.current.cart.items).toHaveLength(0);
      expect(notifications.show).toHaveBeenCalledWith({
        title: 'Out of Stock',
        message: 'Test Product is currently out of stock',
        color: 'red',
      });
    });

    it('should not exceed stock quantity when adding', () => {
      const { result } = renderHook(() => useCart(), {
        wrapper: CartProvider,
      });

      act(() => {
        result.current.addToCart(mockProduct, 15);
      });

      expect(result.current.cart.items).toHaveLength(0);
      expect(notifications.show).toHaveBeenCalledWith({
        title: 'Insufficient Stock',
        message: 'Only 10 items available',
        color: 'orange',
      });
    });

    it('should not exceed stock quantity when updating existing item', () => {
      const { result } = renderHook(() => useCart(), {
        wrapper: CartProvider,
      });

      act(() => {
        result.current.addToCart(mockProduct, 5);
      });

      act(() => {
        result.current.addToCart(mockProduct, 8);
      });

      expect(result.current.cart.items[0].quantity).toBe(5);
      expect(notifications.show).toHaveBeenLastCalledWith({
        title: 'Insufficient Stock',
        message: 'Only 10 items available',
        color: 'orange',
      });
    });

    it('should save cart to localStorage after adding', () => {
      const { result } = renderHook(() => useCart(), {
        wrapper: CartProvider,
      });

      act(() => {
        result.current.addToCart(mockProduct, 2);
      });

      const saved = localStorage.getItem('dukkan-cart');
      expect(saved).toBeTruthy();
      const parsed = JSON.parse(saved!);
      expect(parsed).toHaveLength(1);
      expect(parsed[0].quantity).toBe(2);
    });
  });

  describe('removeFromCart', () => {
    it('should remove product from cart', () => {
      const { result } = renderHook(() => useCart(), {
        wrapper: CartProvider,
      });

      act(() => {
        result.current.addToCart(mockProduct, 2);
      });

      act(() => {
        result.current.removeFromCart('123');
      });

      expect(result.current.cart.items).toHaveLength(0);
      expect(result.current.cart.totalItems).toBe(0);
      expect(result.current.cart.totalPrice).toBe(0);
      expect(notifications.show).toHaveBeenLastCalledWith({
        title: 'Removed from Cart',
        message: 'Test Product removed from cart',
        color: 'blue',
      });
    });

    it('should do nothing if product not in cart', () => {
      const { result } = renderHook(() => useCart(), {
        wrapper: CartProvider,
      });

      act(() => {
        result.current.removeFromCart('non-existent');
      });

      expect(result.current.cart.items).toHaveLength(0);
    });
  });

  describe('updateQuantity', () => {
    it('should update product quantity', () => {
      const { result } = renderHook(() => useCart(), {
        wrapper: CartProvider,
      });

      act(() => {
        result.current.addToCart(mockProduct, 2);
      });

      act(() => {
        result.current.updateQuantity('123', 5);
      });

      expect(result.current.cart.items[0].quantity).toBe(5);
      expect(result.current.cart.totalItems).toBe(5);
      expect(result.current.cart.totalPrice).toBe(499.95);
    });

    it('should remove product when quantity is 0', () => {
      const { result } = renderHook(() => useCart(), {
        wrapper: CartProvider,
      });

      act(() => {
        result.current.addToCart(mockProduct, 2);
      });

      act(() => {
        result.current.updateQuantity('123', 0);
      });

      expect(result.current.cart.items).toHaveLength(0);
    });

    it('should remove product when quantity is negative', () => {
      const { result } = renderHook(() => useCart(), {
        wrapper: CartProvider,
      });

      act(() => {
        result.current.addToCart(mockProduct, 2);
      });

      act(() => {
        result.current.updateQuantity('123', -1);
      });

      expect(result.current.cart.items).toHaveLength(0);
    });

    it('should not exceed stock quantity', () => {
      const { result } = renderHook(() => useCart(), {
        wrapper: CartProvider,
      });

      act(() => {
        result.current.addToCart(mockProduct, 2);
      });

      act(() => {
        result.current.updateQuantity('123', 15);
      });

      expect(result.current.cart.items[0].quantity).toBe(2);
      expect(notifications.show).toHaveBeenLastCalledWith({
        title: 'Insufficient Stock',
        message: 'Only 10 items available',
        color: 'orange',
      });
    });
  });

  describe('clearCart', () => {
    it('should clear all items from cart', () => {
      const { result } = renderHook(() => useCart(), {
        wrapper: CartProvider,
      });

      act(() => {
        result.current.addToCart(mockProduct, 2);
      });

      act(() => {
        result.current.clearCart();
      });

      expect(result.current.cart.items).toHaveLength(0);
      expect(result.current.cart.totalItems).toBe(0);
      expect(result.current.cart.totalPrice).toBe(0);
      expect(notifications.show).toHaveBeenLastCalledWith({
        title: 'Cart Cleared',
        message: 'All items removed from cart',
        color: 'blue',
      });
    });

    it('should save empty cart to localStorage', () => {
      const { result } = renderHook(() => useCart(), {
        wrapper: CartProvider,
      });

      act(() => {
        result.current.addToCart(mockProduct, 2);
      });

      act(() => {
        result.current.clearCart();
      });

      const saved = localStorage.getItem('dukkan-cart');
      expect(saved).toBe('[]');
    });
  });

  describe('isInCart', () => {
    it('should return true if product is in cart', () => {
      const { result } = renderHook(() => useCart(), {
        wrapper: CartProvider,
      });

      act(() => {
        result.current.addToCart(mockProduct, 2);
      });

      expect(result.current.isInCart('123')).toBe(true);
    });

    it('should return false if product is not in cart', () => {
      const { result } = renderHook(() => useCart(), {
        wrapper: CartProvider,
      });

      expect(result.current.isInCart('123')).toBe(false);
    });
  });

  describe('getItemQuantity', () => {
    it('should return quantity if product is in cart', () => {
      const { result } = renderHook(() => useCart(), {
        wrapper: CartProvider,
      });

      act(() => {
        result.current.addToCart(mockProduct, 5);
      });

      expect(result.current.getItemQuantity('123')).toBe(5);
    });

    it('should return 0 if product is not in cart', () => {
      const { result } = renderHook(() => useCart(), {
        wrapper: CartProvider,
      });

      expect(result.current.getItemQuantity('123')).toBe(0);
    });
  });

  describe('useCart hook', () => {
    it('should throw error when used outside provider', () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});

      expect(() => {
        renderHook(() => useCart());
      }).toThrow('useCart must be used within CartProvider');

      consoleSpy.mockRestore();
    });
  });

  describe('cart calculations', () => {
    it('should calculate totals correctly with multiple products', () => {
      const product2 = { ...mockProduct, id: '456', price: 49.99 };
      const { result } = renderHook(() => useCart(), {
        wrapper: CartProvider,
      });

      act(() => {
        result.current.addToCart(mockProduct, 2);
        result.current.addToCart(product2, 3);
      });

      expect(result.current.cart.totalItems).toBe(5);
      expect(result.current.cart.totalPrice).toBe(349.95); // 2*99.99 + 3*49.99
    });

    it('should update totals when removing items', () => {
      const product2 = { ...mockProduct, id: '456', price: 49.99 };
      const { result } = renderHook(() => useCart(), {
        wrapper: CartProvider,
      });

      act(() => {
        result.current.addToCart(mockProduct, 2);
        result.current.addToCart(product2, 3);
      });

      act(() => {
        result.current.removeFromCart('123');
      });

      expect(result.current.cart.totalItems).toBe(3);
      expect(result.current.cart.totalPrice).toBe(149.97); // 3*49.99
    });
  });
});
