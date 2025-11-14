# Dukkan - Architecture Document

## Technology Stack

### Frontend
- **Framework**: React 19.2
- **Language**: TypeScript
- **UI Library**: Mantine UI 8.3.7
- **Build Tool**: Vite
- **State Management**: React Context API
- **HTTP Client**: Axios
- **Routing**: React Router v7
- **Form Handling**: Mantine Form (built-in with Mantine UI)
- **Testing**: Vitest, React Testing Library

### Backend
- **Language**: Java 25
- **Framework**: Spring Boot 3.5.7
- **API Gateway**: Spring Cloud Gateway
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito, Spring Boot Test

### Data Layer
- **Database**: PostgreSQL 17
- **ORM**: Spring Data JPA / Hibernate
- **Migrations**: Liquibase

### Infrastructure
- **Containerization**: Docker & Docker Compose
- **Service Discovery**: Direct routing (Spring Cloud Gateway)
- **Configuration**: Spring Cloud Config (optional for future)
- **API Documentation**: OpenAPI 3.0 (Springdoc OpenAPI)
- **Logging**: SLF4J + Logback
- **Monitoring**: Spring Boot Actuator (Prometheus/Grafana for future)

## Architecture Overview

### Microservices Architecture

```
                                    ┌─────────────┐
                                    │   Browser   │
                                    └──────┬──────┘
                                           │
                                    ┌──────▼──────┐
                                    │   React     │
                                    │  Frontend   │
                                    └──────┬──────┘
                                           │
                                    ┌──────▼──────┐
                                    │     API     │
                                    │   Gateway   │
                                    └──────┬──────┘
                                           │
                    ┌──────────────────────┼──────────────────────┐
                    │                      │                      │
             ┌──────▼──────┐        ┌─────▼─────┐        ┌──────▼──────┐
             │   Product   │        │   User    │        │   Order     │
             │   Service   │        │  Service  │        │  Service    │
             │             │        │ (Future)  │        │  (Future)   │
             └──────┬──────┘        └─────┬─────┘        └──────┬──────┘
                    │                     │                      │
         ┌──────────┼─────────┐           │                      │
         │          │         │           │                      │
  ┌──────▼──────┐  │  ┌──────▼──────┐    │                      │
  │  Product DB │  │  │    Redis    │    │                      │
  │ (PostgreSQL)│  │  │   (Cache)   │    │                      │
  └─────────────┘  │  └─────────────┘    │                      │
                   │                      │                      │
            ┌──────▼──────┐        ┌─────▼─────┐        ┌──────▼──────┐
            │  User DB    │        │  Order DB │        │   More...   │
            │  (Future)   │        │  (Future) │        │             │
            └─────────────┘        └───────────┘        └─────────────┘
```

### Service Responsibilities

#### API Gateway (Port: 8080)
- Single entry point for all client requests
- Request routing to appropriate microservices
- Load balancing
- Authentication/Authorization (future)
- Rate limiting
- CORS handling
- Request/Response logging

#### Product Service (Port: 8081)
- Product CRUD operations
- Product catalog management
- Category management
- Inventory tracking
- Product search and filtering
- Image management

#### User Service (Future - Port: 8082)
- User registration and authentication
- Profile management
- Password management
- Role-based access control

#### Order Service (Future - Port: 8083)
- Shopping cart management
- Order creation and management
- Order history
- Order status tracking

#### Payment Service (Future - Port: 8084)
- Payment processing
- Transaction management
- Payment gateway integration
- Refund handling

## Data Models

### Product Service

#### Product Entity
```java
Product {
  id: UUID (PK)
  name: String (required, max 255)
  description: String (text)
  price: BigDecimal (required, min 0)
  stockQuantity: Integer (required, min 0)
  sku: String (unique, required)
  categoryId: UUID (FK)
  imageUrls: List<String>
  isActive: Boolean (default true)
  createdAt: Timestamp
  updatedAt: Timestamp
}
```

#### Category Entity
```java
Category {
  id: UUID (PK)
  name: String (required, unique, max 100)
  description: String
  parentCategoryId: UUID (FK, nullable - for nested categories)
  isActive: Boolean (default true)
  createdAt: Timestamp
  updatedAt: Timestamp
}
```

### User Service (Future)

#### User Entity
```java
User {
  id: UUID (PK)
  email: String (required, unique)
  passwordHash: String (required)
  firstName: String (required)
  lastName: String (required)
  phoneNumber: String
  role: Enum (CUSTOMER, ADMIN)
  isActive: Boolean (default true)
  emailVerified: Boolean (default false)
  createdAt: Timestamp
  updatedAt: Timestamp
}
```

#### Address Entity
```java
Address {
  id: UUID (PK)
  userId: UUID (FK)
  addressLine1: String (required)
  addressLine2: String
  city: String (required)
  state: String
  postalCode: String (required)
  country: String (required)
  isDefault: Boolean (default false)
  createdAt: Timestamp
  updatedAt: Timestamp
}
```

### Order Service (Future)

#### Order Entity
```java
Order {
  id: UUID (PK)
  userId: UUID (FK)
  orderNumber: String (unique, generated)
  status: Enum (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
  totalAmount: BigDecimal (required)
  shippingAddressId: UUID (FK)
  paymentId: UUID (FK)
  createdAt: Timestamp
  updatedAt: Timestamp
}
```

#### OrderItem Entity
```java
OrderItem {
  id: UUID (PK)
  orderId: UUID (FK)
  productId: UUID (FK)
  quantity: Integer (required, min 1)
  priceAtPurchase: BigDecimal (required)
  subtotal: BigDecimal (calculated)
}
```

## API Design

### RESTful Principles
- Use standard HTTP methods (GET, POST, PUT, DELETE)
- Use plural nouns for resource names
- Use proper HTTP status codes
- Version APIs (e.g., /api/v1/)

### API Gateway Routes

#### Product Service Routes
```
GET    /api/v1/products              - List all products (paginated)
GET    /api/v1/products/{id}         - Get product by ID
POST   /api/v1/products              - Create new product (Admin)
PUT    /api/v1/products/{id}         - Update product (Admin)
DELETE /api/v1/products/{id}         - Delete product (Admin)
GET    /api/v1/products/search       - Search products
GET    /api/v1/products/category/{categoryId} - Products by category

GET    /api/v1/categories            - List all categories
GET    /api/v1/categories/{id}       - Get category by ID
POST   /api/v1/categories            - Create category (Admin)
PUT    /api/v1/categories/{id}       - Update category (Admin)
DELETE /api/v1/categories/{id}       - Delete category (Admin)
```

### Response Format

#### Success Response
```json
{
  "success": true,
  "data": { ... },
  "message": "Operation successful"
}
```

#### Error Response
```json
{
  "success": false,
  "error": {
    "code": "PRODUCT_NOT_FOUND",
    "message": "Product with ID xyz not found",
    "details": []
  },
  "timestamp": "2025-11-13T10:00:00Z"
}
```

#### Paginated Response
```json
{
  "success": true,
  "data": {
    "items": [...],
    "pagination": {
      "page": 1,
      "size": 20,
      "totalPages": 5,
      "totalElements": 100
    }
  }
}
```

## Communication Patterns

### Synchronous Communication
- REST APIs for client-to-gateway and gateway-to-service communication
- HTTP/HTTPS protocol

### Asynchronous Communication (Future)
- Event-driven architecture using message queues
- RabbitMQ or Apache Kafka for inter-service events
- Example events: OrderPlaced, ProductOutOfStock, PaymentCompleted

## Security Architecture

### Authentication & Authorization (Future)
- JWT-based authentication
- Spring Security integration
- Role-based access control (RBAC)
- Token refresh mechanism

### API Security
- HTTPS only in production
- API rate limiting
- Input validation and sanitization
- SQL injection prevention (parameterized queries)
- XSS protection
- CSRF protection for state-changing operations

### Data Security
- Passwords hashed with BCrypt
- Sensitive data encrypted at rest
- Environment-based configuration (no secrets in code)
- Database connection credentials via environment variables

## Error Handling Strategy

### Global Exception Handling
- Centralized exception handlers in each service
- Consistent error response format
- Appropriate HTTP status codes
- Detailed logging for debugging

### Common HTTP Status Codes
- 200 OK - Successful GET, PUT
- 201 Created - Successful POST
- 204 No Content - Successful DELETE
- 400 Bad Request - Validation errors
- 401 Unauthorized - Authentication required
- 403 Forbidden - Insufficient permissions
- 404 Not Found - Resource not found
- 409 Conflict - Resource conflict (e.g., duplicate)
- 500 Internal Server Error - Server-side errors

## Logging Strategy

### Log Levels
- ERROR: Application errors, exceptions
- WARN: Potential issues, deprecated usage
- INFO: Important business events (order created, payment processed)
- DEBUG: Detailed information for debugging
- TRACE: Very detailed diagnostic information

### Log Format
- Structured logging (JSON format preferred)
- Include correlation ID for request tracing
- Log user actions for audit trail
- Avoid logging sensitive data (passwords, tokens)

## Testing Strategy

### Backend Testing
- **Unit Tests**: Test individual methods and classes (JUnit 5, Mockito)
- **Integration Tests**: Test service interactions (Spring Boot Test)
- **API Tests**: Test REST endpoints (MockMvc, RestAssured)
- **Contract Tests**: Verify API contracts between services

### Frontend Testing
- **Unit Tests**: Test components and utilities (Vitest)
- **Integration Tests**: Test component interactions (React Testing Library)
- **E2E Tests**: Test complete user flows (Playwright or Cypress - future)

### Test Coverage Goals
- Minimum 80% code coverage for services
- 100% coverage for critical business logic
- All API endpoints must have tests

## Deployment Architecture

### Docker Containers
- Each service runs in its own container
- Docker Compose for local development
- Container orchestration (Kubernetes - future consideration)

### Environment Configuration
- Development (local)
- Staging (pre-production)
- Production

### CI/CD Pipeline (Future)
- Automated builds on commit
- Run tests before deployment
- Automated deployment to staging
- Manual approval for production
- Rollback capability

## Scalability Considerations

### Horizontal Scaling
- Stateless services for easy scaling
- Load balancing via API Gateway
- Database read replicas (future)

### Caching Strategy
- No caching layer initially (KISS principle - Keep It Simple)
- Can add Redis or Caffeine later when performance optimization is needed
- Database query optimization and proper indexing will handle initial load

### Database Optimization
- Proper indexing on frequently queried columns
- Connection pooling
- Query optimization
- Database sharding (future consideration)

## Monitoring & Observability (Future)

### Health Checks
- Spring Boot Actuator endpoints
- Service health status
- Database connectivity checks

### Metrics
- Request rates and response times
- Error rates
- Service resource usage (CPU, memory)
- Business metrics (orders, revenue)

### Distributed Tracing
- Request tracing across services
- OpenTelemetry or similar

## Development Workflow

### Git Workflow
- Main branch for production-ready code
- Feature branches for new development
- Pull requests for code review
- Commit message conventions

### Code Standards
- Java: Google Java Style Guide
- TypeScript/React: Airbnb Style Guide (or similar)
- ESLint and Prettier for frontend
- Checkstyle or SpotBugs for backend

## Decisions Made

1. **Database**: PostgreSQL 17 - Robust, feature-rich, excellent performance
2. **State Management**: React Context API - Simple, built-in, sufficient for our needs
3. **Service Discovery**: Direct routing via Spring Cloud Gateway - No need for Eureka/Consul initially
4. **Caching**: Deferred - Will add when performance optimization is needed (KISS principle)
5. **Form Handling**: Mantine Form - Built-in with Mantine UI, well-integrated
6. **Database Migrations**: Liquibase - More features, better rollback support
7. **Message Queue**: Deferred - Will add RabbitMQ or Kafka when async communication is needed
8. **Hosting**: TBD - Can be deployed anywhere (AWS, GCP, Azure, or on-premises)
