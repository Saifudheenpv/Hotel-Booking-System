#!/bin/bash

# Deployment script for Hotel Booking System
set -e

echo "🚀 Starting deployment..."

# Pull latest image
docker pull saifudheenpv/hotel-booking-system:latest

# Stop and remove existing container
docker stop hotel-booking-app || true
docker rm hotel-booking-app || true

# Run new container
docker run -d \
    --name hotel-booking-app \
    -p 8080:8080 \
    -e SPRING_PROFILES_ACTIVE=docker \
    --restart unless-stopped \
    saifudheenpv/hotel-booking-system:latest

echo "✅ Deployment completed successfully!"

# Wait for application to start
sleep 30

# Run health check
./scripts/health-check.sh
