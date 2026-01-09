# Docker Security Setup - Recommended Configuration 🔒

## What Should Be Configured:

### ✅ 1. Environment Variables (.env)
- **Template Available**: `.env.example` is provided as a template
- **Setup Required**: Copy `.env.example` to `.env` before first run
- **Security**: `.gitignore` excludes `.env` files from Git (never commit secrets!)
- **Configuration**: Update with your own secure passwords before deployment

### ✅ 2. Security Scanning Tools
- **Available**: `security-scan.sh` (Bash) and `security-scan.ps1` (PowerShell) for local scanning
- **CI/CD Configured**: `.github/workflows/security-scan.yml` runs automated security checks on GitHub
- **Maven Plugin**: OWASP Dependency Check included in `pom.xml` for vulnerability scanning
- **Container Security**: Use Docker Scout or Trivy for image vulnerability scanning

### ✅ 3. Docker Image Versions
- **MySQL**: 8.0.40 (latest secure version)
- **Maven**: 3.9.9 (latest stable build tool)
- **JRE**: 21.0.5_11-jre-alpine (pinned for reproducibility)

## 🚀 Getting Started:

### Step 1: Create Your Environment Configuration
```bash
# Copy the template
cp .env.example .env

# Edit with your secure passwords
# Windows: notepad .env
# Linux/Mac: nano .env

# Required changes:
# - MYSQL_ROOT_PASSWORD: Change from placeholder
# - MYSQL_PASSWORD: Change from placeholder  
# - SPRING_DATASOURCE_PASSWORD: Match MYSQL_PASSWORD
```

### Step 2: Rebuild and Start Services
```bash
# Stop any running services
docker-compose down

# Start with new configuration
docker-compose up --build -d
```

### Step 3: Run Security Scan
```powershell
# On Windows (PowerShell)
.\security-scan.ps1

# On Linux/Mac (Bash)
chmod +x security-scan.sh
./security-scan.sh
```

### Step 4: Run OWASP Dependency Check
```bash
mvn org.owasp:dependency-check-maven:check
# View report: target/dependency-check-report.html
```

## � Security Best Practices:

1. **Environment Variables**: All secrets stored in `.env` (not committed to Git)
2. **Version Pinning**: Specific image versions prevent unexpected updates
3. **Git Protection**: `.env` is in `.gitignore` - never commit passwords
4. **Automated Scanning**: GitHub Actions security workflows run automatically
5. **CVE Threshold**: Builds fail on CVSS 7+ vulnerabilities (prevents deployment of insecure code)
6. **Non-Root Containers**: Application runs as unprivileged `appuser`
7. **Multi-Stage Builds**: Only runtime dependencies in final image (smaller attack surface)

## 📝 Setup Checklist:

- [ ] Copy `.env.example` to `.env`
- [ ] Update passwords in `.env` with secure values
- [ ] Run `docker-compose down && docker-compose up --build -d`
- [ ] Verify `.env` is NOT in `git status` output
- [ ] Test security scan: `.\security-scan.ps1` (Windows) or `./security-scan.sh` (Linux/Mac)
- [ ] Run dependency check: `mvn org.owasp:dependency-check-maven:check`
- [ ] Push to GitHub to trigger CI/CD security workflows

## 🛠️ Optional Security Tools:

### Docker Scout (Recommended)
```bash
# View container image vulnerabilities
docker scout quickview

# Full CVE analysis
docker scout cves
```

### Trivy (Container Scanning)
```powershell
# Windows
choco install trivy
trivy image <image-name>
```

```bash
# Linux
curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sh -s -- -b /usr/local/bin
trivy image <image-name>
```

### Snyk (Dependency Management)
```bash
npm install -g snyk
snyk test  # Test dependencies
snyk monitor  # Continuous monitoring
```

## ⚠️ Important Warnings:

- **NEVER commit `.env`** to Git
- **Change default passwords** before deployment
- **Run security scans regularly** (weekly minimum)
- **Review OWASP reports** after dependency changes
- **Update images monthly** or when CVEs are published

---

Your application is now significantly more secure! 🎉
