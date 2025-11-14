import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { notifications } from '@mantine/notifications';
import { Cart, CartItem, CartContextType } from '@/types/cart';
import { Product } from '@/types/product';

const CART_STORAGE_KEY = 'dukkan-cart';

/**
 * Create Cart Context
 */
const CartContext = createContext<CartContextType | undefined>(undefined);

/**
 * Calculate cart totals
 */
const calculateCart = (items: CartItem[]): Cart => {
  const totalItems = items.reduce((sum, item) => sum + item.quantity, 0);
  const totalPrice = items.reduce((sum, item) => sum + item.product.price * item.quantity, 0);

  return {
    items,
    totalItems,
    totalPrice,
  };
};

/**
 * Load cart from localStorage
 */
const loadCartFromStorage = (): CartItem[] => {
  try {
    const stored = localStorage.getItem(CART_STORAGE_KEY);
    if (stored) {
      return JSON.parse(stored);
    }
  } catch (error) {
    console.error('Failed to load cart from storage:', error);
  }
  return [];
};

/**
 * Save cart to localStorage
 */
const saveCartToStorage = (items: CartItem[]) => {
  try {
    localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(items));
  } catch (error) {
    console.error('Failed to save cart to storage:', error);
  }
};

/**
 * Cart Provider Component
 */
export const CartProvider = ({ children }: { children: ReactNode }) => {
  const [items, setItems] = useState<CartItem[]>(() => loadCartFromStorage());
  const [cart, setCart] = useState<Cart>(() => calculateCart(loadCartFromStorage()));

  // Update cart whenever items change
  useEffect(() => {
    const newCart = calculateCart(items);
    setCart(newCart);
    saveCartToStorage(items);
  }, [items]);

  /**
   * Add product to cart
   */
  const addToCart = (product: Product, quantity = 1) => {
    if (!product.inStock) {
      notifications.show({
        title: 'Out of Stock',
        message: `${product.name} is currently out of stock`,
        color: 'red',
      });
      return;
    }

    setItems((prevItems) => {
      const existingItem = prevItems.find((item) => item.product.id === product.id);

      if (existingItem) {
        // Check if adding quantity exceeds stock
        const newQuantity = existingItem.quantity + quantity;
        if (newQuantity > product.stockQuantity) {
          notifications.show({
            title: 'Insufficient Stock',
            message: `Only ${product.stockQuantity} items available`,
            color: 'orange',
          });
          return prevItems;
        }

        // Update quantity
        notifications.show({
          title: 'Cart Updated',
          message: `${product.name} quantity updated`,
          color: 'blue',
        });

        return prevItems.map((item) =>
          item.product.id === product.id ? { ...item, quantity: newQuantity } : item
        );
      } else {
        // Check stock before adding
        if (quantity > product.stockQuantity) {
          notifications.show({
            title: 'Insufficient Stock',
            message: `Only ${product.stockQuantity} items available`,
            color: 'orange',
          });
          return prevItems;
        }

        // Add new item
        notifications.show({
          title: 'Added to Cart',
          message: `${product.name} added to cart`,
          color: 'green',
        });

        return [...prevItems, { product, quantity }];
      }
    });
  };

  /**
   * Remove product from cart
   */
  const removeFromCart = (productId: string) => {
    setItems((prevItems) => {
      const item = prevItems.find((item) => item.product.id === productId);
      if (item) {
        notifications.show({
          title: 'Removed from Cart',
          message: `${item.product.name} removed from cart`,
          color: 'blue',
        });
      }
      return prevItems.filter((item) => item.product.id !== productId);
    });
  };

  /**
   * Update item quantity
   */
  const updateQuantity = (productId: string, quantity: number) => {
    if (quantity <= 0) {
      removeFromCart(productId);
      return;
    }

    setItems((prevItems) =>
      prevItems.map((item) => {
        if (item.product.id === productId) {
          // Check stock
          if (quantity > item.product.stockQuantity) {
            notifications.show({
              title: 'Insufficient Stock',
              message: `Only ${item.product.stockQuantity} items available`,
              color: 'orange',
            });
            return item;
          }
          return { ...item, quantity };
        }
        return item;
      })
    );
  };

  /**
   * Clear entire cart
   */
  const clearCart = () => {
    setItems([]);
    notifications.show({
      title: 'Cart Cleared',
      message: 'All items removed from cart',
      color: 'blue',
    });
  };

  /**
   * Check if product is in cart
   */
  const isInCart = (productId: string): boolean => {
    return items.some((item) => item.product.id === productId);
  };

  /**
   * Get quantity of product in cart
   */
  const getItemQuantity = (productId: string): number => {
    const item = items.find((item) => item.product.id === productId);
    return item ? item.quantity : 0;
  };

  const value: CartContextType = {
    cart,
    addToCart,
    removeFromCart,
    updateQuantity,
    clearCart,
    isInCart,
    getItemQuantity,
  };

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
};

/**
 * Custom hook to use cart context
 */
export const useCart = (): CartContextType => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart must be used within CartProvider');
  }
  return context;
};
