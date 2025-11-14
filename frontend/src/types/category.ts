/**
 * Category type
 */
export interface Category {
  id: string;
  name: string;
  description: string;
  parentCategoryId: string | null;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

/**
 * Create category request
 */
export interface CreateCategoryRequest {
  name: string;
  description?: string;
  parentCategoryId?: string | null;
}

/**
 * Update category request
 */
export interface UpdateCategoryRequest extends CreateCategoryRequest {
  isActive?: boolean;
}
