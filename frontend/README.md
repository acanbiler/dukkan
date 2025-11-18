# Dukkan Frontend

Modern React frontend application for the Dukkan shopping platform.

## Tech Stack

- **React 19.0** - Latest React with modern features
- **TypeScript** - Type-safe JavaScript
- **Mantine UI 8.3.7** - Modern React component library
- **React Router v7** - Client-side routing
- **Vite 6** - Fast build tool
- **Axios** - HTTP client

## Prerequisites

- Node.js 20+ and npm 10+
- Backend services running (API Gateway on port 8080)

## Getting Started

### Install Dependencies

```bash
npm install
```

### Environment Variables

Copy the example environment file:

```bash
cp .env.example .env
```

Edit `.env` if needed:
```
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

### Run Development Server

```bash
npm run dev
```

The application will be available at `http://localhost:5173`

### Build for Production

```bash
npm run build
```

### Preview Production Build

```bash
npm run preview
```

## Project Structure

```
src/
├── components/          # Reusable UI components
│   ├── cart/           # Shopping cart components
│   │   ├── CartIcon.tsx
│   │   ├── CartDrawer.tsx
│   │   └── CartItem.tsx
│   ├── layout/         # Layout components (Header, Footer, etc.)
│   │   └── Header.tsx
│   └── products/       # Product-specific components
│       └── ProductCard.tsx
├── context/            # React Context providers
│   └── CartContext.tsx # Shopping cart state management
├── pages/              # Page components (routes)
│   ├── HomePage.tsx
│   ├── ProductsPage.tsx
│   ├── ProductDetailPage.tsx
│   ├── CategoriesPage.tsx
│   └── CartPage.tsx
├── services/           # API service calls
│   ├── api.ts         # Axios instance and interceptors
│   ├── productService.ts
│   └── categoryService.ts
├── types/              # TypeScript type definitions
│   ├── api.ts
│   ├── cart.ts
│   ├── product.ts
│   └── category.ts
├── App.tsx            # Main app component with routing
└── main.tsx           # Application entry point
```

## Available Routes

- `/` - Home page
- `/products` - Product listing page with search
- `/products/:id` - Product detail page
- `/categories` - Categories listing page
- `/cart` - Shopping cart page

## API Integration

The frontend communicates with the backend through the API Gateway (port 8080).

### API Client

All API calls use Axios with:
- Base URL configuration
- Request/response interceptors
- Error handling
- TypeScript types

### Services

- **productService** - Product-related API calls
- **categoryService** - Category-related API calls

### Example Usage

```typescript
import { productService } from '@/services/productService';

// Get all products
const products = await productService.getAll(0, 20);

// Get product by ID
const product = await productService.getById(productId);

// Search products
const results = await productService.search('laptop', 0, 20);
```

## Components

### Layout Components

- **Header** - Navigation header with links

### Product Components

- **ProductCard** - Product display card with image, price, stock status

## Styling

- Uses Mantine UI components for consistent design
- Responsive grid system
- Modern, clean interface
- Customizable theme (can be configured in `main.tsx`)

## Development Guidelines

### TypeScript

- All components use TypeScript
- Proper type definitions for props
- Type-safe API calls

### Component Structure

```typescript
interface MyComponentProps {
  // Props definition
}

export const MyComponent = ({ prop1, prop2 }: MyComponentProps) => {
  // Component logic
  return (
    // JSX
  );
};
```

### State Management

- React `useState` for local component state
- **React Context API** for global shopping cart state
- `CartProvider` wraps entire app for cart access
- No Redux (keeping it simple with Context API)

### API Calls

Use async/await with try-catch:

```typescript
const loadData = async () => {
  try {
    setLoading(true);
    const data = await productService.getAll();
    setData(data);
  } catch (error) {
    notifications.show({
      title: 'Error',
      message: 'Failed to load data',
      color: 'red',
    });
  } finally {
    setLoading(false);
  }
};
```

## Common Tasks

### Adding a New Page

1. Create page component in `src/pages/`
2. Add route to `App.tsx`
3. Add navigation link to `Header.tsx` if needed

### Adding a New API Service

1. Define types in `src/types/`
2. Create service in `src/services/`
3. Use service in components

### Styling

Use Mantine components and props:

```typescript
<Button variant="filled" color="blue" size="lg">
  Click me
</Button>
```

## Scripts

- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Run ESLint
- `npm run type-check` - TypeScript type checking

## Known Issues

None currently.

## Features

### Shopping Cart ✅
- Add products to cart
- Update quantities
- Remove items
- Cart drawer for quick view
- Cart page for full view
- localStorage persistence
- Real-time cart total
- Stock validation
- Cart icon with item count badge

### Product Browsing ✅
- Product listing with grid layout
- Search functionality
- Product detail pages
- Category browsing
- Responsive design

### User Authentication ✅
- User registration
- Login with JWT tokens
- Role-based access (Customer, Admin)
- Protected routes
- Auth state management (AuthContext)

### Order Management ✅
- Order placement with checkout flow
- Order history with pagination
- Order details view
- Order status tracking
- Stock validation during checkout

### Payment Integration ✅
- Iyzico payment processing
- Payment confirmation
- Payment status tracking
- Order-payment linkage

### Internationalization ✅
- English and Turkish language support
- Language switcher component
- Persistent language preference
- Browser language detection

### Admin Panel ✅
- Product CRUD operations
- Category management
- Inventory tracking
- Admin-only routes

## Future Enhancements

- Product filtering by category/price
- Product reviews and ratings
- Wishlist functionality
- Dark mode toggle
- Product image upload
- Address management
- Email notifications
- Advanced search with filters
