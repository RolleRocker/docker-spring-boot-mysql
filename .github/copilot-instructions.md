# Copilot Instructions - Simple Java Docker Project

## General Preferences
- **Always use PowerShell** for terminal commands (not cmd) - better error handling, modern scripting, and native Windows integration
- Use single-line git commit messages to avoid parsing issues

## Project Overview
Spring Boot 3.4.13 REST API with MySQL persistence, designed as a Docker learning project. Uses multi-stage Dockerfile builds and Docker Compose orchestration.

## Architecture & Components

**3-Layer Structure:**
- **Controllers** (`org.roland.controller`): REST endpoints under `/api` prefix
- **Models** (`org.roland.model`): JPA entities with Jakarta Persistence annotations
- **Repository**: Spring Data JPA interfaces (e.g., `MessageRepository` with custom query methods like `findAllByOrderByTimestampDesc()`)

**Database Integration:**
- MySQL 8.0 via Docker Compose with health checks
- Connection configured through environment variables in compose file
- JPA auto-creates schema (`hibernate.ddl-auto: update`)
- Init SQL scripts in `init.sql/` directory, mounted to `/docker-entrypoint-initdb.d/`

**Entry Points:**
- `DemoApplication.java`: Main Spring Boot application
- `Main.java`: Alternative entry point (legacy, prefer DemoApplication)

## Development Workflows

**Build & Run:**
```bash
# Local Maven build (requires Java 21)
mvn clean package

# Docker-based build & run (recommended)
./start_docker_services.sh  # Builds, starts compose, tails logs
# Or: docker-compose up --build

# Access:
# - App: http://localhost:8080/api/hello
# - Actuator: http://localhost:8080/actuator/health
# - MySQL: localhost:3306
```

**Testing:**
```bash
mvn test                    # Run all tests with JaCoCo coverage (80% minimum)
mvn jacoco:report          # Generate coverage report
```

**Test Strategy:**
- Unit tests (`SimpleControllerTest`): Mock `MessageRepository` with `@ExtendWith(MockitoExtension.class)`
- Integration tests (`SimpleControllerIntegrationTest`): Use `@WebMvcTest` + custom `@TestConfiguration` for mocked repos
- ObjectMapper configured with `JavaTimeModule` for `LocalDateTime` serialization

## Project-Specific Conventions

**Configuration Management:**
- `application.properties`: Base config (server.port, actuator endpoints)
- `database_configuration.env`: MySQL credentials (used by compose services)
- Environment variables override properties at runtime (see compose file)

**Dockerfile Pattern:**
- Multi-stage build: `maven:3.9.6-eclipse-temurin-21` (build) → `eclipse-temurin:21-jre-alpine` (runtime)
- Dependency caching: Copy `pom.xml` first, download deps, then copy source
- Security: Non-root user (`appuser`), health checks via wget

**API Patterns:**
- All endpoints return `ResponseEntity<T>` with explicit status codes
- Timestamps auto-set to `LocalDateTime.now()` on entity creation
- Counter uses `AtomicLong` for thread-safety

## Common Pitfalls

1. **Space in workspace path**: Directory name is "Docker Prodject" (note spacing). Escape paths in scripts.
2. **Duplicate plugins**: `pom.xml` has `spring-boot-maven-plugin` declared twice (lines 61-67) - keep only one.
3. **Repository package mismatch**: `MessageRepository` is in `org.roland.model` but test imports `org.roland.repository` - use correct package.
4. **Compose health dependency**: App waits for MySQL `service_healthy` before starting. If MySQL health check fails, app won't start.
5. **Config file locations**: Multiple config files exist (root `database_configuration.env`, `application_properties.yml` is unused, controller has orphaned `database_configuration.yml`).

## Key Files
- [dockerfile](../dockerfile): Multi-stage build with health checks
- [docker-compose.yml](../docker-compose.yml): Service orchestration
- [pom.xml](../pom.xml): Java 21, Spring Boot 3.4.5, JaCoCo coverage enforcement
- [SimpleController.java](../src/main/java/org/roland/controller/SimpleController.java): Main REST API endpoints
- [start_docker_services.sh](../start_docker_services.sh): One-command startup script
