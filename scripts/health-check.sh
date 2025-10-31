#!/bin/bash

echo "🏨 Hotel Booking System Health Check"
echo "===================================="

BASE_URL="http://localhost:8080"

# Check health endpoint
echo "1. Checking application health..."
HEALTH_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/actuator/health")
if [ "$HEALTH_RESPONSE" -eq 200 ]; then
    echo "   ✅ Health Check: HTTP $HEALTH_RESPONSE"
else
    echo "   ❌ Health Check: HTTP $HEALTH_RESPONSE"
    exit 1
fi

# Check main pages
echo "2. Checking web pages..."
PAGES=("/" "/hotels" "/login" "/register")

for page in "${PAGES[@]}"; do
    RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL$page")
    if [ "$RESPONSE" -eq 200 ]; then
        echo "   ✅ $page: HTTP $RESPONSE"
    else
        echo "   ⚠️ $page: HTTP $RESPONSE"
    fi
done

echo "===================================="
echo "✅ Health check completed successfully!"