# start_docker_services.ps1
# PowerShell script to start Docker services for the Java Docker project
# Handles Windows paths with spaces properly

Write-Host "Starting the application with Docker Compose..." -ForegroundColor Green

# Build and start services
Write-Host "Building and starting containers..." -ForegroundColor Cyan
& docker-compose up --build -d

if ($LASTEXITCODE -ne 0) {
    Write-Host "Error: Failed to start Docker Compose services" -ForegroundColor Red
    exit 1
}

Write-Host "Services are starting up..." -ForegroundColor Yellow
Write-Host "MySQL will be available on localhost:3306" -ForegroundColor Cyan
Write-Host "Application will be available on localhost:8080" -ForegroundColor Cyan

# Wait for services to be healthy
Write-Host "Waiting for services to be healthy..." -ForegroundColor Yellow
& docker-compose ps

# Show logs
Write-Host "Showing application logs (press Ctrl+C to stop)..." -ForegroundColor Yellow
& docker-compose logs -f app
