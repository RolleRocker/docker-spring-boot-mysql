# NVD API Key Setup Guide

## Why You Need This

The OWASP Dependency Check tool queries the National Vulnerability Database (NVD) to check for known security vulnerabilities in your project dependencies. Without an API key, the scan can take **30+ minutes** because it must download the entire vulnerability database each time.

With an API key, scans complete in **2-5 minutes**! 🚀

## Step 1: Request an NVD API Key

1. Visit: https://nvd.nist.gov/developers/request-an-api-key
2. Fill out the form with your email address
3. Check your email for the API key (arrives within minutes)
4. **Save the API key** - you'll need it in the next step

## Step 2: Add API Key to GitHub Secrets

### Via GitHub Web Interface:

1. Go to your repository: https://github.com/RolleRocker/docker-spring-boot-mysql
2. Click **Settings** (top right)
3. In the left sidebar, click **Secrets and variables** → **Actions**
4. Click **New repository secret**
5. Enter:
   - **Name**: `NVD_API_KEY`
   - **Secret**: [paste your API key here]
6. Click **Add secret**

### Via GitHub CLI (alternative):

**Windows (PowerShell):**
```powershell
# Install GitHub CLI if needed
winget install GitHub.cli

# Authenticate (opens browser for login)
gh auth login

# Add the secret
gh secret set NVD_API_KEY -b "YOUR_API_KEY_HERE" -R RolleRocker/docker-spring-boot-mysql
```

**Linux/Mac (Bash):**
```bash
# Install GitHub CLI if needed
# Mac: brew install gh
# Linux: curl -fsSLo /usr/local/bin/gh.tgz https://github.com/cli/cli/releases/download/v2.0.0/gh_2.0.0_linux_amd64.tar.gz

# Authenticate (opens browser for login)
gh auth login

# Add the secret
gh secret set NVD_API_KEY -b "YOUR_API_KEY_HERE" -R RolleRocker/docker-spring-boot-mysql
```

## Step 3: Verify It Works

The GitHub Actions workflow is already configured to use the API key. To verify:

1. Push a commit or manually trigger the workflow:
   - Go to **Actions** tab
   - Select **Security Scan** workflow
   - Click **Run workflow** → **Run workflow**

2. Watch the "Maven Dependency Scan" job
3. You should see: `Using NVD API Key for faster updates`
4. The scan should complete much faster!

## For Local Development

To use the API key locally when running security scans:

### Windows (PowerShell):
```powershell
# Set environment variable temporarily
$env:NVD_API_KEY="your_key_here"

# Run the scan
mvn org.owasp:dependency-check-maven:check -DnvdApiKey=$env:NVD_API_KEY
```

### Linux/Mac (Bash):
```bash
# Set environment variable temporarily
export NVD_API_KEY="your_key_here"

# Run the scan
mvn org.owasp:dependency-check-maven:check -DnvdApiKey=$NVD_API_KEY
```

### Permanent (add to your shell profile):

**Windows - PowerShell Profile:**
```powershell
# Edit profile
notepad $PROFILE

# Add this line:
$env:NVD_API_KEY="your_key_here"
```

**Linux/Mac - ~/.bashrc or ~/.zshrc:**
```bash
export NVD_API_KEY="your_key_here"
```

## Alternative: Update pom.xml (Not Recommended)

You can add the API key to `pom.xml`, but **this is NOT recommended** because:
- ❌ API keys would be committed to version control
- ❌ Security risk if repository is public
- ❌ Keys visible in build logs

If you must use this approach (for private repos only):

```xml
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>10.0.4</version>
    <configuration>
        <nvdApiKey>${env.NVD_API_KEY}</nvdApiKey>
        <!-- Other config -->
    </configuration>
</plugin>
```

## API Key Limits

- **Free tier**: 5 requests per 30-second rolling window
- **More than enough** for typical CI/CD usage
- The tool caches results, so repeated runs are fast

## Troubleshooting

### "Warning: No NVD API Key found"
- The workflow will still work, just slower
- Add the secret to GitHub (see Step 2 above)

### "API Key Invalid"
- Double-check you copied the full key from the email
- Make sure there are no extra spaces
- Request a new key if needed

### Scans still slow with API key
- Check GitHub Actions logs - look for "Using NVD API Key"
- Verify the secret name is exactly `NVD_API_KEY`
- First scan after key setup may still be slow (downloading data)
- Subsequent scans will be much faster due to caching

## Resources

- [NVD API Key Request](https://nvd.nist.gov/developers/request-an-api-key)
- [OWASP Dependency Check Documentation](https://jeremylong.github.io/DependencyCheck/dependency-check-maven/)
- [GitHub Secrets Documentation](https://docs.github.com/en/actions/security-guides/encrypted-secrets)

---

**Need Help?** [Open an issue](https://github.com/RolleRocker/docker-spring-boot-mysql/issues)
