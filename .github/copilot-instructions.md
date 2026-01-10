# Copilot Instructions - Simple Java Docker Project

## General Preferences
- **Always use PowerShell** for terminal commands (not cmd) - better error handling, modern scripting, and native Windows integration
- Use single-line git commit messages to avoid parsing issues

## Project Overview
Spring Boot 3.5.0 REST API with MySQL persistence, designed as a Docker learning project. Uses multi-stage Dockerfile builds and Docker Compose orchestration.

**Support Timeline:**
- OSS Support: Until 2026-08-31
- Commercial Support (Tanzu Spring Runtime): Until 2027-08-31

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
- `DemoApplication.java`: **PRIMARY** - Main Spring Boot application with `@SpringBootApplication` annotation. Use this for all Docker and production deployments.
- `Main.java`: Legacy placeholder class (IntelliJ template). DO NOT USE - this is a demo file only and does not start the Spring Boot app.

## Development Workflows

**Build & Run:**
```powershell
# Local Maven build (requires Java 21)
mvn clean package

# Docker-based build & run (RECOMMENDED on Windows)
.\start_docker_services.ps1  # Builds, starts compose, tails logs
# Or manually: docker-compose up --build

# Access:
# - App: http://localhost:8080/api/hello
# - Actuator: http://localhost:8080/actuator/health
# - MySQL: localhost:3306
```

**PowerShell Startup Script:**
- Use `start_docker_services.ps1` on Windows (handles paths with spaces properly)
- Script automatically builds, starts containers, and tails application logs
- Press Ctrl+C to stop log tailing (containers remain running)

**Testing:**
```bash
mvn test                    # Run all tests with JaCoCo coverage (80% minimum)
mvn jacoco:report          # Generate coverage report
```

**Jackson LocalDateTime Serialization (Critical for Tests):**
```java
@TestConfiguration
static class TestConfig {
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
```
- **Must register `JavaTimeModule`** to handle `LocalDateTime` serialization in JSON responses
- **Disable `WRITE_DATES_AS_TIMESTAMPS`** to serialize as ISO-8601 strings instead of milliseconds
- Mock the `ObjectMapper` bean in `@TestConfiguration` to ensure test consistency
- Without this config, LocalDateTime fields will fail to serialize in MockMvc tests

**Test Strategy:**
- Unit tests (`SimpleControllerTest`): Mock `MessageRepository` with `@ExtendWith(MockitoExtension.class)`
- Integration tests (`SimpleControllerIntegrationTest`): Use `@WebMvcTest` + custom `@TestConfiguration` for mocked repos

## Project-Specific Conventions

**Configuration Management:**
- `application.properties`: Base Spring config (server.port=8080, actuator endpoints)
- `database_configuration.env`: Single source of truth for all Docker environment variables (MySQL credentials, Spring datasource URLs)
- Environment variables override properties at runtime (docker-compose.yml references .env)
- **DEPRECATED**: `application_properties.yml` and `database_configuration.yml` have been removed from the project

**Dockerfile Pattern:**
- Multi-stage build: `maven:3.9.6-eclipse-temurin-21` (build) → `eclipse-temurin:21-jre-alpine` (runtime)
- Dependency caching: Copy `pom.xml` first, download deps, then copy source
- Security: Non-root user (`appuser`), health checks via wget

**API Patterns:**
- All endpoints return `ResponseEntity<T>` with explicit status codes
- Timestamps auto-set to `LocalDateTime.now()` on entity creation
- Counter uses `AtomicLong` for thread-safety

## Common Pitfalls

1. **Space in workspace path**: Directory name is "Docker Prodject" (note spacing). All scripts handle this - use PowerShell scripts, not bash.

2. **Repository package names**: `MessageRepository` is in `org.roland.model` - always import from here, NOT `org.roland.repository`.

3. **Entry point confusion**: Only `DemoApplication.java` starts the Spring Boot app. `Main.java` is a legacy placeholder and does nothing.

4. **LocalDateTime serialization fails in tests**: Must configure Jackson with `JavaTimeModule` and `disable(WRITE_DATES_AS_TIMESTAMPS)` in `@TestConfiguration`. See Test Strategy section above.

5. **MySQL health check failures**: If the app container doesn't start, check these diagnostics:
   ```powershell
   docker-compose ps                    # Check service status
   docker-compose logs mysql           # MySQL init errors
   docker-compose logs app             # App startup errors
   docker ps -a                        # View all containers
   ```
   Common causes:
   - MySQL init script failure (check `initial_setup.sql`)
   - Port 3306 already in use
   - Insufficient disk space for MySQL volume
   - Health check timeout (default 10s, may need increase for slow systems)

6. **Config file consolidation**: All environment variables go in `database_configuration.env`. Don't create `application_properties.yml` or `database_configuration.yml` - they create confusion and override issues.

## Key Files
- **dockerfile**: Multi-stage build with health checks
- **docker-compose.yml**: Service orchestration
- **pom.xml**: Java 21, Spring Boot 3.5.9, JaCoCo coverage enforcement (80% minimum)
- **DemoApplication.java**: **PRIMARY** Spring Boot entry point (src/main/java/org/roland/DemoApplication.java)
- **SimpleController.java**: Main REST API endpoints (src/main/java/org/roland/controller/SimpleController.java)
- **start_docker_services.ps1**: Windows PowerShell startup script (recommended)
- **start_docker_services.sh**: Bash startup script (legacy)
