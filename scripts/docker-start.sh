#!/bin/bash

# Docker start script for Dukkan
# Usage: ./scripts/docker-start.sh [options]

set -e  # Exit on error

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo "======================================"
echo "Dukkan Docker Start Script"
echo "======================================"

# Check if .env exists
if [ ! -f .env ]; then
    echo -e "${YELLOW}Warning: .env file not found${NC}"
    echo "Creating .env from .env.example..."
    if [ -f .env.example ]; then
        cp .env.example .env
        echo -e "${GREEN}.env file created. Please update with your values.${NC}"
    fi
fi

# Parse arguments
BUILD=false
LOGS=false

while [[ $# -gt 0 ]]; do
    case $1 in
        --build|-b)
            BUILD=true
            shift
            ;;
        --logs|-l)
            LOGS=true
            shift
            ;;
        --help|-h)
            echo "Usage: ./scripts/docker-start.sh [options]"
            echo ""
            echo "Options:"
            echo "  --build, -b    Build images before starting"
            echo "  --logs, -l     Follow logs after starting"
            echo "  --help, -h     Show this help message"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

# Build if requested
if [ "$BUILD" = true ]; then
    echo ""
    echo -e "${BLUE}Building images...${NC}"
    docker compose build
fi

# Start services
echo ""
echo -e "${BLUE}Starting services...${NC}"
docker compose up -d

# Wait for services to be healthy
echo ""
echo -e "${BLUE}Waiting for services to be ready...${NC}"
sleep 10

# Show status
echo ""
echo -e "${GREEN}Services started!${NC}"
echo ""
docker compose ps

# Show URLs
echo ""
echo "======================================"
echo -e "${GREEN}Application URLs:${NC}"
echo "======================================"
echo -e "${BLUE}Frontend:${NC}          http://localhost:80"
echo -e "${BLUE}API Gateway:${NC}       http://localhost:8080"
echo -e "${BLUE}Product Service:${NC}   http://localhost:8081/swagger-ui.html"
echo -e "${BLUE}User Service:${NC}      http://localhost:8082/swagger-ui.html"
echo -e "${BLUE}Order Service:${NC}     http://localhost:8083/swagger-ui.html"
echo -e "${BLUE}Payment Service:${NC}   http://localhost:8084/swagger-ui.html"
echo -e "${BLUE}PostgreSQL:${NC}        localhost:5432"
echo "======================================"

# Follow logs if requested
if [ "$LOGS" = true ]; then
    echo ""
    echo -e "${BLUE}Following logs (Ctrl+C to exit)...${NC}"
    docker compose logs -f
fi
