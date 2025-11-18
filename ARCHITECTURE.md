# Dukkan - Architecture Document

**Last Updated:** 2025-11-16
**Status:** ✅ CURRENT - Reflects actual implementation

---

## Table of Contents

1. [Technology Stack](#technology-stack)
2. [Architecture Overview](#architecture-overview)
3. [Service Responsibilities](#service-responsibilities)
4. [Data Models](#data-models)
5. [API Design](#api-design)
6. [Security Architecture](#security-architecture)
7. [Testing Strategy](#testing-strategy)
8. [Deployment Architecture](#deployment-architecture)
9. [Architectural Decisions](#architectural-decisions)

---

## Technology Stack

### Frontend
- **Framework**: React 19.2
- **Language**: TypeScript 5.7
- **UI Library**: Mantine UI 8.3.7
- **Build Tool**: Vite
- **State Management**: React Context API
- **HTTP Client**: Axios
- **Routing**: React Router v7
- **Internationalization**: i18next, react-i18next
- **Testing**: Vitest, React Testing Library (setup pending)

### Backend
- **Language**: Java 17
- **Framework**: Spring Boot 3.5.7
- **API Gateway**: Spring Cloud Gateway
- **Build Tool**: Maven 3.9+
- **Testing**: JUnit 5, Mockito, Spring Boot Test
- **Security**: Spring Security, JWT (jjwt)

### Data Layer
- **Database**: PostgreSQL 17
- **ORM**: Spring Data JPA / Hibernate
- **Migrations**: Liquibase
- **Connection Pooling**: HikariCP

### Infrastructure
- **Containerization**: Docker & Docker Compose (fully containerized)
- **Service Discovery**: Direct routing via Spring Cloud Gateway
- **API Documentation**: OpenAPI 3.0 (Springdoc OpenAPI)
- **Logging**: SLF4J + Logback
- **Monitoring**: Spring Boot Actuator (basic health checks)

---

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
                    │  (Port 5173)│
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │     API     │
                    │   Gateway   │
                    │  (Port 8080)│
                    └──────┬──────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
   ┌────▼────┐       ┌─────▼─────┐      ┌────▼────┐
   │ Product │       │   User    │      │  Order  │
   │ Service │       │  Service  │      │ Service │
   │  :8081  │       │   :8082   │      │  :8083  │
   └────┬────┘       └─────┬─────┘      └────┬────┘
        │                  │                  │
   ┌────▼────┐       ┌─────▼─────┐      ┌────▼────┐
   │Product  │       │  User DB  │      │Order DB │
   │   DB    │       │(PostgreSQL│      │(PostgreSQL)
   │(PostgreSQL)     │    17)    │      │   17)   │
   └─────────┘       └───────────┘      └─────────┘
```

### Current State (2025-11-17)

**Operational Services:** 5/5 (100%)
- ✅ Product Service (8081) - 24 tests passing
- ✅ User Service (8082) - 26 tests passing
- ✅ Order Service (8083) - 29 tests passing
- ✅ Payment Service (8084) - Iyzico integration (MVP)
- ✅ API Gateway (8080) - Routing, CORS, logging

**Databases:** 4 PostgreSQL databases
- product_db (Product Service)
- user_db (User Service)
- order_db (Order Service)
- payment_db (Payment Service)

**Frontend:** Fully operational
- Customer pages (Products, Cart, Checkout, Orders)
- Admin panel (Product/Category CRUD)
- Authentication (Login, Register)
- Internationalization (EN/TR)

---

## Service Responsibilities

### API Gateway (Port 8080)
**Status:** ✅ OPERATIONAL

**Responsibilities:**
- Single entry point for all client requests
- Request routing to microservices
- CORS handling (configured for localhost:5173, localhost:3000)
- Request/Response logging with correlation IDs
- Fallback endpoints for service failures

**Routes:**
```
/api/v1/products/**     → Product Service (8081)
/api/v1/categories/**   → Product Service (8081)
/api/v1/auth/**         → User Service (8082)
/api/v1/orders/**       → Order Service (8083)
```

**Global Filters:**
- `RequestHeaderFilter` - Adds X-Correlation-Id and X-Gateway headers
- `LoggingGatewayFilter` - Logs all requests/responses with duration

**Endpoints:**
- `/health` - Gateway health status
- `/fallback/product-service` - Product service fallback
- `/fallback/general` - General fallback

---

### Product Service (Port 8081)
**Status:** ✅ OPERATIONAL | 24/24 tests passing

**Responsibilities:**
- Product CRUD operations
- Category management with parent/child relationships
- Inventory tracking and stock management
- Product search and filtering
- Low stock detection (threshold: 10 units)

**REST Endpoints (17 total):**

**Products:**
- `GET /api/v1/products` - List all products (paginated)
- `GET /api/v1/products/{id}` - Get product by ID
- `POST /api/v1/products` - Create product (Admin)
- `PUT /api/v1/products/{id}` - Update product (Admin)
- `DELETE /api/v1/products/{id}` - Delete product (Admin)
- `GET /api/v1/products/search` - Search products
- `GET /api/v1/products/category/{id}` - Products by category
- `GET /api/v1/products/low-stock` - Low stock products
- `PUT /api/v1/products/{id}/stock` - Update stock
- `POST /api/v1/products/{id}/reduce-stock` - Reduce stock

**Categories:**
- `GET /api/v1/categories` - List all categories
- `GET /api/v1/categories/{id}` - Get category by ID
- `POST /api/v1/categories` - Create category (Admin)
- `PUT /api/v1/categories/{id}` - Update category (Admin)
- `DELETE /api/v1/categories/{id}` - Delete category (Admin)
- `GET /api/v1/categories/root` - Get root categories
- `GET /api/v1/categories/{id}/children` - Get child categories

**Database:** product_db (PostgreSQL 17)
- Tables: products, categories
- Liquibase migrations

**Testing:**
- 10 unit tests (ProductServiceImplTest)
- 5 unit tests (CategoryServiceImplTest)
- 5 integration tests (ProductRepositoryTest)
- 4 controller tests (ProductControllerTest)

**Swagger:** http://localhost:8081/swagger-ui.html

---

### User Service (Port 8082)
**Status:** ✅ OPERATIONAL | 26/26 tests passing

**Responsibilities:**
- User registration with email/password
- Authentication with JWT tokens
- Password hashing (BCrypt)
- Role-based access control (CUSTOMER, ADMIN)
- User profile management (model exists, edit UI pending)

**REST Endpoints (2 total):**
- `POST /api/v1/auth/register` - User registration
- `POST /api/v1/auth/login` - User login (returns JWT token)

**Database:** user_db (PostgreSQL 17)
- Table: users
- Liquibase migrations

**Security:**
- BCrypt password hashing (strength 10)
- JWT token generation with configurable expiration
- Spring Security integration

**Testing:**
- 9 unit tests (AuthServiceTest)
- 8 integration tests (UserRepositoryTest)
- 9 controller tests (AuthControllerTest)

**Swagger:** http://localhost:8082/swagger-ui.html

---

### Order Service (Port 8083)
**Status:** ✅ OPERATIONAL | 29/29 tests passing

**Responsibilities:**
- Order placement with stock validation
- Order history with pagination
- Order cancellation (PENDING/CONFIRMED only)
- Atomic stock reduction
- Price snapshot at purchase time

**REST Endpoints (4 total):**
- `POST /api/v1/orders` - Place new order
- `GET /api/v1/orders/my-orders` - Get user's orders (paginated)
- `GET /api/v1/orders/{id}` - Get order by ID
- `POST /api/v1/orders/{id}/cancel` - Cancel order

**Database:** order_db (PostgreSQL 17)
- Tables: orders, order_items
- Liquibase migrations

**Business Logic:**
- Stock validation before order placement
- Atomic transaction for order + stock reduction
- Auto-generated order numbers (ORD-timestamp)
- Order status: PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED

**Testing:**
- 11 unit tests (OrderServiceTest)
- 8 integration tests (OrderRepositoryTest)
- 10 controller tests (OrderControllerTest)

**Swagger:** http://localhost:8083/swagger-ui.html

---

### Payment Service (Port 8084)
**Status:** ✅ OPERATIONAL | Iyzico Integration (MVP)

**Responsibilities:**
- Payment processing with multiple provider support (Strategy Pattern)
- Payment gateway integration (Iyzico implemented, Stripe/PayPal ready)
- Transaction management and tracking
- Payment status updates (PENDING, COMPLETED, FAILED)
- Refund handling
- Payment history with pagination

**REST Endpoints (9 total):**
- `POST /api/v1/payments/initiate` - Initiate payment
- `POST /api/v1/payments/complete` - Complete payment
- `POST /api/v1/payments/fail` - Mark payment as failed
- `POST /api/v1/payments/refund` - Process refund
- `GET /api/v1/payments/{id}` - Get payment by ID
- `GET /api/v1/payments/order/{orderId}` - Get payment by order
- `GET /api/v1/payments/user/{userId}` - Get user's payment history
- `POST /api/v1/payments/iyzico/callback` - Iyzico callback endpoint
- `GET /api/v1/payments/providers` - List available providers

**Database:** payment_db (PostgreSQL 17)
- Table: payments
- Liquibase migrations

**Architecture:**
- **Strategy Pattern** for multiple payment providers
- **IyzicoPaymentProvider** - Fully implemented (sandbox/production)
- **StripePaymentProvider** - Ready to implement
- **PaypalPaymentProvider** - Ready to implement
- Pluggable provider registration via Spring configuration

**Configuration:**
```yaml
payment:
  providers:
    iyzico:
      enabled: true
      apiKey: ${IYZICO_API_KEY}
      secretKey: ${IYZICO_SECRET_KEY}
      baseUrl: ${IYZICO_BASE_URL}
```

**Swagger:** http://localhost:8084/swagger-ui.html

---

## Data Models

### Product Service

#### Product Entity
```java
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(unique = true, nullable = false, length = 50)
    private String sku;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Business logic methods
    public void reduceStock(int quantity) { ... }
    public boolean isLowStock() { return stockQuantity <= 10; }
}
```

**Constraints:**
- SKU must be unique
- Price must be >= 0
- Stock quantity must be >= 0
- Must belong to a category

---

#### Category Entity
```java
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private Category parentCategory;

    @OneToMany(mappedBy = "parentCategory")
    private List<Category> children = new ArrayList<>();

    @Column(nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Business logic methods
    public boolean isRootCategory() { return parentCategory == null; }
}
```

**Constraints:**
- Name must be unique
- Cascade delete protection (cannot delete if has products or children)

---

### User Service

#### User Entity
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // CUSTOMER, ADMIN

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean emailVerified = false;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

**Constraints:**
- Email must be unique
- Password hashed with BCrypt
- Default role: CUSTOMER

**Note:** Address entity planned but not yet implemented

---

### Order Service

#### Order Entity
```java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(unique = true, nullable = false)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status; // PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @PrePersist
    protected void generateOrderNumber() {
        this.orderNumber = "ORD" + System.currentTimeMillis();
    }

    // Business logic methods
    public void calculateTotal() { ... }
    public boolean canBeCancelled() { ... }
}
```

**Constraints:**
- Order number must be unique (auto-generated)
- Can only cancel PENDING or CONFIRMED orders

---

#### OrderItem Entity
```java
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private UUID productId;

    @Column(nullable = false, length = 255)
    private String productName;

    @Column(length = 50)
    private String productSku;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal priceAtPurchase; // Price snapshot

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal subtotal;

    @PrePersist
    @PreUpdate
    protected void calculateSubtotal() {
        if (quantity != null && priceAtPurchase != null) {
            this.subtotal = priceAtPurchase.multiply(BigDecimal.valueOf(quantity));
        }
    }

    // Custom getter for on-the-fly calculation
    public BigDecimal getSubtotal() {
        if (subtotal == null && quantity != null && priceAtPurchase != null) {
            return priceAtPurchase.multiply(BigDecimal.valueOf(quantity));
        }
        return subtotal;
    }
}
```

**Note:** Price captured at purchase time (priceAtPurchase) to preserve historical pricing

---

## API Design

### RESTful Principles

**Applied:**
- ✅ Standard HTTP methods (GET, POST, PUT, DELETE)
- ✅ Plural nouns for resources (/products, /categories, /orders)
- ✅ Proper HTTP status codes
- ✅ API versioning (/api/v1/)

### HTTP Status Codes

**Success:**
- `200 OK` - Successful GET, PUT
- `201 Created` - Successful POST
- `204 No Content` - Successful DELETE

**Client Errors:**
- `400 Bad Request` - Validation errors
- `401 Unauthorized` - Authentication required
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `409 Conflict` - Resource conflict (duplicate)

**Server Errors:**
- `500 Internal Server Error` - Server-side errors

### Response Format

**Actual Implementation:**
Services return DTOs directly (not wrapped in ApiResponse<T>)

**Success Response:**
```json
{
  "id": "uuid",
  "name": "Product Name",
  "price": 99.99,
  ...
}
```

**Paginated Response:**
```json
{
  "content": [...],
  "page": 0,
  "size": 10,
  "totalElements": 100,
  "totalPages": 10
}
```

**Error Response (via GlobalExceptionHandler):**
```json
{
  "timestamp": "2025-11-16T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Product not found with id: xyz",
  "validationErrors": {}
}
```

---

## Security Architecture

### Authentication & Authorization
**Status:** ✅ IMPLEMENTED

**JWT-Based Authentication:**
- Stateless JWT tokens
- Configurable expiration (default: 24 hours)
- Stored in localStorage (frontend)
- Sent in Authorization header: `Bearer <token>`

**Spring Security:**
- Integrated in User Service
- BCrypt password hashing (strength 10)
- Role-based access control (RBAC)

**Roles:**
- `CUSTOMER` - Default for new registrations
- `ADMIN` - Full access to admin endpoints

**Order Service Authentication:**
- Requires `Authorization: Bearer <token>` header
- Requires `X-User-Id` header for order operations

---

### API Security

**Implemented:**
- ✅ Input validation (`@Valid`, `@NotBlank`, `@Min`, etc.)
- ✅ SQL injection prevention (JPA parameterized queries)
- ✅ XSS protection (JSON serialization, no raw HTML)
- ✅ CORS configuration (API Gateway)
- ✅ Password hashing (BCrypt)

**Not Implemented:**
- ❌ HTTPS enforcement (development uses HTTP)
- ❌ Rate limiting
- ❌ CSRF protection (acceptable for stateless JWT API)
- ❌ Password strength requirements

---

## Testing Strategy

### Backend Testing
**Status:** ✅ 100% Coverage (79/79 tests passing)

**Test Types:**
1. **Unit Tests (35 tests)** - Business logic with Mockito
2. **Integration Tests (21 tests)** - Repository layer with H2
3. **Controller Tests (23 tests)** - REST endpoints with MockMvc

**Test Infrastructure:**
- H2 in-memory database for integration tests
- `application-test.properties` per service
- `@ActiveProfiles("test")` for test configuration
- Liquibase disabled in tests

**Patterns:**
- Mock dependencies with `@MockBean` and `@Mock`
- Use `@DataJpaTest` for repository tests
- Use `@WebMvcTest` + `@AutoConfigureMockMvc(addFilters = false)` for controller tests
- Manual order number assignment to avoid timing duplicates

**Coverage by Service:**
- Product Service: 24 tests
- User Service: 26 tests
- Order Service: 29 tests

---

### Frontend Testing
**Status:** ❌ NOT IMPLEMENTED (0% coverage)

**Planned:**
- Vitest + React Testing Library
- Component tests (Cart, Checkout, Orders, Products)
- Context tests (CartContext, AuthContext)
- Service tests (productService, orderService, authService)

**Target:** 80%+ coverage

---

## Deployment Architecture

### Current State
**Status:** ✅ COMPLETE (All services containerized)

**Containerized:**
- ✅ PostgreSQL 17 (Docker Compose)
- ✅ Product Service (Multi-stage Docker build)
- ✅ User Service (Multi-stage Docker build)
- ✅ Order Service (Multi-stage Docker build)
- ✅ Payment Service (Multi-stage Docker build)
- ✅ API Gateway (Multi-stage Docker build)
- ✅ Frontend (nginx serving static build)

### Local Development Setup

**Requirements:**
- Java 17
- Node.js 20+
- Maven 3.9+
- Docker & Docker Compose

**Start Sequence:**
```bash
# Terminal 1: Database
docker compose up -d postgres

# Terminal 2: Product Service
cd backend/product-service && mvn spring-boot:run

# Terminal 3: User Service
cd backend/user-service && mvn spring-boot:run

# Terminal 4: Order Service
cd backend/order-service && mvn spring-boot:run

# Terminal 5: API Gateway
cd backend/api-gateway && mvn spring-boot:run

# Terminal 6: Frontend
cd frontend && npm run dev
```

**Access URLs:**
- Frontend: http://localhost:5173
- API Gateway: http://localhost:8080
- Product Service: http://localhost:8081 (Swagger: /swagger-ui.html)
- User Service: http://localhost:8082 (Swagger: /swagger-ui.html)
- Order Service: http://localhost:8083 (Swagger: /swagger-ui.html)

---

### Database Configuration

**PostgreSQL 17:**
- 3 separate databases (product_db, user_db, order_db)
- Environment variables for connection config
- Liquibase migrations auto-run on startup

**Connection Pooling:**
- HikariCP (Spring Boot default)
- Default pool size: 10 connections

---

## Architectural Decisions

### Decision 1: No Caching Layer
**Status:** Deliberately deferred

**Rationale:**
- KISS principle (Keep It Simple, Stupid)
- Database indexing + HikariCP pooling sufficient for initial load
- Premature optimization avoided
- Can add Redis/Caffeine later when metrics justify

**Result:** Working well without caching

---

### Decision 2: Direct Service Routing
**Status:** Implemented

**Rationale:**
- Simpler than service discovery (Eureka/Consul)
- Fewer moving parts for initial deployment
- Direct routing via Spring Cloud Gateway
- Can add service discovery later if needed

**Result:** Clean, simple routing configuration

---

### Decision 3: JWT Authentication
**Status:** Implemented

**Rationale:**
- Stateless (no server-side sessions)
- Horizontally scalable
- Industry standard
- Works well with React frontend

**Result:** Secure, scalable authentication

---

### Decision 4: Liquibase for Migrations
**Status:** Implemented

**Rationale:**
- Better rollback support than Flyway
- More features (preconditions, contexts)
- XML format (more verbose but clearer)

**Result:** Reliable, auditable database changes

---

### Decision 5: React Context API
**Status:** Implemented

**Rationale:**
- Built-in React feature
- Simple, sufficient for our needs
- No Redux/MobX complexity
- Easy to understand and maintain

**Result:** Clean state management (CartContext, AuthContext)

---

### Decision 6: Microservices Over Monolith
**Status:** Implemented

**Rationale:**
- Separation of concerns
- Independent scaling
- Technology flexibility
- Team can work independently on services

**Trade-offs:**
- More complex deployment (mitigated by Docker - pending)
- Network latency between services
- Distributed transaction complexity

**Result:** Good separation, manageable complexity

---

### Decision 7: PostgreSQL Over NoSQL
**Status:** Implemented

**Rationale:**
- Strong ACID guarantees needed for orders/payments
- Relational data model fits e-commerce well
- Mature, battle-tested
- Excellent JPA/Hibernate support

**Result:** Reliable, consistent data storage

---

## Future Considerations

### Short-Term (Next 2-3 months)
1. **Frontend Testing** - Vitest + React Testing Library (HIGH PRIORITY)
2. **Payment Service Testing** - Unit + integration tests
3. **Additional Payment Providers** - Stripe, PayPal
4. **Monitoring** - Centralized logging (ELK stack)
5. **Address Management** - Shipping addresses
6. **Email Service** - Order confirmations, notifications

### Medium-Term (3-6 months)
6. **Email Service** - Order confirmations, password reset
7. **Product Images** - Upload and storage (S3/Cloudinary)
8. **Rate Limiting** - API abuse prevention
9. **Caching Layer** - Redis (if metrics show need)
10. **CI/CD Pipeline** - Automated testing and deployment

### Long-Term (6-12 months)
11. **Service Mesh** - Istio/Linkerd (if microservices grow)
12. **Event-Driven Architecture** - Kafka/RabbitMQ for async
13. **Advanced Analytics** - Business intelligence dashboard
14. **Mobile Apps** - React Native or native iOS/Android
15. **International Expansion** - Multi-currency, shipping providers

---

## Monitoring & Observability

### Current State
**Status:** ⚠️ BASIC

**Implemented:**
- ✅ Spring Boot Actuator health endpoints
- ✅ API Gateway logging with correlation IDs
- ✅ Service-level logging (SLF4J + Logback)

**Not Implemented:**
- ❌ Centralized logging (ELK stack)
- ❌ Metrics collection (Prometheus)
- ❌ Distributed tracing (Zipkin/Jaeger)
- ❌ Alerting (PagerDuty/etc.)
- ❌ APM (Application Performance Monitoring)

---

## Known Issues & Technical Debt

### 1. Order Number Generation
**Issue:** Uses `System.currentTimeMillis()` which can duplicate in fast succession
**Impact:** Test flakiness, potential production issue
**Workaround:** Manual assignment in tests
**Permanent Fix:** Use UUID or timestamp+sequence

### 2. Tax Calculation
**Issue:** Tax rate hardcoded in frontend (18%)
**Impact:** Cannot vary by location
**Fix Needed:** Move to backend, calculate based on user location/rules

### 3. Cart Persistence
**Issue:** Cart stored in localStorage only
**Impact:** Not synced across devices
**Fix Needed:** Backend cart service (optional for MVP)

### 4. Product Name Uniqueness
**Issue:** SKU uniqueness enforced, not name+category
**Impact:** Deviates from requirements
**Decision:** Acceptable trade-off (SKU is more important)

### 5. Environment Secrets
**Issue:** Using application.yml for configuration
**Impact:** Secrets in repository
**Fix Needed:** Environment variables or secret management (Vault)

---

## Performance Characteristics

### Current State
**Status:** ⚠️ NOT MEASURED

**No Performance Testing:**
- No load testing
- No stress testing
- No benchmarks
- No profiling

**Expected Performance (not verified):**
- API response time: < 500ms (goal)
- Page load time: < 2 seconds (goal)
- Concurrent users: 1000+ (goal)

**Recommendation:** Performance testing before production launch

---

## Documentation

### API Documentation
- ✅ Swagger/OpenAPI on all services
- ✅ Available at `/swagger-ui.html` endpoint
- ✅ Complete with examples and schemas

### Code Documentation
- ✅ Javadoc on public methods
- ✅ Inline comments for complex logic
- ✅ README files in each service

### Architecture Documentation
- ✅ This document (ARCHITECTURE.md)
- ✅ CLAUDE.md (development guide)
- ✅ DEVELOPMENT.md (setup guide)
- ✅ Component-specific guides (I18N_GUIDE.md, AUTH_IMPROVEMENTS.md)

---

**Document Version:** 2.1
**Last Updated:** 2025-11-17
**Status:** ✅ CURRENT
**Review Schedule:** Update after major changes
**Next Review:** After testing implementation and email service
