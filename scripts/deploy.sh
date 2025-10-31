#!/bin/bash
set -e

echo "🚀 Starting Hotel Booking System Deployment..."

# Variables
DOCKER_REGISTRY="saifudheenpv"
APP_NAME="hotel-booking-system"
VERSION="$1"
DEV_SERVER_IP="43.204.234.54"  # Your dev-server IP
LOCAL_DB_IP="157.51.222.57"  # UPDATE THIS!

if [ -z "$VERSION" ]; then
    echo "❌ Version parameter is required"
    echo "Usage: ./scripts/deploy.sh <version>"
    exit 1
fi

echo "📦 Deploying version: $VERSION"

# Deploy to Dev Server
echo "🔗 Connecting to Dev Server..."
ssh -o StrictHostKeyChecking=no ubuntu@$DEV_SERVER_IP "
    echo '📥 Pulling Docker image...'
    docker pull $DOCKER_REGISTRY/$APP_NAME:$VERSION
    
    echo '🛑 Stopping existing container...'
    docker stop hotel-booking-app || true
    docker rm hotel-booking-app || true
    
    echo '🎯 Starting new container...'
    docker run -d \\
        --name hotel-booking-app \\
        -p 8080:8080 \\
        -e SPRING_PROFILES_ACTIVE=docker,prod \\
        -e DB_HOST=$LOCAL_DB_IP \\
        -e DB_USER=root \\
        -e DB_PASSWORD=Shanu@9090! \\
        -e APP_DATA_INITIALIZE=true \\
        $DOCKER_REGISTRY/$APP_NAME:$VERSION
    
    echo '⏳ Waiting for application to start...'
    sleep 30
    
    echo '🔍 Performing health check...'
    curl -f http://localhost:8080/actuator/health || exit 1
    
    echo '✅ Deployment completed successfully!'
"

echo "🎉 Hotel Booking System deployed to http://$DEV_SERVER_IP:8080"