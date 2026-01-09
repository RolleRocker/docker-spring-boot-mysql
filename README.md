# Docker Spring Boot MySQL Project

[![Security Scan](https://github.com/RolleRocker/docker-spring-boot-mysql/actions/workflows/security-scan.yml/badge.svg)](https://github.com/RolleRocker/docker-spring-boot-mysql/actions/workflows/security-scan.yml)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.13-brightgreen)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0.40-blue)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED)](https://www.docker.com/)

A production-ready Spring Boot REST API with MySQL persistence, designed as a comprehensive Docker learning project featuring multi-stage builds, security scanning, and cloud-native best practices.

## 🎯 Features

- **Modern Stack**: Spring Boot 3.4.13 + Java 21 + MySQL 8.0.40
- **RESTful API**: Full CRUD operations with message persistence using record-based DTOs
- **Docker Optimized**: Multi-stage Dockerfile with dependency caching
- **Security First**: OWASP dependency scanning, environment variables, automated CI/CD checks
- **Production Ready**: Health checks, actuator endpoints, non-root containers
- **Test Coverage**: 80% minimum enforced with JaCoCo
- **Automated Workflows**: GitHub Actions for security scanning and validation

## 🏗️ Architecture

```text
┌─────────────────┐
│   REST Client   │
└────────┬────────┘
         │ HTTP
┌────────▼────────┐
│  Spring Boot    │ Port 8080
│  Application    │
│  (Container)    │
└────────┬────────┘
         │ JDBC
┌────────▼────────┐
│   MySQL 8.0     │ Port 3306
│   (Container)   │
│   + Volume      │
└─────────────────┘
```

### Components

**4-Layer Architecture:**

- **Controllers** (`org.roland.controller`): REST endpoints under `/api` prefix using constructor injection for dependency management
- **DTOs** (`org.roland.dto`): Immutable record-based request/response objects with Bean Validation (`@Valid`)
- **Models** (`org.roland.model`): JPA entities with Jakarta Persistence annotations and `@Entity` mappings
- **Repository** (`org.roland.model`): Spring Data JPA interfaces extending `JpaRepository` with custom query methods like `findAllByOrderByTimestampDesc()`

This 4-layer structure provides clear separation of concerns:
- **HTTP Layer**: Controllers handle REST requests/responses
- **Data Transfer Layer**: DTOs provide typed contracts for API communication
- **Domain Layer**: Models represent database entities
- **Data Access Layer**: Repository provides database operations

## 📋 Prerequisites

- Docker Desktop (or Docker Engine + Docker Compose)
- Java 21 (for local development)
- Maven 3.9+ (for local builds)
- Git

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/RolleRocker/docker-spring-boot-mysql.git
cd docker-spring-boot-mysql
```

### 2. Configure Environment Variables

```bash
# Copy the example environment file
cp .env.example .env

# Edit .env with secure passwords (IMPORTANT!)
# On Windows: notepad .env
# On Linux/Mac: nano .env

# Change these placeholder values:
# - MYSQL_ROOT_PASSWORD=your_secure_root_password_here
# - MYSQL_PASSWORD=your_secure_app_password_here
# - SPRING_DATASOURCE_PASSWORD=your_secure_app_password_here

# Note: .env is in .gitignore and never committed to Git
```

### 3. Start with Docker Compose

```bash
# Build and start all services
docker-compose up --build -d

# Or use the convenience script:
# Windows (PowerShell):
.\start_docker_services.ps1

# Linux/Mac (Bash):
chmod +x start_docker_services.sh
./start_docker_services.sh
```

### 4. Verify Deployment

```bash
# Check application health
curl http://localhost:8080/actuator/health

# Test the API
curl http://localhost:8080/api/hello

# View logs
docker-compose logs -f app
```

## 🔧 API Endpoints

| Method | Endpoint | Description | Response Type |
| ------ | -------- | ----------- | ------------- |
| `GET` | `/api/hello` | Hello world message with timestamp | `HelloResponse` |
| `GET` | `/api/counter` | Thread-safe atomic counter | `CounterResponse` |
| `GET` | `/api/messages` | Retrieve all messages (newest first) | `List<MessageResponse>` |
| `POST` | `/api/messages` | Create a new message | `MessageResponse` |
| `GET` | `/api/info` | Application info and statistics | `InfoResponse` |
| `GET` | `/actuator/health` | Health check endpoint | JSON |
| `GET` | `/actuator/info` | Application information | JSON |
| `GET` | `/actuator/metrics` | Application metrics | JSON |

### Example Requests

```bash
# Create a message
curl -X POST http://localhost:8080/api/messages \
  -H "Content-Type: application/json" \
  -d '{"content":"Hello from Docker!"}'

# Get all messages
curl http://localhost:8080/api/messages

# Get application info
curl http://localhost:8080/api/info
```

## 🛠️ Development

### Local Development (without Docker)

```bash
# Start MySQL locally or update application.properties
mvn spring-boot:run

# Or build and run the JAR
mvn clean package
java -jar target/simple-java-docker-0.0.1-SNAPSHOT.jar
```

### Running Tests

```bash
# Run all tests with coverage
mvn test

# Generate JaCoCo coverage report
mvn jacoco:report
# Report available at: target/site/jacoco/index.html

# Check for dependency vulnerabilities
mvn org.owasp:dependency-check-maven:check
# Report available at: target/dependency-check-report.html
```

### Docker Commands

```bash
# Build the application image
docker build -t spring-boot-app .

# Start services
docker-compose up -d

# Stop services
docker-compose down

# Stop and remove volumes
docker-compose down -v

# View logs
docker-compose logs -f

# Restart a service
docker-compose restart app

# Execute commands in container
docker exec -it messages-app sh
docker exec -it messages-mysql mysql -u root -p
```

## 🔐 Security Features

### Environment Variables

All sensitive configuration is managed through environment variables loaded from `.env`:

- MySQL root password
- Application database credentials
- JPA configuration

### Security Scanning

- **OWASP Dependency Check**: Scans for known vulnerabilities (fails on CVSS 7+)
- **Docker Scout/Trivy**: Container image vulnerability scanning
- **GitHub Actions**: Automated security checks on every push/PR
- **Gitleaks**: Secret detection in repository

### Run Security Scan Locally

```powershell
# Windows (PowerShell)
.\security-scan.ps1

# Linux/Mac (Bash)
chmod +x security-scan.sh
./security-scan.sh
```

### Security Best Practices Implemented

✅ Non-root container user (`appuser`)  
✅ Multi-stage Docker builds (smaller attack surface)  
✅ Pinned image versions (reproducible builds)  
✅ Health checks for containers  
✅ Environment variable configuration  
✅ `.env` excluded from Git  
✅ HTTPS ready (configure in `application.properties`)  

## 📦 Docker Configuration

### Multi-Stage Build

The Dockerfile uses a two-stage build process:

1. **Build Stage**: Maven 3.9.9 + JDK 21
   - Dependency caching for faster rebuilds
   - Full build environment

2. **Runtime Stage**: Eclipse Temurin 21 JRE Alpine
   - Minimal footprint (~200MB vs ~1GB)
   - Only runtime dependencies
   - Security-hardened

### Docker Compose Services

- **MySQL 8.0.40**: Database with persistent volume
  - Health check enabled
  - Custom init scripts supported
  - Environment-based configuration

- **Spring Boot App**: Java application
  - Waits for MySQL to be healthy
  - Exposed on port 8080
  - Auto-restart enabled

## 🔄 CI/CD

### GitHub Actions Workflows

**Security Scan** (`.github/workflows/security-scan.yml`)

- Triggers: Push, PR, weekly schedule
- Docker image scanning (Trivy)
- OWASP dependency check
- Secret detection (Gitleaks)
- Results uploaded to GitHub Security tab

### Setting Up CI/CD

1. Push to GitHub (already done!)
2. GitHub Actions will run automatically
3. View results in the **Actions** tab
4. Security findings appear in **Security** > **Code scanning alerts**

## 🐛 Troubleshooting

### Application won't start

```bash
# Check logs
docker-compose logs app

# Common issues:
# 1. MySQL not ready - check health status
docker-compose ps

# 2. Port conflict - change port in docker-compose.yml
# 3. Missing .env file - copy from .env.example
```

### Database connection errors

```bash
# Verify MySQL is running
docker exec messages-mysql mysql -u app_user -p messages_db

# Reset database
docker-compose down -v
docker-compose up -d
```

### Build failures

```bash
# Clear Maven cache
mvn clean

# Clear Docker build cache
docker builder prune -a

# Rebuild from scratch
docker-compose down
docker-compose build --no-cache
docker-compose up -d
```

## 📊 Project Structure

```text
.
├── .github/
│   ├── copilot-instructions.md    # AI coding guidelines
│   └── workflows/
│       └── security-scan.yml      # CI/CD security workflow
├── src/
│   ├── main/
│   │   ├── java/org/roland/
│   │   │   ├── controller/        # REST controllers
│   │   │   ├── dto/               # Record-based DTOs with validation
│   │   │   ├── model/             # JPA entities & repositories
│   │   │   └── DemoApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/                      # Unit & integration tests
├── docker-compose.yml             # Service orchestration
├── dockerfile                     # Multi-stage build
├── pom.xml                        # Maven dependencies
├── .env.example                   # Environment template
├── .gitignore                     # Git exclusions
├── security-scan.ps1              # Windows security scanner
├── security-scan.sh               # Linux/Mac security scanner
├── start_docker_services.sh       # Quick start script
└── README.md                      # This file
```

## 🧪 Testing Strategy

- **Unit Tests**: Mockito with `@ExtendWith(MockitoExtension.class)` using DTO-based assertions
- **Integration Tests**: `@WebMvcTest` with `@TestConfiguration` and mocked repositories
- **Coverage**: JaCoCo enforces 80% minimum line coverage
- **Date Handling**: `JavaTimeModule` for `LocalDateTime` serialization in DTOs
- **Validation Testing**: Bean Validation constraints tested with `@Valid` on DTOs

## 📈 Performance & Monitoring

### Actuator Endpoints

Access monitoring endpoints at `/actuator/*`:

- `/health` - Application health status
- `/info` - Build and version information  
- `/metrics` - Runtime metrics

### Database Performance

- Connection pooling via HikariCP
- JPA query optimization with custom repository methods
- Index on timestamp column (auto-created by Hibernate)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style

- Follow Spring Boot conventions
- Maintain 80%+ test coverage
- Run security scans before PR
- Update documentation as needed

## 📝 License

This project is open source and available under the [MIT License](LICENSE).

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Docker community for containerization best practices
- OWASP for security scanning tools
- GitHub Actions for CI/CD automation

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/RolleRocker/docker-spring-boot-mysql/issues)
- **Discussions**: [GitHub Discussions](https://github.com/RolleRocker/docker-spring-boot-mysql/discussions)
- **Documentation**: See [SECURITY_SETUP.md](SECURITY_SETUP.md) for security details

---

## Built with ❤️ using Spring Boot, Docker, and security best practices
