# Security Scan Script (PowerShell version)
# Run this script to check for vulnerabilities in Docker images and dependencies

Write-Host "================================" -ForegroundColor Cyan
Write-Host "Docker Security Scan" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Check if docker is installed
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host "Error: Docker is not installed" -ForegroundColor Red
    exit 1
}

Write-Host "1. Scanning MySQL image for vulnerabilities..." -ForegroundColor Yellow
Write-Host "----------------------------------------"
docker scout quickview mysql:8.0.40 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "Docker Scout not available. Install it for detailed scanning." -ForegroundColor Gray
}
Write-Host ""

Write-Host "2. Scanning Eclipse Temurin JRE image..." -ForegroundColor Yellow
Write-Host "----------------------------------------"
docker scout quickview eclipse-temurin:21.0.5_11-jre-alpine 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "Docker Scout not available. Install it for detailed scanning." -ForegroundColor Gray
}
Write-Host ""

Write-Host "3. Scanning application image (if built)..." -ForegroundColor Yellow
Write-Host "----------------------------------------"
$appImage = docker images -q messages-app:latest 2>$null
if ($appImage) {
    docker scout quickview messages-app:latest 2>$null
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Docker Scout not available." -ForegroundColor Gray
    }
} else {
    Write-Host "Application image not built yet. Run 'docker-compose build' first." -ForegroundColor Gray
}
Write-Host ""

Write-Host "4. Checking for outdated images..." -ForegroundColor Yellow
Write-Host "----------------------------------------"
docker images --format "table {{.Repository}}:{{.Tag}}\t{{.CreatedAt}}" | Select-String -Pattern "mysql|temurin|maven"
Write-Host ""

Write-Host "5. Scanning Maven dependencies for vulnerabilities..." -ForegroundColor Yellow
Write-Host "----------------------------------------"
if (Test-Path "pom.xml") {
    Write-Host "Running Maven dependency check..."
    if (Get-Command mvn -ErrorAction SilentlyContinue) {
        mvn dependency:analyze -DignoreNonCompile=true 2>$null
        Write-Host ""
        Write-Host "Checking for dependency updates..."
        mvn versions:display-dependency-updates 2>$null
    } else {
        Write-Host "Maven not available. Install Maven to run dependency analysis." -ForegroundColor Gray
    }
} else {
    Write-Host "pom.xml not found" -ForegroundColor Gray
}
Write-Host ""

Write-Host "6. Checking .env file security..." -ForegroundColor Yellow
Write-Host "----------------------------------------"
if (Test-Path ".env") {
    $envContent = Get-Content ".env" -Raw
    if ($envContent -match "your_secure") {
        Write-Host "WARNING: .env contains placeholder passwords!" -ForegroundColor Red
        Write-Host "Please update .env with secure passwords before deployment."
    } else {
        Write-Host "✓ .env file exists with custom passwords" -ForegroundColor Green
    }
    
    $gitTracked = git ls-files --error-unmatch .env 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Host "CRITICAL: .env is tracked by Git!" -ForegroundColor Red
        Write-Host "Run: git rm --cached .env" -ForegroundColor Yellow
        Write-Host "Then: git commit -m 'Remove .env from tracking'" -ForegroundColor Yellow
    } else {
        Write-Host "✓ .env is not tracked by Git" -ForegroundColor Green
    }
} else {
    Write-Host "WARNING: .env file not found" -ForegroundColor Yellow
    Write-Host "Copy .env.example to .env and configure secure passwords"
}
Write-Host ""

Write-Host "7. Checking for exposed ports..." -ForegroundColor Yellow
Write-Host "----------------------------------------"
docker-compose ps 2>$null | Select-String -Pattern "3306|8080"
if ($LASTEXITCODE -ne 0) {
    Write-Host "Containers not running. Start with 'docker-compose up'" -ForegroundColor Gray
}
Write-Host ""

Write-Host "================================" -ForegroundColor Cyan
Write-Host "Security Scan Complete" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Recommendations:" -ForegroundColor White
Write-Host "- Install Docker Scout: https://docs.docker.com/scout/" -ForegroundColor Gray
Write-Host "- Install Trivy: https://aquasecurity.github.io/trivy/" -ForegroundColor Gray
Write-Host "- Run 'mvn org.owasp:dependency-check-maven:check' for OWASP dependency scan" -ForegroundColor Gray
Write-Host "- Regularly update base images and dependencies" -ForegroundColor Gray
Write-Host "- Never commit .env or secrets to version control" -ForegroundColor Gray
