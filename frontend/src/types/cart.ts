import { Product } from './product';

/**
 * Cart item type
 */
export interface CartItem {
  product: Product;
  quantity: number;
}

/**
 * Cart state type
 */
export interface Cart {
  items: CartItem[];
  totalItems: number;
  totalPrice: number;
}

/**
 * Cart context type
 */
export interface CartContextType {
  cart: Cart;
  addToCart: (product: Product, quantity?: number) => void;
  removeFromCart: (productId: string) => void;
  updateQuantity: (productId: string, quantity: number) => void;
  clearCart: () => void;
  isInCart: (productId: string) => boolean;
  getItemQuantity: (productId: string) => number;
}
