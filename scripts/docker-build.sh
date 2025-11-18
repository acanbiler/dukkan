#!/bin/bash

# Docker build script for Dukkan services
# Usage: ./scripts/docker-build.sh [service-name]
# Example: ./scripts/docker-build.sh payment-service
# If no service specified, builds all services

set -e  # Exit on error

echo "======================================"
echo "Dukkan Docker Build Script"
echo "======================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to build a service
build_service() {
    local service=$1
    echo ""
    echo -e "${YELLOW}Building ${service}...${NC}"

    if docker compose build --no-cache "$service"; then
        echo -e "${GREEN}✓ ${service} built successfully${NC}"
        return 0
    else
        echo -e "${RED}✗ ${service} build failed${NC}"
        return 1
    fi
}

# Function to build all services
build_all() {
    local services=("product-service" "user-service" "order-service" "payment-service" "api-gateway" "frontend")
    local failed_services=()

    for service in "${services[@]}"; do
        if ! build_service "$service"; then
            failed_services+=("$service")
        fi
    done

    echo ""
    echo "======================================"
    if [ ${#failed_services[@]} -eq 0 ]; then
        echo -e "${GREEN}All services built successfully!${NC}"
        echo "======================================"
        return 0
    else
        echo -e "${RED}Some services failed to build:${NC}"
        for service in "${failed_services[@]}"; do
            echo -e "${RED}  - ${service}${NC}"
        done
        echo "======================================"
        return 1
    fi
}

# Main logic
if [ $# -eq 0 ]; then
    echo "No service specified. Building all services..."
    build_all
else
    build_service "$1"
fi

echo ""
echo "Done!"
