#!/bin/bash

# Health check script for Hotel Booking System
echo "🔍 Starting health check..."

# Check if application is responding
response=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health)

if [ "$response" -eq 200 ]; then
    echo "✅ Application is healthy (HTTP $response)"
    exit 0
else
    echo "❌ Application health check failed (HTTP $response)"
    exit 1
fi
