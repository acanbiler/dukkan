# Dukkan - Development Guide

## Prerequisites

### Required Tools
- **Java Development Kit (JDK) 17**
- **Node.js 20+** and **npm 10+**
- **Maven 3.9+**
- **Docker 24+** and **Docker Compose 2.20+**
- **Git 2.40+**
- **IDE**: IntelliJ IDEA (recommended for Java) or VS Code

### Recommended IDE Extensions

#### For VS Code
- Java Extension Pack
- Spring Boot Extension Pack
- ESLint
- Prettier
- Docker
- GitLens

#### For IntelliJ IDEA
- Spring Boot plugin (built-in)
- Docker plugin (built-in)
- SonarLint

## Project Setup

### Initial Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd dukkan
   ```

2. **Setup Backend Services**
   ```bash
   # Build all backend services
   cd backend/api-gateway
   mvn clean install

   cd ../product-service
   mvn clean install
   ```

3. **Setup Frontend**
   ```bash
   cd frontend
   npm install
   ```

4. **Setup Environment Variables**
   ```bash
   # Copy example environment files
   cp backend/api-gateway/.env.example backend/api-gateway/.env
   cp backend/product-service/.env.example backend/product-service/.env
   cp frontend/.env.example frontend/.env

   # Edit .env files with your local configuration
   ```

5. **Start Database (if using Docker)**
   ```bash
   docker-compose up -d postgres
   ```

## Development Workflow

### Running Services Locally

#### Start Backend Services

**API Gateway**
```bash
cd backend/api-gateway
mvn spring-boot:run
```
Access: http://localhost:8080

**Product Service**
```bash
cd backend/product-service
mvn spring-boot:run
```
Access: http://localhost:8081

#### Start Frontend
```bash
cd frontend
npm run dev
```
Access: http://localhost:5173

### Using Docker Compose

Start all services:
```bash
docker-compose up -d
```

Stop all services:
```bash
docker-compose down
```

View logs:
```bash
docker-compose logs -f [service-name]
```

Rebuild after changes:
```bash
docker-compose up -d --build
```

## Coding Standards

### Java / Spring Boot

#### Naming Conventions
- **Classes**: PascalCase (e.g., `ProductService`, `ProductController`)
- **Methods**: camelCase (e.g., `getProductById`, `createProduct`)
- **Variables**: camelCase (e.g., `productId`, `totalPrice`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `MAX_RETRY_ATTEMPTS`)
- **Packages**: lowercase (e.g., `com.dukkan.product.service`)

#### Project Structure (per service)
```
src/main/java/com/dukkan/{service}/
├── controller/          # REST controllers
├── service/            # Business logic
├── repository/         # Data access layer
├── model/              # Entity classes
├── dto/                # Data Transfer Objects
├── exception/          # Custom exceptions
├── config/             # Configuration classes
└── util/               # Utility classes

src/main/resources/
├── application.yml     # Main configuration
├── application-dev.yml # Development config
├── application-prod.yml # Production config
└── db/migration/       # Database migrations
```

#### Code Style
- Follow Google Java Style Guide
- Use meaningful variable and method names
- Keep methods small and focused (Single Responsibility)
- Add JavaDoc for public APIs
- Use Optional instead of returning null
- Prefer constructor injection over field injection
- Use lombok to reduce boilerplate (e.g., @Data, @Builder)

#### Example Controller
```java
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<ProductDTO> products = productService.getAllProducts(page, size);
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable UUID id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        ProductDTO product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(product));
    }
}
```

#### Example Service
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductDTO getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        log.info("Retrieved product with id: {}", id);
        return productMapper.toDTO(product);
    }

    @Transactional
    public ProductDTO createProduct(CreateProductRequest request) {
        Product product = productMapper.toEntity(request);
        Product savedProduct = productRepository.save(product);
        log.info("Created product with id: {}", savedProduct.getId());
        return productMapper.toDTO(savedProduct);
    }
}
```

### TypeScript / React

#### Naming Conventions
- **Components**: PascalCase (e.g., `ProductCard`, `ProductList`)
- **Files**: PascalCase for components, camelCase for utilities
- **Variables/Functions**: camelCase (e.g., `productId`, `fetchProducts`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `API_BASE_URL`)
- **Interfaces/Types**: PascalCase with 'I' prefix optional (e.g., `Product`, `IProduct`)

#### Project Structure
```
src/
├── components/         # Reusable UI components
│   ├── common/        # Common components (Button, Input, etc.)
│   └── products/      # Feature-specific components
├── pages/             # Page components (routes)
├── services/          # API service calls
├── hooks/             # Custom React hooks
├── types/             # TypeScript type definitions
├── utils/             # Utility functions
├── assets/            # Images, fonts, etc.
├── App.tsx           # Main app component
└── main.tsx          # Entry point
```

#### Code Style
- Use functional components with hooks
- Use TypeScript for type safety
- Prefer named exports over default exports
- Use absolute imports with path aliases
- Keep components small and focused
- Extract business logic to custom hooks
- Use proper TypeScript types (avoid 'any')

#### Example Component
```typescript
import { FC } from 'react';
import { Card, Text, Button, Group } from '@mantine/core';
import { Product } from '@/types/product';

interface ProductCardProps {
  product: Product;
  onAddToCart: (productId: string) => void;
}

export const ProductCard: FC<ProductCardProps> = ({ product, onAddToCart }) => {
  return (
    <Card shadow="sm" padding="lg">
      <Text size="lg" weight={500}>{product.name}</Text>
      <Text size="sm" color="dimmed">{product.description}</Text>
      <Text size="xl" weight={700}>${product.price}</Text>
      <Group position="apart" mt="md">
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

#### Example Custom Hook
```typescript
import { useState, useEffect } from 'react';
import { productService } from '@/services/productService';
import { Product } from '@/types/product';

export const useProducts = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchProducts = async () => {
      setLoading(true);
      try {
        const data = await productService.getAll();
        setProducts(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to fetch products');
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();
  }, []);

  return { products, loading, error };
};
```

#### Example API Service
```typescript
import axios from 'axios';
import { Product, CreateProductRequest } from '@/types/product';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
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

  create: async (product: CreateProductRequest): Promise<Product> => {
    const response = await apiClient.post<ApiResponse<Product>>('/products', product);
    return response.data.data;
  },
};
```

## Testing

### Backend Testing

#### Unit Tests
```bash
# Run all tests
mvn test

# Run tests for specific service
cd backend/product-service
mvn test

# Run tests with coverage
mvn test jacoco:report
```

#### Example Unit Test
```java
@SpringBootTest
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void getProductById_WhenExists_ReturnsProduct() {
        // Given
        UUID productId = UUID.randomUUID();
        Product product = new Product();
        product.setId(productId);
        product.setName("Test Product");

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        // When
        ProductDTO result = productService.getProductById(productId);

        // Then
        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals("Test Product", result.getName());
    }

    @Test
    void getProductById_WhenNotExists_ThrowsException() {
        // Given
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductNotFoundException.class,
                () -> productService.getProductById(productId));
    }
}
```

### Frontend Testing

#### Run Tests
```bash
cd frontend
npm test

# Run with coverage
npm run test:coverage

# Run in watch mode
npm run test:watch
```

#### Example Component Test
```typescript
import { render, screen, fireEvent } from '@testing-library/react';
import { ProductCard } from './ProductCard';

describe('ProductCard', () => {
  const mockProduct = {
    id: '1',
    name: 'Test Product',
    description: 'Test Description',
    price: 99.99,
    stockQuantity: 10,
  };

  it('renders product information', () => {
    render(<ProductCard product={mockProduct} onAddToCart={jest.fn()} />);

    expect(screen.getByText('Test Product')).toBeInTheDocument();
    expect(screen.getByText('Test Description')).toBeInTheDocument();
    expect(screen.getByText('$99.99')).toBeInTheDocument();
  });

  it('calls onAddToCart when button clicked', () => {
    const mockOnAddToCart = jest.fn();
    render(<ProductCard product={mockProduct} onAddToCart={mockOnAddToCart} />);

    fireEvent.click(screen.getByText('Add to Cart'));

    expect(mockOnAddToCart).toHaveBeenCalledWith('1');
  });

  it('disables button when out of stock', () => {
    const outOfStockProduct = { ...mockProduct, stockQuantity: 0 };
    render(<ProductCard product={outOfStockProduct} onAddToCart={jest.fn()} />);

    expect(screen.getByText('Add to Cart')).toBeDisabled();
  });
});
```

## Database Management

### Running Migrations

```bash
# Migrations run automatically on application start
# Located in: src/main/resources/db/migration/

# Example migration file naming:
# V1__create_products_table.sql
# V2__add_category_to_products.sql
```

### Database Access

```bash
# Connect to database (if using Docker)
docker exec -it dukkan-postgres psql -U dukkan -d dukkan

# Backup database
docker exec dukkan-postgres pg_dump -U dukkan dukkan > backup.sql

# Restore database
docker exec -i dukkan-postgres psql -U dukkan dukkan < backup.sql
```

## API Documentation

### Accessing Swagger UI

Once services are running:
- Product Service: http://localhost:8081/swagger-ui.html
- API Gateway: http://localhost:8080/swagger-ui.html

### Generating API Docs

API documentation is auto-generated from code annotations:
```java
@Operation(summary = "Get product by ID", description = "Retrieves a single product by its ID")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Product found"),
    @ApiResponse(responseCode = "404", description = "Product not found")
})
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<ProductDTO>> getProductById(@PathVariable UUID id) {
    // ...
}
```

## Debugging

### Backend Debugging
1. Run service in debug mode:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
   ```
2. Connect your IDE debugger to port 5005

### Frontend Debugging
- Use browser DevTools
- React DevTools extension
- Console logs and breakpoints

## Common Issues & Solutions

### Backend Issues

**Issue: Port already in use**
```bash
# Find process using port
lsof -i :8080
# Kill the process
kill -9 <PID>
```

**Issue: Maven dependencies not resolving**
```bash
mvn clean install -U
# or delete ~/.m2/repository and rebuild
```

**Issue: Database connection failed**
- Check if database is running: `docker ps`
- Verify connection string in application.yml
- Check credentials in .env file

### Frontend Issues

**Issue: Module not found**
```bash
# Clear node_modules and reinstall
rm -rf node_modules package-lock.json
npm install
```

**Issue: Port 5173 in use**
```bash
# Kill process on port 5173
lsof -i :5173
kill -9 <PID>
```

## Git Workflow

### Branch Naming Convention
- Feature: `feature/description` (e.g., `feature/add-product-search`)
- Bug fix: `fix/description` (e.g., `fix/product-price-calculation`)
- Hotfix: `hotfix/description` (e.g., `hotfix/security-patch`)

### Commit Message Format
```
<type>(<scope>): <subject>

<body>

<footer>
```

Types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks

Example:
```
feat(product): add product search functionality

Implement search endpoint with filtering by name and category.
Includes pagination support.

Closes #123
```

### Pull Request Process
1. Create feature branch from main
2. Make changes and commit
3. Push branch and create PR
4. Ensure tests pass
5. Request code review
6. Address review comments
7. Merge after approval

## Performance Optimization

### Backend
- Use pagination for large datasets
- Implement caching for frequently accessed data
- Use database indexes on commonly queried fields
- Use lazy loading for relationships
- Monitor slow queries and optimize

### Frontend
- Use React.memo for expensive components
- Implement virtual scrolling for long lists
- Lazy load routes and components
- Optimize images (compression, lazy loading)
- Use code splitting

## Security Best Practices

### Backend
- Never commit secrets or credentials
- Use environment variables for sensitive config
- Validate all user inputs
- Use parameterized queries (prevent SQL injection)
- Implement rate limiting
- Keep dependencies updated
- Use HTTPS in production

### Frontend
- Sanitize user inputs
- Use HTTPS for API calls
- Store tokens securely (httpOnly cookies)
- Implement CSRF protection
- Validate data from backend
- Don't expose sensitive data in client code

## Useful Commands Reference

### Backend
```bash
# Build without tests
mvn clean install -DskipTests

# Run specific test class
mvn test -Dtest=ProductServiceTest

# Check for dependency updates
mvn versions:display-dependency-updates

# Format code
mvn spotless:apply
```

### Frontend
```bash
# Build for production
npm run build

# Preview production build
npm run preview

# Lint code
npm run lint

# Format code
npm run format

# Type check
npm run type-check
```

### Docker
```bash
# View running containers
docker ps

# View all containers
docker ps -a

# View logs
docker logs -f <container-name>

# Execute command in container
docker exec -it <container-name> bash

# Remove all stopped containers
docker container prune

# Remove all unused images
docker image prune -a
```

## Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [React Documentation](https://react.dev)
- [Mantine UI Documentation](https://mantine.dev)
- [Docker Documentation](https://docs.docker.com)
- [Maven Documentation](https://maven.apache.org/guides/)
