# Dukkan - Quick Start Guide

## 🚀 Running the Application with Docker

### Prerequisites
- Docker Desktop installed and running
- At least 8GB RAM available
- Ports 80, 5432, 8080-8085 available

### Development Mode (Local Testing)

1. **Start Docker Desktop**

2. **Start all services:**
   ```bash
   docker compose up --build
   ```

3. **Access the application:**
   - **Frontend**: http://localhost (port 80)
   - **API Gateway**: http://localhost:8080
   - **Product Service**: http://localhost:8081
   - **Swagger UI**: http://localhost:8081/swagger-ui.html

4. **Stop all services:**
   ```bash
   docker compose down
   ```

5. **Clean restart (remove volumes):**
   ```bash
   docker compose down -v
   docker compose up --build
   ```

### What You'll See

**Initial Database:** The database will be automatically populated with:
- **8 Categories**: Electronics, Clothing, Books, Home & Kitchen, Sports & Outdoors, Toys & Games, Beauty & Personal Care, Automotive
- **32 Products**: Diverse products with realistic prices ($19.99 - $599.99) and stock levels

**Sample Users:** You'll need to register users through the UI:
- Navigate to http://localhost
- Click "Register" to create a customer account
- Use http://localhost/admin to access admin panel (requires ADMIN role user)

### Testing the Complete Flow

#### 1. Customer Journey
```
http://localhost
  ↓
Browse Products → Add to Cart → Checkout → Place Order → View Orders
```

#### 2. Admin Journey
```
http://localhost/admin
  ↓
Manage Products → Manage Categories → View Orders
```

### Troubleshooting

**Services not starting?**
```bash
# Check Docker daemon is running
docker info

# View service logs
docker compose logs -f [service-name]

# Examples:
docker compose logs -f product-service
docker compose logs -f postgres
docker compose logs -f api-gateway
```

**Database connection errors?**
```bash
# Wait for postgres to be healthy (takes 10-15 seconds)
docker compose ps

# Check postgres logs
docker compose logs postgres
```

**Port conflicts?**
```bash
# Check if ports are already in use
lsof -i :80
lsof -i :8080
lsof -i :5432

# Kill conflicting processes or change ports in docker-compose.yml
```

**Frontend can't reach API?**
- Ensure API Gateway is running: `docker compose ps`
- Check CORS settings in docker-compose.yml `ALLOWED_ORIGINS`
- Verify frontend build args in docker-compose.yml

### Production Deployment

For production deployment, use the production Docker Compose:

1. **Create production environment file:**
   ```bash
   cp .env.production.example .env.production
   # Edit .env.production with actual credentials
   ```

2. **Start production stack:**
   ```bash
   docker compose -f docker-compose.prod.yml --env-file .env.production up -d
   ```

3. **View production logs:**
   ```bash
   docker compose -f docker-compose.prod.yml logs -f
   ```

### Service URLs

| Service | Development | Production | Health Check |
|---------|-------------|------------|--------------|
| Frontend | http://localhost | https://yourdomain.com | http://localhost/index.html |
| API Gateway | http://localhost:8080 | https://api.yourdomain.com | http://localhost:8080/actuator/health |
| Product Service | http://localhost:8081 | Internal | http://localhost:8081/actuator/health |
| User Service | http://localhost:8082 | Internal | http://localhost:8082/actuator/health |
| Order Service | http://localhost:8083 | Internal | http://localhost:8083/actuator/health |
| Payment Service | http://localhost:8084 | Internal | http://localhost:8084/actuator/health |
| Email Service | http://localhost:8085 | Internal | http://localhost:8085/actuator/health |
| PostgreSQL | localhost:5432 | localhost:5432 | `docker compose ps` |

### Database Access

**Connect to database:**
```bash
docker exec -it dukkan-postgres psql -U dukkan -d dukkan_product
```

**View categories:**
```sql
SELECT id, name, description FROM categories;
```

**View products:**
```sql
SELECT name, price, stock_quantity, sku FROM products ORDER BY created_at DESC LIMIT 10;
```

**Check database sizes:**
```bash
docker exec -it dukkan-postgres psql -U dukkan -c "\l+"
```

### Performance Tips

**Speed up builds:**
```bash
# Use BuildKit for faster builds
DOCKER_BUILDKIT=1 docker compose build

# Build services in parallel
docker compose build --parallel
```

**Reduce image sizes:**
- All services use multi-stage builds
- Frontend uses nginx alpine
- Backend services use Eclipse Temurin JRE (not JDK)

**Monitor resource usage:**
```bash
docker stats
```

### Next Steps

After verifying the application runs:
1. Test user registration and login
2. Test product browsing and search
3. Test cart functionality
4. Test order placement (requires payment integration)
5. Test admin panel operations

### Need Help?

- Check `CLAUDE.md` for architecture details
- Check `ARCHITECTURE.md` for technical overview
- Check `DEVELOPMENT.md` for development guidelines
- Check `DEPLOYMENT.md` for detailed deployment guide
- Check service logs: `docker compose logs [service-name]`

### Quick Commands Reference

```bash
# Start everything
docker compose up -d

# Stop everything
docker compose down

# View logs (follow)
docker compose logs -f

# Rebuild specific service
docker compose build product-service
docker compose up -d product-service

# Clean everything (including volumes)
docker compose down -v
docker system prune -a --volumes

# Check service health
docker compose ps
```

---

**Ready to launch?** Just run `docker compose up --build` and visit http://localhost! 🎉
