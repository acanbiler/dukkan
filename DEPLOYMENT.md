# Dukkan Deployment Guide

Comprehensive guide for deploying the Dukkan e-commerce platform to various environments.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Quick Start (Docker)](#quick-start-docker)
- [Local Development](#local-development)
- [Production Deployment](#production-deployment)
  - [Docker Compose Production](#docker-compose-production)
  - [Kubernetes](#kubernetes)
  - [Cloud Platforms](#cloud-platforms)
- [Environment Configuration](#environment-configuration)
- [Database Management](#database-management)
- [Monitoring & Logging](#monitoring--logging)
- [Security Hardening](#security-hardening)
- [Backup & Recovery](#backup--recovery)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software

- **Docker 24.0+** and **Docker Compose V2+**
- **Git** for version control
- **(Optional) kubectl** for Kubernetes deployments
- **(Optional) Cloud CLI** (AWS CLI, gcloud, az) for cloud deployments

### System Requirements

**Minimum (Development):**
- 8GB RAM
- 4 CPU cores
- 20GB disk space
- Linux, macOS, or Windows 10+ with WSL2

**Recommended (Production):**
- 16GB RAM
- 8 CPU cores
- 100GB SSD storage
- Ubuntu 22.04 LTS or similar

---

## Quick Start (Docker)

Deploy the entire stack locally in under 5 minutes:

```bash
# Clone repository
git clone <repository-url>
cd dukkan

# Create environment file
cp .env.example .env
# Edit .env with your Iyzico credentials (optional)

# Start all services
docker compose up -d --build

# View logs
docker compose logs -f

# Access the application
open http://localhost:80
```

**Service URLs:**
- Frontend: http://localhost:80
- API Gateway: http://localhost:8080
- Product Service Swagger: http://localhost:8081/swagger-ui.html
- User Service Swagger: http://localhost:8082/swagger-ui.html
- Order Service Swagger: http://localhost:8083/swagger-ui.html
- Payment Service Swagger: http://localhost:8084/swagger-ui.html
- Email Service Swagger: http://localhost:8085/swagger-ui.html

---

## Local Development

For active development with hot-reload and debugging capabilities:

### Option 1: Hybrid (Database in Docker, Services Local)

```bash
# Terminal 1: Start PostgreSQL
docker compose up -d postgres

# Terminal 2-7: Backend Services
cd backend/product-service && mvn spring-boot:run
cd backend/user-service && mvn spring-boot:run
cd backend/order-service && mvn spring-boot:run
cd backend/payment-service && mvn spring-boot:run
cd backend/email-service && mvn spring-boot:run
cd backend/api-gateway && mvn spring-boot:run

# Terminal 8: Frontend
cd frontend && npm run dev
```

**Advantages:**
- ✅ Fast hot-reload
- ✅ Easy debugging
- ✅ IDE integration

**Access:** http://localhost:5173

### Option 2: Full Docker with Volume Mounts

```bash
# Use development compose file (when available)
docker compose -f docker-compose.yml -f docker-compose.dev.yml up
```

---

## Production Deployment

### Docker Compose Production

#### 1. Create Production Configuration

Create `docker-compose.prod.yml`:

```yaml
version: '3.8'

services:
  postgres:
    environment:
      POSTGRES_PASSWORD: ${DB_PASSWORD}  # Use strong password
    volumes:
      - /var/lib/postgresql/data:/var/lib/postgresql/data  # Persistent storage
    restart: always

  product-service:
    environment:
      SPRING_PROFILES_ACTIVE: prod
      JAVA_OPTS: "-Xmx1024m -Xms512m"
    restart: always

  user-service:
    environment:
      SPRING_PROFILES_ACTIVE: prod
      JAVA_OPTS: "-Xmx1024m -Xms512m"
    restart: always

  order-service:
    environment:
      SPRING_PROFILES_ACTIVE: prod
      JAVA_OPTS: "-Xmx1024m -Xms512m"
    restart: always

  payment-service:
    environment:
      SPRING_PROFILES_ACTIVE: prod
      JAVA_OPTS: "-Xmx1024m -Xms512m"
      IYZICO_BASE_URL: https://api.iyzipay.com  # Production Iyzico URL
    restart: always

  email-service:
    environment:
      SPRING_PROFILES_ACTIVE: prod
      JAVA_OPTS: "-Xmx512m -Xms256m"
      SENDGRID_API_KEY: ${SENDGRID_API_KEY}  # Production SendGrid API key
    restart: always

  api-gateway:
    environment:
      SPRING_PROFILES_ACTIVE: prod
      JAVA_OPTS: "-Xmx1024m -Xms512m"
      ALLOWED_ORIGINS: https://yourdomain.com
    restart: always

  frontend:
    environment:
      VITE_API_BASE_URL: https://api.yourdomain.com/api/v1
    restart: always

  # Add nginx reverse proxy for SSL
  nginx:
    image: nginx:1.27-alpine
    ports:
      - "443:443"
      - "80:80"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/ssl:/etc/nginx/ssl:ro
    depends_on:
      - frontend
      - api-gateway
    restart: always
```

#### 2. Configure Production Environment

Create `.env.prod`:

```bash
# Database
POSTGRES_USER=dukkan
POSTGRES_PASSWORD=<strong-random-password>
DB_HOST=postgres
DB_PORT=5432

# Iyzico Production
IYZICO_API_KEY=<production-api-key>
IYZICO_SECRET_KEY=<production-secret-key>
IYZICO_BASE_URL=https://api.iyzipay.com

# SendGrid Production
SENDGRID_API_KEY=<production-sendgrid-api-key>
SENDGRID_FROM_EMAIL=noreply@yourdomain.com
SENDGRID_FROM_NAME=Dukkan

# CORS
ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com
```

**Security Note:** Never commit `.env.prod` to version control!

#### 3. Deploy to Production

```bash
# Build production images
docker compose -f docker-compose.yml -f docker-compose.prod.yml build

# Start services
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d

# Verify health
docker compose ps
curl http://localhost:8080/actuator/health
```

#### 4. Setup SSL with Let's Encrypt

```bash
# Install certbot
sudo apt install certbot python3-certbot-nginx

# Obtain certificate
sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com

# Auto-renewal (cron job)
sudo crontab -e
# Add: 0 12 * * * /usr/bin/certbot renew --quiet
```

### Kubernetes Deployment

#### 1. Create Kubernetes Manifests

```yaml
# k8s/namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: dukkan

---
# k8s/postgres-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: postgres
  namespace: dukkan
spec:
  replicas: 1
  selector:
    matchLabels:
      app: postgres
  template:
    metadata:
      labels:
        app: postgres
    spec:
      containers:
      - name: postgres
        image: postgres:17-alpine
        env:
        - name: POSTGRES_USER
          value: dukkan
        - name: POSTGRES_PASSWORD
          valueFrom:
            secretKeyRef:
              name: postgres-secret
              key: password
        - name: POSTGRES_MULTIPLE_DATABASES
          value: dukkan_product,dukkan_user,dukkan_order,payment_db,email_db
        ports:
        - containerPort: 5432
        volumeMounts:
        - name: postgres-storage
          mountPath: /var/lib/postgresql/data
      volumes:
      - name: postgres-storage
        persistentVolumeClaim:
          claimName: postgres-pvc

---
# k8s/product-service-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: product-service
  namespace: dukkan
spec:
  replicas: 3
  selector:
    matchLabels:
      app: product-service
  template:
    metadata:
      labels:
        app: product-service
    spec:
      containers:
      - name: product-service
        image: dukkan/product-service:latest
        ports:
        - containerPort: 8081
        env:
        - name: DB_HOST
          value: postgres
        - name: DB_NAME
          value: dukkan_product
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8081
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 5
```

#### 2. Deploy to Kubernetes

```bash
# Apply manifests
kubectl apply -f k8s/

# Check deployment status
kubectl get pods -n dukkan
kubectl get services -n dukkan

# View logs
kubectl logs -f deployment/product-service -n dukkan
```

### Cloud Platforms

#### AWS ECS Deployment

```bash
# Build and push images to ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com

docker build -t dukkan/product-service -f docker/product-service/Dockerfile .
docker tag dukkan/product-service:latest <account-id>.dkr.ecr.us-east-1.amazonaws.com/product-service:latest
docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/product-service:latest

# Create ECS task definitions and services using AWS Console or CLI
```

#### Google Cloud Run

```bash
# Build and deploy
gcloud builds submit --tag gcr.io/PROJECT_ID/product-service
gcloud run deploy product-service --image gcr.io/PROJECT_ID/product-service --platform managed
```

#### Azure Container Instances

```bash
# Build and push to ACR
az acr build --registry myregistry --image dukkan/product-service:latest .
az container create --resource-group myResourceGroup --name product-service \
  --image myregistry.azurecr.io/dukkan/product-service:latest
```

---

## Environment Configuration

### Required Environment Variables

**Database (All Services):**
```bash
DB_HOST=postgres
DB_PORT=5432
DB_NAME=<service_specific_db>
DB_USER=dukkan
DB_PASSWORD=<strong-password>
```

**Payment Service:**
```bash
IYZICO_API_KEY=<your-api-key>
IYZICO_SECRET_KEY=<your-secret-key>
IYZICO_BASE_URL=https://api.iyzipay.com  # Production
# IYZICO_BASE_URL=https://sandbox-api.iyzipay.com  # Sandbox
```

**Email Service:**
```bash
SENDGRID_API_KEY=<your-sendgrid-api-key>
SENDGRID_FROM_EMAIL=noreply@yourdomain.com
SENDGRID_FROM_NAME=Dukkan
```

**API Gateway:**
```bash
PRODUCT_SERVICE_URL=http://product-service:8081
USER_SERVICE_URL=http://user-service:8082
ORDER_SERVICE_URL=http://order-service:8083
PAYMENT_SERVICE_URL=http://payment-service:8084
EMAIL_SERVICE_URL=http://email-service:8085
ALLOWED_ORIGINS=https://yourdomain.com
```

**Frontend:**
```bash
VITE_API_BASE_URL=https://api.yourdomain.com/api/v1
```

### Spring Profiles

Create profile-specific configurations:

**application-prod.yml:**
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
  jpa:
    show-sql: false
    properties:
      hibernate:
        format_sql: false

logging:
  level:
    root: INFO
    com.dukkan: INFO
```

Activate profile:
```bash
java -jar -Dspring.profiles.active=prod app.jar
# Or via environment variable
SPRING_PROFILES_ACTIVE=prod
```

---

## Database Management

### Backups

#### Automated Backups (Cron)

```bash
# Create backup script
cat > /opt/dukkan/backup-db.sh << 'EOF'
#!/bin/bash
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR=/var/backups/dukkan
mkdir -p $BACKUP_DIR

docker exec dukkan-postgres pg_dump -U dukkan dukkan_product > $BACKUP_DIR/product_$TIMESTAMP.sql
docker exec dukkan-postgres pg_dump -U dukkan dukkan_user > $BACKUP_DIR/user_$TIMESTAMP.sql
docker exec dukkan-postgres pg_dump -U dukkan dukkan_order > $BACKUP_DIR/order_$TIMESTAMP.sql
docker exec dukkan-postgres pg_dump -U dukkan payment_db > $BACKUP_DIR/payment_$TIMESTAMP.sql
docker exec dukkan-postgres pg_dump -U dukkan email_db > $BACKUP_DIR/email_$TIMESTAMP.sql

# Keep only last 7 days
find $BACKUP_DIR -name "*.sql" -mtime +7 -delete

echo "Backup completed: $TIMESTAMP"
EOF

chmod +x /opt/dukkan/backup-db.sh

# Schedule daily backups at 2 AM
crontab -e
# Add: 0 2 * * * /opt/dukkan/backup-db.sh
```

#### Manual Backup

```bash
# Backup all databases
docker exec dukkan-postgres pg_dumpall -U dukkan > dukkan_backup_$(date +%Y%m%d).sql

# Backup specific database
docker exec dukkan-postgres pg_dump -U dukkan payment_db > payment_backup.sql
docker exec dukkan-postgres pg_dump -U dukkan email_db > email_backup.sql
```

### Restore

```bash
# Restore all databases
cat dukkan_backup_20251117.sql | docker exec -i dukkan-postgres psql -U dukkan

# Restore specific database
cat payment_backup.sql | docker exec -i dukkan-postgres psql -U dukkan -d payment_db
```

### Migrations

Liquibase runs automatically on service startup. To manually control:

```yaml
# Disable auto-migration
spring:
  liquibase:
    enabled: false

# Or use command line
mvn liquibase:update  # Apply migrations
mvn liquibase:rollback -Dliquibase.rollbackCount=1  # Rollback last changeset
```

---

## Monitoring & Logging

### Health Checks

All services expose actuator endpoints:

```bash
# Check individual service
curl http://localhost:8081/actuator/health

# Check all services
for port in 8081 8082 8083 8084 8085 8080; do
  echo "Service on $port:"
  curl http://localhost:$port/actuator/health
  echo -e "\n"
done
```

### Prometheus + Grafana Setup

```yaml
# docker-compose.monitoring.yml
version: '3.8'

services:
  prometheus:
    image: prom/prometheus:latest
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    ports:
      - "9090:9090"
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
    networks:
      - dukkan-network

  grafana:
    image: grafana/grafana:latest
    volumes:
      - grafana_data:/var/lib/grafana
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    ports:
      - "3000:3000"
    networks:
      - dukkan-network

volumes:
  prometheus_data:
  grafana_data:
```

**prometheus.yml:**
```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'product-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['product-service:8081']

  - job_name: 'user-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['user-service:8082']

  # Add other services...
```

### Centralized Logging (ELK Stack)

```bash
# Deploy ELK stack
docker compose -f docker-compose.elk.yml up -d

# Configure services to send logs to Logstash
# Add to each service in docker-compose.yml:
logging:
  driver: "json-file"
  options:
    max-size: "10m"
    max-file: "3"
```

---

## Security Hardening

### Production Checklist

- [ ] Use strong database passwords
- [ ] Enable SSL/TLS for all services
- [ ] Restrict database access to backend services only
- [ ] Use secrets management (HashiCorp Vault, AWS Secrets Manager)
- [ ] Implement rate limiting on API Gateway
- [ ] Enable CSRF protection
- [ ] Use httpOnly cookies for JWT tokens
- [ ] Regular security updates for base images
- [ ] Network isolation (VPC/Security Groups)
- [ ] Implement WAF (Web Application Firewall)
- [ ] Enable audit logging
- [ ] Set up intrusion detection

### Docker Security

```yaml
# Run containers as non-root user
user: "1000:1000"

# Read-only filesystem
read_only: true
tmpfs:
  - /tmp

# Drop unnecessary capabilities
cap_drop:
  - ALL
cap_add:
  - NET_BIND_SERVICE

# Resource limits
resources:
  limits:
    cpus: '2.0'
    memory: 2G
```

---

## Backup & Recovery

### Disaster Recovery Plan

1. **Regular Backups:**
   - Database: Daily at 2 AM
   - Application config: Weekly
   - Docker volumes: Weekly

2. **Off-site Storage:**
   - Upload backups to S3/GCS/Azure Blob
   ```bash
   aws s3 sync /var/backups/dukkan s3://dukkan-backups/$(date +%Y%m%d)/
   ```

3. **Recovery Time Objective (RTO):** < 1 hour
4. **Recovery Point Objective (RPO):** < 24 hours

### Full System Recovery

```bash
# 1. Restore database backups
cat payment_backup.sql | docker exec -i dukkan-postgres psql -U dukkan -d payment_db

# 2. Restart services
docker compose up -d

# 3. Verify health
./scripts/health-check.sh
```

---

## Troubleshooting

### Common Issues

#### Services Won't Start

```bash
# Check port conflicts
lsof -i :8080
lsof -i :8081

# Check logs
docker compose logs product-service

# Rebuild images
docker compose build --no-cache product-service
```

#### Database Connection Failures

```bash
# Verify postgres is healthy
docker compose ps postgres

# Check database logs
docker compose logs postgres

# Test connection
docker exec -it dukkan-postgres psql -U dukkan -d payment_db
```

#### Out of Memory

```bash
# Check container resources
docker stats

# Increase JVM memory
JAVA_OPTS="-Xmx2048m -Xms1024m"

# Restart with more resources
docker compose up -d --force-recreate payment-service
```

#### Slow Performance

```bash
# Check database indexes
docker exec -it dukkan-postgres psql -U dukkan -d payment_db -c "\d+ payments"

# Enable query logging
# Set in application.yml:
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
```

### Debug Mode

```bash
# Enable debug logging
docker compose exec payment-service \
  java -jar -Dlogging.level.com.dukkan=DEBUG app.jar

# Or via environment
environment:
  LOGGING_LEVEL_COM_DUKKAN: DEBUG
```

---

## Performance Tuning

### Database Optimization

```sql
-- Create indexes on frequently queried columns
CREATE INDEX idx_payment_user_status ON payments(user_id, status);
CREATE INDEX idx_payment_created_at ON payments(created_at DESC);

-- Analyze query performance
EXPLAIN ANALYZE SELECT * FROM payments WHERE user_id = '...' AND status = 'COMPLETED';
```

### JVM Tuning

```bash
# Production JVM options
JAVA_OPTS="-Xmx2048m \
  -Xms1024m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/tmp/heapdump.hprof"
```

### Connection Pooling

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

---

## Scaling

### Horizontal Scaling

```yaml
# Scale services
docker compose up -d --scale product-service=3 --scale payment-service=2

# With load balancer
# Add nginx upstream configuration
upstream product_service {
  server product-service-1:8081;
  server product-service-2:8081;
  server product-service-3:8081;
}
```

### Vertical Scaling

```yaml
# Increase resources per container
services:
  payment-service:
    deploy:
      resources:
        limits:
          cpus: '4.0'
          memory: 8G
        reservations:
          cpus: '2.0'
          memory: 4G
```

---

**Last Updated:** 2025-11-18
**Version:** 1.1
**Maintained By:** Dukkan DevOps Team
