# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Dukkan is a shopping web application built with a microservices architecture using React 19.2 frontend and Spring Boot 3.5.7 backend services. The project follows SOLID principles, Domain-Driven Design, and emphasizes simplicity (KISS principle).

## Technology Stack

**Backend:**
- Java 25, Spring Boot 3.5.7, Spring Cloud Gateway
- PostgreSQL 17, Liquibase for migrations
- Maven for build management
- No caching layer (deferred for simplicity)

**Frontend:**
- React 19.2, TypeScript, Mantine UI 8.3.7, Vite
- React Context API for state management

**Infrastructure:**
- Docker & Docker Compose
- Direct routing via Spring Cloud Gateway (no service discovery)

## Common Commands

### Backend (Product Service)

```bash
# Navigate to service
cd backend/product-service

# Build service
mvn clean install

# Run service (requires PostgreSQL running)
mvn spring-boot:run

# Run tests
mvn test

# Run specific test class
mvn test -Dtest=ProductServiceTest

# Skip tests during build
mvn clean install -DskipTests
```

### Database

```bash
# Start PostgreSQL only
docker compose up -d postgres

# Stop all services
docker compose down

# View PostgreSQL logs
docker compose logs postgres

# Connect to database
docker exec -it dukkan-postgres psql -U dukkan -d dukkan_product
```

### API Gateway

```bash
cd backend/api-gateway
mvn clean install
mvn spring-boot:run
```

**Note**: API Gateway requires Product Service to be running for proper routing.

### Frontend

```bash
cd frontend

# Install dependencies (first time only)
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Type check
npm run type-check
```

**Note**: Frontend requires API Gateway to be running on port 8080.

## Architecture Principles

### Layered Architecture (Inside-Out Development)

The Product Service follows a strict layered architecture built from domain outward:

1. **Domain Layer** (`model/`) - Core business entities with rich behavior
   - Entities: `Product`, `Category`
   - Contains business logic methods (e.g., `reduceStock()`, `isLowStock()`)
   - NOT anemic - domain objects have behavior, not just data

2. **Repository Layer** (`repository/`) - Data access interfaces
   - Spring Data JPA repositories
   - Custom query methods using method naming and `@Query`
   - Returns `Optional<T>` for single results, `Page<T>` for paginated results

3. **Service Layer** (`service/`) - Business logic orchestration
   - Interface + Implementation pattern (e.g., `ProductService` + `ProductServiceImpl`)
   - Transaction management via `@Transactional`
   - Uses DTOs for API contracts, Mappers for entity↔DTO conversion
   - Business validation and exception handling

4. **Controller Layer** (`controller/`) - REST API endpoints
   - Thin controllers - delegate to services
   - Global exception handling via `@RestControllerAdvice`
   - Consistent response format using `ApiResponse<T>`
   - OpenAPI/Swagger annotations for documentation

### Dependency Flow

```
Controller → Service Interface → Service Impl → Repository Interface → Database
                ↓                      ↓
              DTOs              Mapper (Entity ↔ DTO)
```

**Critical Rule**: Dependencies only flow inward. Controllers depend on services, services depend on repositories. Never the reverse.

## Key Architectural Decisions

### No Caching Layer
The project intentionally has **no caching** (Redis removed for simplicity). This is a deliberate architectural choice:
- Database indexing and HikariCP pooling handle initial load
- Can add Redis/Caffeine later when metrics justify it
- Follows KISS principle

When adding caching later:
1. Add `spring-boot-starter-data-redis` to pom.xml
2. Add `@EnableCaching` to main application class
3. Add `@Cacheable` on read methods, `@CacheEvict` on write methods
4. Configure Redis in application.yml

### Exception Handling Strategy

Custom exceptions are thrown at service layer and caught globally:
- `ResourceNotFoundException` → 404
- `DuplicateResourceException` → 409
- `InvalidOperationException` → 400
- Validation errors → 400

All exceptions handled by `GlobalExceptionHandler` with consistent `ErrorResponse` format.

### Database Migrations

Liquibase changelogs in `src/main/resources/db/changelog/changes/`:
- Naming: `001-create-products-table.xml`, `002-create-categories-table.xml`
- Master changelog includes all changes in order
- Migrations run automatically on startup (`liquibase.enabled=true`)

**Adding new migration:**
1. Create new file in `changes/` with incremented number
2. Add `<include>` to `db.changelog-master.xml`
3. Restart application to apply

## Code Organization Patterns

### DTO Pattern
- DTOs in `dto/` package represent API contracts
- Separate from domain entities (different concerns)
- Validation annotations on DTOs (`@NotBlank`, `@Min`, etc.)
- Mappers convert between Entity and DTO

### Mapper Pattern
- Separate mapper classes in `mapper/` package
- Not using MapStruct - simple, explicit mapping
- Three methods: `toDTO()`, `toEntity()`, `updateEntityFromDTO()`

### Service Interface Pattern
```java
// Interface defines contract
public interface ProductService {
    ProductDTO getProductById(UUID id);
}

// Implementation has business logic
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {
    // Implementation
}
```

## Working with Product Service

### Adding New Endpoint

1. **Add method to Service interface** (`ProductService.java`)
2. **Implement in Service implementation** (`ProductServiceImpl.java`)
   - Add `@Transactional` if it modifies data
   - Use repositories for data access
   - Convert entities to DTOs using mappers
3. **Add controller method** (`ProductController.java`)
   - Use `@PostMapping`, `@GetMapping`, etc.
   - Add OpenAPI annotations
   - Return `ApiResponse<T>`
4. **Test the endpoint**

### Adding New Entity Field

1. **Update Entity** (`Product.java` or `Category.java`)
   - Add field with JPA annotations
   - Add to builder if using `@Builder`
2. **Create Liquibase migration**
   - New file in `db/changelog/changes/`
   - `<addColumn>` changeset
3. **Update DTO** - Add field to corresponding DTO
4. **Update Mapper** - Include field in mapping methods
5. **Restart application** - Liquibase applies migration

## API Documentation

- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8081/api-docs`
- All endpoints documented with `@Operation` annotations

## Configuration

### Environment Variables
Service uses environment variables for configuration:
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`
- Defined in `.env.example`, override via `.env` file

### Application Profiles
- Default profile for local development
- Can add `application-dev.yml`, `application-prod.yml` for environments

### Important Properties
```yaml
spring.jpa.hibernate.ddl-auto: validate  # Never auto-create schema
spring.jpa.open-in-view: false          # Prevent lazy loading issues
spring.liquibase.enabled: true          # Auto-run migrations
```

## Testing Strategy

### Current State
Tests have not been written yet. When adding tests:

**Unit Tests** (Service layer):
- Mock repositories with Mockito
- Test business logic in isolation
- Example: Test `ProductService.reduceStock()` throws exception when insufficient stock

**Integration Tests** (Repository layer):
- Use `@DataJpaTest` with H2 in-memory database
- Test custom queries
- Example: Test `ProductRepository.findLowStockProducts()`

**API Tests** (Controller layer):
- Use `MockMvc` to test endpoints
- Test request/response mapping
- Test exception handling

## Common Patterns to Follow

### Creating New Service

1. Create package: `com.dukkan.{service}`
2. Create layers in order: model → repository → service → controller
3. Copy structure from product-service
4. Add Liquibase changelogs
5. Update docker-compose.yml and API Gateway routes

### SOLID Principles
- **Single Responsibility**: One class, one reason to change
- **Open/Closed**: Extend behavior via interfaces, don't modify
- **Liskov Substitution**: Service implementations are substitutable
- **Interface Segregation**: Small, focused interfaces
- **Dependency Inversion**: Depend on abstractions (interfaces)

### Simplicity (KISS)
- Avoid premature optimization
- No caching until proven needed
- No complex frameworks unless justified
- Straightforward, readable code over clever code

## Port Assignments

- Frontend: `5173` (development server)
- API Gateway: `8080` (main entry point)
- Product Service: `8081`
- PostgreSQL: `5432`

**Access Pattern**: User → Frontend (5173) → API Gateway (8080) → Product Service (8081) → Database

## API Gateway

### Overview
Spring Cloud Gateway routes all client requests to appropriate microservices. Uses reactive WebFlux stack.

### Key Components

**Global Filters:**
- `RequestHeaderFilter` - Adds correlation ID and gateway headers to all requests (highest priority)
- `LoggingGatewayFilter` - Logs all requests/responses with duration (lowest priority)

**Routes (configured in application.yml):**
```
/api/v1/products/**     → Product Service (localhost:8081)
/api/v1/categories/**   → Product Service (localhost:8081)
```

**Fallback Endpoints:**
- `/fallback/product-service` - Triggered when Product Service is down
- `/fallback/general` - General fallback for other services

**Health Checks:**
- `/health` - Gateway health status
- `/info` - Gateway information
- `/actuator/health` - Spring Boot Actuator health
- `/actuator/gateway/routes` - View all configured routes

### CORS Configuration
- Allowed Origins: `localhost:5173`, `localhost:3000` (configurable via `ALLOWED_ORIGINS`)
- Allowed Methods: GET, POST, PUT, DELETE, OPTIONS
- Credentials: Allowed

### Request Flow
```
Client Request
    ↓
API Gateway (Port 8080)
    ↓
RequestHeaderFilter (adds X-Correlation-Id, X-Gateway headers)
    ↓
Route Matching (/api/v1/products/** → localhost:8081)
    ↓
Product Service
    ↓
Response
    ↓
LoggingGatewayFilter (logs duration)
    ↓
Client Response
```

### Adding New Route
1. Edit `application.yml` under `spring.cloud.gateway.routes`
2. Add new route configuration:
   ```yaml
   - id: new-service
     uri: http://localhost:8082
     predicates:
       - Path=/api/v1/new/**
   ```
3. Optional: Add fallback in `FallbackController`
4. Restart gateway

## Frontend Architecture

### Overview
React 19 application with TypeScript, Mantine UI, and React Router. Uses Axios for API communication.

### Project Structure

```
frontend/src/
├── components/         # Reusable UI components
│   ├── layout/        # Header, Footer, etc.
│   └── products/      # Product-specific components
├── pages/             # Page components (routes)
├── services/          # API service layer
├── types/             # TypeScript definitions
├── App.tsx           # Main app with routing
└── main.tsx          # Entry point with providers
```

### Key Patterns

**API Service Pattern**:
```typescript
// Define types
export interface Product { ... }

// Create service
export const productService = {
  getAll: async () => { ... },
  getById: async (id: string) => { ... },
};

// Use in components
const products = await productService.getAll();
```

**Component Pattern**:
```typescript
interface MyComponentProps {
  prop1: string;
  prop2: number;
}

export const MyComponent = ({ prop1, prop2 }: MyComponentProps) => {
  const [state, setState] = useState(...);

  useEffect(() => {
    // Load data
  }, []);

  return <div>...</div>;
};
```

**Error Handling**:
- All API calls wrapped in try-catch
- Mantine notifications for user feedback
- Loading states for better UX

### Routes

- `/` - Home page
- `/products` - Product listing with search
- `/products/:id` - Product detail page
- `/categories` - Categories listing

### Adding New Features

1. **New API Endpoint**:
   - Add type to `src/types/`
   - Add service method to `src/services/`
   - Use in component

2. **New Page**:
   - Create page component in `src/pages/`
   - Add route to `App.tsx`
   - Add navigation link to `Header.tsx`

3. **New Component**:
   - Create in `src/components/`
   - Define TypeScript props interface
   - Export and use in pages

## Current Status

**Completed Features:**
- ✅ Backend: Product Service (29 Java classes, 28 endpoints)
- ✅ Backend: API Gateway (7 classes, routing, CORS, logging)
- ✅ Frontend: Customer pages (products, categories, cart)
- ✅ Frontend: Admin panel (product/category CRUD)
- ✅ Infrastructure: PostgreSQL with Docker, Liquibase migrations

**NOT Implemented (Priority for next session):**
- ❌ Testing (0% coverage - HIGH PRIORITY)
- ❌ Full Dockerization (only database containerized)
- ❌ User authentication
- ❌ Order management
- ❌ Payment integration
- ❌ Product images (using placeholders)

**See NEXT_STEPS.md for detailed roadmap**

## Key Files

- `NEXT_STEPS.md` - **START HERE** - Roadmap and priorities for next session
- `REQUIREMENTS.md` - User stories and business requirements
- `ARCHITECTURE.md` - Complete technical architecture
- `DEVELOPMENT.md` - Detailed development guidelines
- `GETTING_STARTED.md` - Quick start instructions
- `frontend/README.md` - Frontend-specific documentation
- `.claude/` - Claude Code commands and hooks

## Important Notes

### Database Constraints
- Product SKU must be unique
- Category name must be unique
- Cannot delete category with products or child categories
- Stock quantity cannot be negative

### Business Rules (in Domain Entities)
- `Product.reduceStock()` - Validates sufficient stock
- `Product.isLowStock()` - Returns true if stock ≤ 10
- `Category.isRootCategory()` - Checks if parent is null

### API Response Format
Success:
```json
{
  "success": true,
  "data": {...},
  "message": "...",
  "timestamp": "2025-11-13T10:00:00"
}
```

Error:
```json
{
  "success": false,
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "Product not found with id: '123'",
    "details": []
  },
  "timestamp": "2025-11-13T10:00:00"
}
```

## Development Workflow

### Full Stack Development

1. **Start Database**: `docker compose up -d postgres`
2. **Start Product Service**: `cd backend/product-service && mvn spring-boot:run` (port 8081)
3. **Start API Gateway**: `cd backend/api-gateway && mvn spring-boot:run` (port 8080)
4. **Start Frontend**: `cd frontend && npm run dev` (port 5173)
5. **Access Application**: `http://localhost:5173`
6. **Access API Docs**: `http://localhost:8081/swagger-ui.html`

### Making Changes

**Backend Changes**:
- Follow layered architecture (inside-out: domain → repository → service → controller)
- Update Liquibase migrations for schema changes
- Test via Swagger UI or write tests

**Frontend Changes**:
- Add types in `src/types/`
- Create services in `src/services/` for API calls
- Build components in `src/components/`
- Create pages in `src/pages/` and add routes to `App.tsx`

**Commit**: Follow conventional commit messages

## Future Considerations

When scaling/optimizing:
- Add Redis caching (`@Cacheable`, `@CacheEvict`)
- Add message queue for async operations (RabbitMQ/Kafka)
- Add service discovery (Eureka) if services grow
- Add centralized configuration (Spring Cloud Config)
- Add distributed tracing (Sleuth/Zipkin)

But only when metrics justify the added complexity.
