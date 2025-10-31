#!/bin/bash

# Deployment script for Hotel Booking System
set -e

echo "🚀 Starting deployment..."

# Pull latest images
docker pull saifudheenpv/hotel-booking-system:latest

# Stop and remove existing containers
docker stop hotel-booking-app || true
docker rm hotel-booking-app || true

# Deploy new container
docker run -d \
    --name hotel-booking-app \
    --restart unless-stopped \
    -e SPRING_PROFILES_ACTIVE=docker \
    -p 8080:8080 \
    saifudheenpv/hotel-booking-system:latest

echo "✅ Deployment completed successfully!"

# Health check
sleep 30
curl -f http://localhost:8080/actuator/health || exit 1

echo "✅ Health check passed!"