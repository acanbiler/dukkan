---
name: backend-dev-guidelines
description: Java 25 and Spring Boot 3.5.7 microservices development for Dukkan. Use when creating or modifying REST APIs, controllers, services, repositories, entities, DTOs, mappers, exception handlers, database migrations (Liquibase), JPA queries, PostgreSQL schemas, validation logic, or implementing layered architecture following SOLID and DDD principles.
---

# Backend Development Guidelines for Dukkan

Expert guidance for developing Spring Boot microservices in the Dukkan e-commerce SaaS platform.

## Tech Stack Overview

- **Java 25** with modern language features
- **Spring Boot 3.5.7**
- **Spring Cloud Gateway 2024.0.0** for API routing
- **PostgreSQL 17** (production) with **Liquibase** migrations
- **H2** for testing
- **Lombok** for boilerplate reduction
- **SpringDoc OpenAPI 2.7.0** for API documentation
- **Maven** for builds

## Core Architecture Principles

### 1. SOLID Principles (Mandatory)
- **Single Responsibility**: One class, one reason to change
- **Open/Closed**: Extend via interfaces, don't modify existing
- **Liskov Substitution**: Service implementations are interchangeable
- **Interface Segregation**: Small, focused interfaces
- **Dependency Inversion**: Depend on abstractions (interfaces), use constructor injection

### 2. KISS Principle (Keep It Simple, Stupid)
- **No premature optimization** - Add complexity only when metrics justify it
- **No caching layer initially** - Database indexing + HikariCP handles load
- **Straightforward code over clever code**
- Example: Redis was intentionally removed for simplicity

### 3. Domain-Driven Design
- Rich domain entities with behavior (NOT anemic)
- Business logic lives in domain objects
- Entities have methods like `reduceStock()`, `isLowStock()`

## Layered Architecture (Inside-Out Development)

**Dependency Flow** (always flows inward):
```
Controller → Service Interface → Service Impl → Repository → Database
    ↓              ↓                    ↓
  DTOs         Mappers         Domain Entities
```

### Layer 1: Domain (Core Business Logic)

**Location**: `src/main/java/com/dukkan/{service}/model/`

**Rules**:
- Entities contain business behavior, not just data
- Use JPA annotations for persistence
- Use Lombok `@Builder`, `@Getter`, `@Setter`
- Avoid bidirectional relationships unless necessary

**Example**:
```java
@Entity
@Table(name = "products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false, length = 50)
    private String sku;

    @Column(nullable = false)
    private Integer stockQuantity;

    // Business logic in domain (NOT anemic)
    public void reduceStock(Integer quantity) {
        if (stockQuantity < quantity) {
            throw new InvalidOperationException("Insufficient stock");
        }
        this.stockQuantity -= quantity;
    }

    public boolean isLowStock() {
        return stockQuantity != null && stockQuantity <= 10;
    }
}
```

### Layer 2: Repository (Data Access)

**Location**: `src/main/java/com/dukkan/{service}/repository/`

**Rules**:
- Extend `JpaRepository<Entity, UUID>`
- Return `Optional<T>` for single results
- Return `Page<T>` for paginated results
- Use method naming or `@Query` for custom queries

**Example**:
```java
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findBySku(String sku);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= :threshold")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);

    boolean existsBySku(String sku);
}
```

### Layer 3: Service (Business Orchestration)

**Location**: `src/main/java/com/dukkan/{service}/service/`

**Pattern**: Interface + Implementation

**Rules**:
- Services return DTOs, never entities
- Use `@Transactional(readOnly = true)` at class level
- Override with `@Transactional` for write methods
- Throw custom exceptions (don't return null)
- Keep thin - delegate to domain entities

**Example Interface**:
```java
public interface ProductService {
    ProductDTO getProductById(UUID id);
    ProductDTO createProduct(CreateProductRequest request);
    Page<ProductDTO> getAllProducts(int page, int size);
}
```

**Example Implementation**:
```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductDTO getProductById(UUID id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        return productMapper.toDTO(product);
    }

    @Override
    @Transactional
    public ProductDTO createProduct(CreateProductRequest request) {
        // Validation
        if (productRepository.existsBySku(request.getSku())) {
            throw new DuplicateResourceException(
                "Product with SKU " + request.getSku() + " already exists"
            );
        }

        Product product = productMapper.toEntity(request);
        Product saved = productRepository.save(product);
        log.info("Created product: {}", saved.getId());
        return productMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public void reduceStock(UUID productId, Integer quantity) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        // Business logic delegated to domain
        product.reduceStock(quantity);
        // No explicit save - managed entity updates automatically
    }
}
```

### Layer 4: Controller (REST API)

**Location**: `src/main/java/com/dukkan/{service}/controller/`

**Rules**:
- Thin controllers - only HTTP concerns
- Return `ResponseEntity<ApiResponse<T>>`
- Use `@Valid` for validation
- Add OpenAPI annotations

**Example**:
```java
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product management")
public class ProductController {
    private final ProductService productService;

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductDTO>> getProduct(@PathVariable UUID id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(
            ApiResponse.success(product, "Product retrieved successfully")
        );
    }

    @PostMapping
    @Operation(summary = "Create new product")
    public ResponseEntity<ApiResponse<ProductDTO>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        ProductDTO product = productService.createProduct(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(product, "Product created successfully"));
    }
}
```

## DTOs and Mappers

### DTOs
**Location**: `src/main/java/com/dukkan/{service}/dto/`

**Rules**:
- Separate request and response DTOs
- Use validation annotations
- Can use Java records for immutability

```java
// Request DTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {
    @NotBlank(message = "SKU is required")
    @Size(max = 50)
    private String sku;

    @NotBlank(message = "Name is required")
    @Size(max = 255)
    private String name;

    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stockQuantity = 0;
}

// Response DTO (Java record)
public record ProductDTO(
    UUID id,
    String sku,
    String name,
    BigDecimal price,
    Integer stockQuantity,
    LocalDateTime createdAt
) {}
```

### Mappers
**Location**: `src/main/java/com/dukkan/{service}/mapper/`

**Rules**:
- Manual mapping (no MapStruct for simplicity)
- Three methods: `toDTO()`, `toEntity()`, `updateEntity()`

```java
@Component
public class ProductMapper {
    public ProductDTO toDTO(Product entity) {
        return new ProductDTO(
            entity.getId(),
            entity.getSku(),
            entity.getName(),
            entity.getPrice(),
            entity.getStockQuantity(),
            entity.getCreatedAt()
        );
    }

    public Product toEntity(CreateProductRequest dto) {
        return Product.builder()
            .sku(dto.getSku())
            .name(dto.getName())
            .stockQuantity(dto.getStockQuantity())
            .build();
    }
}
```

## Exception Handling

### Custom Exceptions
**Location**: `src/main/java/com/dukkan/{service}/exception/`

```java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, UUID id) {
        super(String.format("%s not found with id: %s", resource, id));
    }
}

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}

public class InvalidOperationException extends RuntimeException {
    public InvalidOperationException(String message) {
        super(message);
    }
}
```

### Global Exception Handler
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
            ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage());
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error("RESOURCE_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
            MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .toList();
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("VALIDATION_ERROR", "Validation failed", errors));
    }
}
```

## Database Migrations (Liquibase)

**Location**: `src/main/resources/db/changelog/`

**Structure**:
- `db.changelog-master.xml` - Master file
- `changes/001-create-products-table.xml` - Incremental changes

**Critical Rules**:
- NEVER use `spring.jpa.hibernate.ddl-auto=create` or `update`
- Always use `validate` - let Liquibase manage schema
- Create new file for each change
- Include in master changelog

**Example Migration**:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                   http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.20.xsd">

    <changeSet id="001" author="dukkan">
        <createTable tableName="products">
            <column name="id" type="UUID">
                <constraints primaryKey="true" nullable="false"/>
            </column>
            <column name="sku" type="VARCHAR(50)">
                <constraints unique="true" nullable="false"/>
            </column>
            <column name="stock_quantity" type="INT" defaultValue="0">
                <constraints nullable="false"/>
            </column>
        </createTable>

        <createIndex indexName="idx_products_sku" tableName="products">
            <column name="sku"/>
        </createIndex>
    </changeSet>
</databaseChangeLog>
```

## API Response Format (Consistent Wrapper)

```java
@Getter
@Setter
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private ErrorResponse error;
    private LocalDateTime timestamp;

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        ErrorResponse error = new ErrorResponse(code, message, List.of());
        return new ApiResponse<>(false, null, null, error, LocalDateTime.now());
    }
}
```

## Configuration (application.yml)

```yaml
spring:
  application:
    name: product-service

  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:dukkan_product}
    username: ${DB_USER:dukkan}
    password: ${DB_PASSWORD:dukkan123}

  jpa:
    hibernate:
      ddl-auto: validate  # CRITICAL: Never create/update
    open-in-view: false   # Prevent lazy loading issues
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

  liquibase:
    enabled: true
    change-log: classpath:db/changelog/db.changelog-master.xml

server:
  port: ${SERVER_PORT:8081}
```

## Testing Strategy

### Unit Tests (Service Layer)
```java
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void getProductById_WhenExists_ReturnsProduct() {
        UUID id = UUID.randomUUID();
        Product product = Product.builder().id(id).build();
        ProductDTO dto = new ProductDTO(id, "SKU1", "Product", ...);

        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productMapper.toDTO(product)).thenReturn(dto);

        ProductDTO result = productService.getProductById(id);

        assertThat(result).isEqualTo(dto);
        verify(productRepository).findById(id);
    }
}
```

### Integration Tests (Repository)
```java
@DataJpaTest
class ProductRepositoryTest {
    @Autowired
    private ProductRepository productRepository;

    @Test
    void findBySku_Success() {
        Product product = Product.builder()
            .sku("TEST-SKU")
            .name("Test Product")
            .build();
        productRepository.save(product);

        Optional<Product> found = productRepository.findBySku("TEST-SKU");

        assertThat(found).isPresent();
    }
}
```

## Common Mistakes to Avoid

❌ **Don't expose entities in controllers**
```java
// Bad
@GetMapping("/{id}")
public Product getProduct(@PathVariable UUID id) {
    return productRepository.findById(id).orElseThrow();
}
```

✅ **Use DTOs and services**
```java
// Good
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<ProductDTO>> getProduct(@PathVariable UUID id) {
    ProductDTO product = productService.getProductById(id);
    return ResponseEntity.ok(ApiResponse.success(product, "Product retrieved"));
}
```

❌ **Don't put business logic in controllers**
❌ **Don't use `Optional` in service return types**
❌ **Don't use field injection** (use constructor injection)

## Port Assignments

- API Gateway: 8080 (main entry point)
- Product Service: 8081
- PostgreSQL: 5432

## Quick Commands

```bash
# Build service
cd backend/{service} && mvn clean install

# Run service
mvn spring-boot:run

# Run tests
mvn test

# Skip tests
mvn clean install -DskipTests

# Start database
docker compose up -d postgres
```

## When to Add Complexity

Only add these when metrics justify:
- **Redis caching**: When database becomes bottleneck
- **Message queue**: When async operations needed
- **Service discovery**: When services grow beyond 5-10
- **Distributed tracing**: When debugging cross-service issues

**Current Status**: Intentionally simple. No caching, no message queue. KISS principle.
