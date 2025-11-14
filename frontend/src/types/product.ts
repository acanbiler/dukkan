/**
 * Product type
 */
export interface Product {
  id: string;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  sku: string;
  categoryId: string;
  imageUrls: string[];
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
  inStock: boolean;
  lowStock: boolean;
}

/**
 * Create product request
 */
export interface CreateProductRequest {
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  sku: string;
  categoryId: string;
  imageUrls?: string[];
}

/**
 * Update product request
 */
export interface UpdateProductRequest extends CreateProductRequest {
  isActive?: boolean;
}
