# Docker Setup Guide

This guide explains how to run the Dukkan application using Docker Compose.

## Prerequisites

- Docker Engine 20.10+
- Docker Compose V2+
- At least 4GB of available RAM
- At least 10GB of available disk space

## Quick Start

### 1. Start All Services

```bash
# Build and start all services
docker compose up -d

# View logs
docker compose logs -f

# View logs for specific service
docker compose logs -f payment-service
```

### 2. Access the Application

- **Frontend**: http://localhost:80
- **API Gateway**: http://localhost:8080
- **Swagger UI (Product Service)**: http://localhost:8081/swagger-ui.html
- **Swagger UI (Payment Service)**: http://localhost:8084/swagger-ui.html
- **PostgreSQL**: localhost:5432

### 3. Stop All Services

```bash
# Stop services (keeps data)
docker compose stop

# Stop and remove containers (keeps data)
docker compose down

# Stop, remove containers, and delete data
docker compose down -v
```

## Service Architecture

The application consists of:

1. **PostgreSQL** (port 5432) - Database with 4 schemas
2. **Product Service** (port 8081) - Product catalog management
3. **User Service** (port 8082) - User authentication and management
4. **Order Service** (port 8083) - Order processing
5. **Payment Service** (port 8084) - Payment processing (Iyzico integration)
6. **API Gateway** (port 8080) - Request routing and aggregation
7. **Frontend** (port 80) - React SPA served by nginx

## Build Configuration

### Multi-Stage Builds

All Spring Boot services use multi-stage Dockerfiles:
- **Build stage**: Maven 3.9.9 + JDK 17 (builds the JAR)
- **Runtime stage**: JRE 17 Alpine (runs the JAR)

Frontend uses:
- **Build stage**: Node 20 (builds React app)
- **Runtime stage**: nginx 1.27 (serves static files)

### Build Context

All Dockerfiles use the repository root (`.`) as build context to access their respective source directories.

## Environment Variables

### Database Configuration

All services connect to PostgreSQL with these credentials (defined in docker-compose.yml):
- Host: `postgres`
- Port: `5432`
- User: `dukkan`
- Password: `dukkan123`

### Payment Service (Optional)

For Iyzico payment integration, create a `.env` file:

```bash
# .env file
IYZICO_API_KEY=your-iyzico-api-key
IYZICO_SECRET_KEY=your-iyzico-secret-key
```

If not provided, sandbox default values will be used.

## Useful Commands

### Build Specific Service

```bash
# Build only payment service
docker compose build payment-service

# Build without cache
docker compose build --no-cache payment-service
```

### Start Specific Services

```bash
# Start only database and product service
docker compose up -d postgres product-service

# Start with dependencies
docker compose up -d api-gateway  # Starts all dependent services too
```

### View Service Status

```bash
# List running containers
docker compose ps

# View resource usage
docker stats

# View service health
docker compose ps --format json | jq '.[] | {name: .Name, health: .Health}'
```

### Debugging

```bash
# Execute command in running container
docker compose exec payment-service sh

# View environment variables
docker compose exec payment-service env

# View logs with timestamps
docker compose logs -f -t payment-service

# View last 100 lines
docker compose logs --tail=100 payment-service
```

### Database Access

```bash
# Connect to PostgreSQL
docker compose exec postgres psql -U dukkan -d payment_db

# Run SQL file
docker compose exec -T postgres psql -U dukkan -d payment_db < backup.sql

# Backup database
docker compose exec postgres pg_dump -U dukkan payment_db > backup.sql
```

## Troubleshooting

### Services Won't Start

1. **Check logs**: `docker compose logs [service-name]`
2. **Verify port availability**: `lsof -i :8080` (macOS/Linux)
3. **Rebuild images**: `docker compose build --no-cache`
4. **Clean up**: `docker system prune -a`

### Database Connection Errors

1. **Wait for database**: Services have `depends_on` with health checks
2. **Check database is healthy**: `docker compose ps postgres`
3. **Verify credentials**: Check environment variables in docker-compose.yml

### Build Failures

1. **Maven dependencies**:
   ```bash
   docker compose build --no-cache payment-service
   ```

2. **Out of disk space**:
   ```bash
   docker system prune -a
   docker volume prune
   ```

3. **Memory issues**: Increase Docker Desktop memory allocation (Preferences > Resources)

### Frontend Not Loading

1. **Check API Gateway**: `curl http://localhost:8080/actuator/health`
2. **Check CORS settings**: Verify `ALLOWED_ORIGINS` in docker-compose.yml
3. **Browser cache**: Hard refresh (Cmd+Shift+R / Ctrl+F5)

## Development Workflow

### Local Development + Docker Database

Run only PostgreSQL in Docker, run services locally:

```bash
# Start only database
docker compose up -d postgres

# Run services locally
cd backend/payment-service
mvn spring-boot:run

cd ../../frontend
npm run dev
```

### Full Docker Development

All services in Docker with hot-reload:

```bash
# TODO: Add volume mounts for source code
# This requires modifying Dockerfiles to support development mode
```

## Production Considerations

For production deployment, consider:

1. **Environment-specific configs**: Use different compose files
   ```bash
   docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
   ```

2. **Secrets management**: Use Docker secrets or external secret managers
3. **Health checks**: All services have health checks configured
4. **Resource limits**: Add memory/CPU limits to services
5. **Logging**: Configure centralized logging (ELK, Splunk, etc.)
6. **Monitoring**: Add Prometheus + Grafana
7. **Backup strategy**: Automated PostgreSQL backups
8. **SSL/TLS**: Add nginx reverse proxy with SSL certificates

## Next Steps

- Configure monitoring with Prometheus + Grafana
- Add Redis for caching
- Setup CI/CD pipeline for automated builds
- Implement blue-green deployment strategy
- Add Elasticsearch for product search
