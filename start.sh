#!/bin/bash

set -e

echo "Building application..."
./mvnw package -DskipTests

echo "Starting services..."
docker compose up --build -d

echo "Waiting for services to be healthy..."
sleep 20

echo "Checking health..."
curl -s http://localhost:8080/q/health

echo ""
echo "Services started successfully!"
echo "- API: http://localhost:8080"
echo "- Swagger UI: http://localhost:8080/q/swagger-ui"
echo "- MinIO Console: http://localhost:9001"
echo ""
echo "To view logs: docker compose logs -f"
echo "To stop: docker compose down"
