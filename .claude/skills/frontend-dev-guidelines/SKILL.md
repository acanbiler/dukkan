---
name: frontend-dev-guidelines
description: React 19 and TypeScript 5.7 frontend development for Dukkan. Use when creating or modifying React components, pages, forms, modals, dialogs, custom hooks, Context API state management, API services (Axios), shopping cart logic, admin panels, Mantine UI layouts (Grid, Stack), notifications, routing, or handling API calls and frontend business logic.
---

# Frontend Development Guidelines for Dukkan

Expert guidance for developing the React frontend in the Dukkan e-commerce SaaS platform.

## Tech Stack Overview

- **React 19.0.0** with modern hooks and features
- **TypeScript 5.7.2** for type safety
- **Mantine UI 8.3.7** - Component library
- **Mantine Hooks 8.3.7** - Utility hooks
- **Mantine Form 8.3.7** - Form management
- **Mantine Notifications 8.3.7** - Toast notifications
- **Vite 6.0.7** - Build tool and dev server
- **React Router v7.1.1** - Client-side routing
- **Axios 1.7.9** - HTTP client
- **Tabler Icons 3.29.0** - Icon library

## Project Structure

```
frontend/src/
├── components/          # Reusable UI components
│   ├── layout/         # Header, Footer, navigation
│   ├── products/       # Product-specific components
│   ├── admin/          # Admin panel components
│   └── common/         # Shared components (future)
├── pages/              # Page components (routes)
│   ├── admin/         # Admin pages
│   └── customer/      # Customer pages
├── services/           # API service layer
├── context/            # React Context for state
├── types/              # TypeScript definitions
├── hooks/              # Custom React hooks (future)
├── utils/              # Utility functions (future)
├── App.tsx            # Main app with routing
└── main.tsx           # Entry point with providers
```

## Core Principles

### 1. Type Safety First
- **Never use `any`** - Use proper TypeScript types
- Define interfaces for all props
- Type all API responses
- Use type inference when obvious

### 2. Component Design
- **Functional components only** - No class components
- **Small, focused components** - Single responsibility
- **Composition over inheritance**
- **Named exports preferred** over default exports

### 3. State Management
- **React Context API** for global state (cart, auth)
- **Local state** (`useState`) for component-specific state
- **No Redux** - Context is sufficient for SaaS scale
- **localStorage** for persistence (cart)

## Naming Conventions

- **Components**: `PascalCase` (e.g., `ProductCard`, `ProductList`)
- **Files**: `PascalCase` for components, `camelCase` for utilities
- **Variables/Functions**: `camelCase` (e.g., `productId`, `fetchProducts`)
- **Constants**: `UPPER_SNAKE_CASE` (e.g., `API_BASE_URL`)
- **Types/Interfaces**: `PascalCase` (e.g., `Product`, `ApiResponse<T>`)

## Component Patterns

### Basic Component Template

```typescript
import { FC } from 'react';
import { Card, Text, Button, Group } from '@mantine/core';
import { Product } from '../../types/product';

interface ProductCardProps {
  product: Product;
  onAddToCart: (productId: string) => void;
}

export const ProductCard: FC<ProductCardProps> = ({ product, onAddToCart }) => {
  return (
    <Card shadow="sm" padding="lg" radius="md" withBorder>
      <Text size="lg" fw={500}>{product.name}</Text>
      <Text size="sm" c="dimmed">{product.description}</Text>
      <Text size="xl" fw={700}>${product.price}</Text>
      <Group justify="space-between" mt="md">
        <Text size="sm">Stock: {product.stockQuantity}</Text>
        <Button
          onClick={() => onAddToCart(product.id)}
          disabled={product.stockQuantity === 0}
        >
          Add to Cart
        </Button>
      </Group>
    </Card>
  );
};
```

### Page Component Template

```typescript
import { FC, useEffect, useState } from 'react';
import { Container, Title, Grid, Loader, Text } from '@mantine/core';
import { notifications } from '@mantine/notifications';
import { productService } from '../../services/productService';
import { Product } from '../../types/product';
import { ProductCard } from '../../components/products/ProductCard';

export const ProductsPage: FC = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadProducts();
  }, []);

  const loadProducts = async () => {
    try {
      setLoading(true);
      const data = await productService.getAll();
      setProducts(data);
    } catch (error) {
      notifications.show({
        title: 'Error',
        message: 'Failed to load products',
        color: 'red',
      });
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <Container>
        <Loader size="xl" />
      </Container>
    );
  }

  return (
    <Container size="xl" py="xl">
      <Title order={1} mb="xl">Products</Title>
      {products.length === 0 ? (
        <Text c="dimmed">No products found</Text>
      ) : (
        <Grid>
          {products.map((product) => (
            <Grid.Col key={product.id} span={{ base: 12, sm: 6, md: 4 }}>
              <ProductCard product={product} onAddToCart={handleAddToCart} />
            </Grid.Col>
          ))}
        </Grid>
      )}
    </Container>
  );
};
```

## API Service Layer

### Service Pattern

**Location**: `src/services/`

**Rules**:
- One service per resource (e.g., `productService`, `categoryService`)
- Typed responses using generics
- Centralized error handling
- Use Axios interceptors for common concerns

```typescript
import axios, { AxiosInstance } from 'axios';
import { Product, CreateProductRequest, UpdateProductRequest } from '../types/product';
import { ApiResponse, PaginatedResponse } from '../types/api';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

// Create axios instance
const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add interceptors for auth (future)
apiClient.interceptors.request.use((config) => {
  // Add auth token when implemented
  // const token = localStorage.getItem('token');
  // if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export const productService = {
  getAll: async (): Promise<Product[]> => {
    const response = await apiClient.get<ApiResponse<Product[]>>('/products');
    return response.data.data;
  },

  getById: async (id: string): Promise<Product> => {
    const response = await apiClient.get<ApiResponse<Product>>(`/products/${id}`);
    return response.data.data;
  },

  getPaginated: async (page: number, size: number): Promise<PaginatedResponse<Product>> => {
    const response = await apiClient.get<ApiResponse<PaginatedResponse<Product>>>(
      `/products?page=${page}&size=${size}`
    );
    return response.data.data;
  },

  search: async (query: string): Promise<Product[]> => {
    const response = await apiClient.get<ApiResponse<Product[]>>(
      `/products/search?name=${encodeURIComponent(query)}`
    );
    return response.data.data;
  },

  create: async (product: CreateProductRequest): Promise<Product> => {
    const response = await apiClient.post<ApiResponse<Product>>('/products', product);
    return response.data.data;
  },

  update: async (id: string, product: UpdateProductRequest): Promise<Product> => {
    const response = await apiClient.put<ApiResponse<Product>>(`/products/${id}`, product);
    return response.data.data;
  },

  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/products/${id}`);
  },
};
```

## TypeScript Types

### Type Definitions

**Location**: `src/types/`

```typescript
// src/types/product.ts
export interface Product {
  id: string;
  sku: string;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  category: Category | null;
  imageUrls: string[];
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateProductRequest {
  sku: string;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  categoryId: string | null;
  imageUrls: string[];
}

export interface UpdateProductRequest {
  name?: string;
  description?: string;
  price?: number;
  stockQuantity?: number;
  categoryId?: string | null;
  imageUrls?: string[];
  isActive?: boolean;
}

// src/types/api.ts
export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message: string;
  error?: ErrorResponse;
  timestamp: string;
}

export interface ErrorResponse {
  code: string;
  message: string;
  details: string[];
}

export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
```

## State Management with Context

### Context Pattern

```typescript
// src/context/CartContext.tsx
import { createContext, useContext, useState, useEffect, FC, ReactNode } from 'react';
import { Product } from '../types/product';

interface CartItem {
  product: Product;
  quantity: number;
}

interface CartContextValue {
  items: CartItem[];
  addItem: (product: Product, quantity: number) => void;
  removeItem: (productId: string) => void;
  updateQuantity: (productId: string, quantity: number) => void;
  clearCart: () => void;
  totalItems: number;
  totalPrice: number;
}

const CartContext = createContext<CartContextValue | undefined>(undefined);

export const CartProvider: FC<{ children: ReactNode }> = ({ children }) => {
  const [items, setItems] = useState<CartItem[]>(() => {
    // Load from localStorage
    const saved = localStorage.getItem('cart');
    return saved ? JSON.parse(saved) : [];
  });

  // Save to localStorage on changes
  useEffect(() => {
    localStorage.setItem('cart', JSON.stringify(items));
  }, [items]);

  const addItem = (product: Product, quantity: number = 1) => {
    setItems((prev) => {
      const existing = prev.find((item) => item.product.id === product.id);
      if (existing) {
        return prev.map((item) =>
          item.product.id === product.id
            ? { ...item, quantity: item.quantity + quantity }
            : item
        );
      }
      return [...prev, { product, quantity }];
    });
  };

  const removeItem = (productId: string) => {
    setItems((prev) => prev.filter((item) => item.product.id !== productId));
  };

  const updateQuantity = (productId: string, quantity: number) => {
    if (quantity <= 0) {
      removeItem(productId);
      return;
    }
    setItems((prev) =>
      prev.map((item) =>
        item.product.id === productId ? { ...item, quantity } : item
      )
    );
  };

  const clearCart = () => setItems([]);

  const totalItems = items.reduce((sum, item) => sum + item.quantity, 0);
  const totalPrice = items.reduce(
    (sum, item) => sum + item.product.price * item.quantity,
    0
  );

  return (
    <CartContext.Provider
      value={{
        items,
        addItem,
        removeItem,
        updateQuantity,
        clearCart,
        totalItems,
        totalPrice,
      }}
    >
      {children}
    </CartContext.Provider>
  );
};

// Custom hook
export const useCart = () => {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart must be used within CartProvider');
  }
  return context;
};
```

### Using Context in Components

```typescript
import { FC } from 'react';
import { Button } from '@mantine/core';
import { useCart } from '../../context/CartContext';
import { Product } from '../../types/product';

export const AddToCartButton: FC<{ product: Product }> = ({ product }) => {
  const { addItem } = useCart();

  return (
    <Button
      onClick={() => addItem(product, 1)}
      disabled={product.stockQuantity === 0}
    >
      Add to Cart
    </Button>
  );
};
```

## Form Handling with Mantine Form

### Form Pattern

```typescript
import { FC, useState } from 'react';
import { useForm } from '@mantine/form';
import { TextInput, NumberInput, Textarea, Button, Stack } from '@mantine/core';
import { notifications } from '@mantine/notifications';
import { productService } from '../../services/productService';
import { CreateProductRequest } from '../../types/product';

export const ProductForm: FC = () => {
  const [loading, setLoading] = useState(false);

  const form = useForm<CreateProductRequest>({
    initialValues: {
      sku: '',
      name: '',
      description: '',
      price: 0,
      stockQuantity: 0,
      categoryId: null,
      imageUrls: [],
    },
    validate: {
      sku: (value) => (value.length < 3 ? 'SKU must be at least 3 characters' : null),
      name: (value) => (value.length < 3 ? 'Name must be at least 3 characters' : null),
      price: (value) => (value <= 0 ? 'Price must be positive' : null),
      stockQuantity: (value) => (value < 0 ? 'Stock cannot be negative' : null),
    },
  });

  const handleSubmit = async (values: CreateProductRequest) => {
    try {
      setLoading(true);
      await productService.create(values);
      notifications.show({
        title: 'Success',
        message: 'Product created successfully',
        color: 'green',
      });
      form.reset();
    } catch (error) {
      notifications.show({
        title: 'Error',
        message: 'Failed to create product',
        color: 'red',
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={form.onSubmit(handleSubmit)}>
      <Stack gap="md">
        <TextInput
          label="SKU"
          placeholder="Enter SKU"
          required
          {...form.getInputProps('sku')}
        />

        <TextInput
          label="Name"
          placeholder="Product name"
          required
          {...form.getInputProps('name')}
        />

        <Textarea
          label="Description"
          placeholder="Product description"
          {...form.getInputProps('description')}
        />

        <NumberInput
          label="Price"
          placeholder="0.00"
          required
          min={0}
          decimalScale={2}
          prefix="$"
          {...form.getInputProps('price')}
        />

        <NumberInput
          label="Stock Quantity"
          placeholder="0"
          required
          min={0}
          {...form.getInputProps('stockQuantity')}
        />

        <Button type="submit" loading={loading}>
          Create Product
        </Button>
      </Stack>
    </form>
  );
};
```

## Routing with React Router

### Route Configuration

```typescript
// src/App.tsx
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { MantineProvider } from '@mantine/core';
import { Notifications } from '@mantine/notifications';
import { CartProvider } from './context/CartContext';
import { Layout } from './components/layout/Layout';
import { HomePage } from './pages/HomePage';
import { ProductsPage } from './pages/ProductsPage';
import { ProductDetailPage } from './pages/ProductDetailPage';
import { AdminLayout } from './components/admin/AdminLayout';
import { AdminProductsPage } from './pages/admin/AdminProductsPage';

export const App = () => {
  return (
    <MantineProvider>
      <Notifications position="top-right" />
      <CartProvider>
        <BrowserRouter>
          <Routes>
            {/* Customer Routes */}
            <Route path="/" element={<Layout />}>
              <Route index element={<HomePage />} />
              <Route path="products" element={<ProductsPage />} />
              <Route path="products/:id" element={<ProductDetailPage />} />
            </Route>

            {/* Admin Routes */}
            <Route path="/admin" element={<AdminLayout />}>
              <Route index element={<AdminDashboard />} />
              <Route path="products" element={<AdminProductsPage />} />
              <Route path="categories" element={<AdminCategoriesPage />} />
            </Route>
          </Routes>
        </BrowserRouter>
      </CartProvider>
    </MantineProvider>
  );
};
```

### Navigation

```typescript
import { FC } from 'react';
import { Link } from 'react-router-dom';
import { Button } from '@mantine/core';

export const NavigationButton: FC = () => {
  return (
    <Button component={Link} to="/products">
      View Products
    </Button>
  );
};
```

## Error Handling

### Loading and Error States

```typescript
import { FC, useEffect, useState } from 'react';
import { Alert, Loader, Center, Stack, Button } from '@mantine/core';
import { IconAlertCircle } from '@tabler/icons-react';
import { productService } from '../services/productService';
import { Product } from '../types/product';

export const ProductList: FC = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadProducts = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await productService.getAll();
      setProducts(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load products');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProducts();
  }, []);

  if (loading) {
    return (
      <Center h={200}>
        <Loader size="xl" />
      </Center>
    );
  }

  if (error) {
    return (
      <Stack gap="md">
        <Alert icon={<IconAlertCircle />} title="Error" color="red">
          {error}
        </Alert>
        <Button onClick={loadProducts}>Retry</Button>
      </Stack>
    );
  }

  return (
    <div>
      {/* Render products */}
    </div>
  );
};
```

## Mantine UI Best Practices

### Responsive Design

```typescript
import { Grid, Stack } from '@mantine/core';

// Responsive grid
<Grid>
  <Grid.Col span={{ base: 12, sm: 6, md: 4, lg: 3 }}>
    {/* Content */}
  </Grid.Col>
</Grid>

// Stack with responsive spacing
<Stack gap={{ base: 'sm', md: 'md', lg: 'lg' }}>
  {/* Content */}
</Stack>
```

### Theme Usage

```typescript
import { Text, useMantineTheme } from '@mantine/core';

export const ThemedComponent = () => {
  const theme = useMantineTheme();

  return (
    <Text c={theme.colors.blue[6]}>
      Themed text
    </Text>
  );
};
```

## Custom Hooks (Future Pattern)

```typescript
// src/hooks/useProducts.ts
import { useState, useEffect } from 'react';
import { productService } from '../services/productService';
import { Product } from '../types/product';

export const useProducts = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await productService.getAll();
      setProducts(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, []);

  return { products, loading, error, refetch: fetchProducts };
};
```

## Common Mistakes to Avoid

❌ **Don't use `any`**
```typescript
// Bad
const handleClick = (data: any) => { ... }
```

✅ **Use proper types**
```typescript
// Good
const handleClick = (data: Product) => { ... }
```

❌ **Don't fetch in render**
```typescript
// Bad
const MyComponent = () => {
  const products = productService.getAll(); // Wrong!
  ...
}
```

✅ **Use useEffect for data fetching**
```typescript
// Good
const MyComponent = () => {
  const [products, setProducts] = useState<Product[]>([]);

  useEffect(() => {
    productService.getAll().then(setProducts);
  }, []);
  ...
}
```

❌ **Don't forget error handling**
❌ **Don't forget loading states**
❌ **Don't mutate state directly**

## Environment Variables

```typescript
// .env
VITE_API_BASE_URL=http://localhost:8080/api/v1

// Usage in code
const apiUrl = import.meta.env.VITE_API_BASE_URL;
```

## Quick Commands

```bash
# Install dependencies
npm install

# Start dev server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Type check
npm run type-check

# Lint
npm run lint
```

## Performance Optimization

- Use `React.memo` for expensive components
- Lazy load routes: `const Page = lazy(() => import('./Page'))`
- Optimize images (compression, lazy loading)
- Use code splitting for large features
- Avoid unnecessary re-renders

## Accessibility

- Use semantic HTML
- Add proper ARIA labels
- Ensure keyboard navigation works
- Use Mantine's built-in accessibility features
- Test with screen readers

## Current Application Structure

**Customer Features**:
- Product browsing with search
- Product detail pages
- Shopping cart with localStorage
- Category listing

**Admin Features**:
- Product CRUD with forms
- Category CRUD with forms
- Admin sidebar navigation

**State Management**:
- CartContext for shopping cart
- localStorage for persistence
- Local state for forms and UI

This is a SaaS e-commerce platform - keep patterns consistent and maintainable!
