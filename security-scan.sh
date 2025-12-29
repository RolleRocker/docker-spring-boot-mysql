#!/bin/bash
# Security scanning script for Docker images and dependencies

echo "================================"
echo "Docker Security Scan"
echo "================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if docker is installed
if ! command -v docker &> /dev/null; then
    echo -e "${RED}Error: Docker is not installed${NC}"
    exit 1
fi

echo "1. Scanning MySQL image for vulnerabilities..."
echo "----------------------------------------"
docker scout quickview mysql:8.0.40 || docker scan mysql:8.0.40 2>/dev/null || echo "Install Docker Scout or Snyk for detailed scanning"
echo ""

echo "2. Scanning Eclipse Temurin JRE image..."
echo "----------------------------------------"
docker scout quickview eclipse-temurin:21.0.5_11-jre-alpine || docker scan eclipse-temurin:21.0.5_11-jre-alpine 2>/dev/null || echo "Install Docker Scout or Snyk for detailed scanning"
echo ""

echo "3. Scanning application image (if built)..."
echo "----------------------------------------"
if docker images | grep -q "messages-app"; then
    docker scout quickview messages-app:latest || docker scan messages-app:latest 2>/dev/null || echo "Application image not found or scanner not available"
else
    echo "Application image not built yet. Run 'docker-compose build' first."
fi
echo ""

echo "4. Checking for outdated images..."
echo "----------------------------------------"
docker images --format "table {{.Repository}}:{{.Tag}}\t{{.CreatedAt}}" | grep -E "mysql|temurin|maven"
echo ""

echo "5. Scanning Maven dependencies for vulnerabilities..."
echo "----------------------------------------"
if [ -f "pom.xml" ]; then
    echo "Running Maven dependency check..."
    mvn dependency:analyze -DignoreNonCompile=true 2>/dev/null || echo "Maven not available locally. Skipping dependency analysis."
    echo ""
    echo "Checking for dependency updates..."
    mvn versions:display-dependency-updates 2>/dev/null || echo "Maven not available. Run 'mvn versions:display-dependency-updates' manually."
else
    echo "pom.xml not found"
fi
echo ""

echo "6. Checking .env file security..."
echo "----------------------------------------"
if [ -f ".env" ]; then
    if grep -q "your_secure" .env; then
        echo -e "${RED}WARNING: .env contains placeholder passwords!${NC}"
        echo "Please update .env with secure passwords before deployment."
    else
        echo -e "${GREEN}✓ .env file exists with custom passwords${NC}"
    fi
    
    if git ls-files --error-unmatch .env 2>/dev/null; then
        echo -e "${RED}CRITICAL: .env is tracked by Git!${NC}"
        echo "Run: git rm --cached .env && git commit -m 'Remove .env from tracking'"
    else
        echo -e "${GREEN}✓ .env is not tracked by Git${NC}"
    fi
else
    echo -e "${YELLOW}WARNING: .env file not found${NC}"
    echo "Copy .env.example to .env and configure secure passwords"
fi
echo ""

echo "7. Checking for exposed ports..."
echo "----------------------------------------"
docker-compose ps 2>/dev/null | grep -E "3306|8080" || echo "Containers not running. Start with 'docker-compose up'"
echo ""

echo "================================"
echo "Security Scan Complete"
echo "================================"
echo ""
echo "Recommendations:"
echo "- Install Docker Scout: https://docs.docker.com/scout/"
echo "- Install Trivy: https://aquasecurity.github.io/trivy/"
echo "- Run 'mvn org.owasp:dependency-check-maven:check' for OWASP dependency scan"
echo "- Regularly update base images and dependencies"
echo "- Never commit .env or secrets to version control"
