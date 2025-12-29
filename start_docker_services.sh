#!/bin/bash
# start.sh

echo "Starting the application with Docker Compose..."

# Build and start services
docker-compose up --build -d

echo "Services are starting up..."
echo "MySQL will be available on localhost:3306"
echo "Application will be available on localhost:8080"

# Wait for services to be healthy
echo "Waiting for services to be healthy..."
docker-compose ps

# Show logs
echo "Showing application logs..."
docker-compose logs -f app