# Getting Started with Dukkan

## What We've Set Up

### Configuration Files Created

✅ **Backend Services**
- `backend/product-service/pom.xml` - Maven configuration with Spring Boot 3.5.7 & Java 25
- `backend/product-service/src/main/resources/application.yml` - Service configuration
- `backend/product-service/src/main/java/com/dukkan/product/ProductServiceApplication.java` - Main class

- `backend/api-gateway/pom.xml` - Maven configuration with Spring Cloud Gateway
- `backend/api-gateway/src/main/resources/application.yml` - Gateway routing configuration
- `backend/api-gateway/src/main/java/com/dukkan/gateway/ApiGatewayApplication.java` - Main class

✅ **Infrastructure**
- `docker-compose.yml` - PostgreSQL 17 + Redis 7 setup
- `.env.example` files for both services

✅ **Database**
- Liquibase changelog structure ready at `backend/product-service/src/main/resources/db/changelog/`

## Quick Start

### 1. Start Infrastructure (PostgreSQL)

```bash
# Start PostgreSQL
docker compose up -d postgres

# Verify it's running
docker compose ps
```

### 2. Build and Run Product Service

```bash
cd backend/product-service

# Build the service
mvn clean install

# Run the service
mvn spring-boot:run
```

The service will be available at: `http://localhost:8081`

### 3. Build and Run API Gateway

```bash
cd backend/api-gateway

# Build the gateway
mvn clean install

# Run the gateway
mvn spring-boot:run
```

The gateway will be available at: `http://localhost:8080`

### 4. Verify Everything Works

**Check Product Service Health:**
```bash
curl http://localhost:8081/actuator/health
```

**Check API Gateway Health:**
```bash
curl http://localhost:8080/actuator/health
```

**View API Documentation:**
- Product Service: http://localhost:8081/swagger-ui.html
- API Docs JSON: http://localhost:8081/api-docs

## Configuration Highlights

### Product Service (Port 8081)
- PostgreSQL database connection with HikariCP pooling
- Liquibase migrations enabled
- OpenAPI/Swagger documentation
- Spring Boot Actuator for health checks

### API Gateway (Port 8080)
- Routes requests to Product Service (`/api/v1/products/**`, `/api/v1/categories/**`)
- CORS enabled for frontend (localhost:5173, localhost:3000)
- Health checks and metrics exposed

### Docker Infrastructure
- **PostgreSQL 17**: Port 5432, database `dukkan_product`, user `dukkan`

## Environment Variables

Both services use environment variables for configuration. Copy `.env.example` to `.env` if needed:

```bash
cp backend/product-service/.env.example backend/product-service/.env
cp backend/api-gateway/.env.example backend/api-gateway/.env
```

## What's Next?

Following the **Domain-First** approach, we should implement:

1. **Domain Layer** - Product and Category entities
2. **Repository Layer** - JPA repositories
3. **Service Layer** - Business logic with caching
4. **Controller Layer** - REST API endpoints
5. **Tests** - Unit and integration tests

## Troubleshooting

**Port already in use?**
```bash
# Find process using port
lsof -i :8081
lsof -i :8080

# Kill process
kill -9 <PID>
```

**Database connection issues?**
```bash
# Check if PostgreSQL is running
docker compose ps postgres

# View PostgreSQL logs
docker compose logs postgres

# Connect to PostgreSQL
docker exec -it dukkan-postgres psql -U dukkan -d dukkan_product
```

