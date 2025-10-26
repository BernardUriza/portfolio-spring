# CI/CD Pipeline Documentation

> Complete guide to the Continuous Integration and Continuous Deployment pipeline for Portfolio Backend

## 📋 Overview

This project uses **GitHub Actions** for automated testing, security scanning, building, and deployment. The pipeline runs on every push and pull request, ensuring code quality and reliability.

## 🏗️ Pipeline Architecture

### Backend Pipeline (`portfolio-spring`)

**Workflow File**: `.github/workflows/ci.yml`

```
┌─────────────────────────────────────────────────────────────────┐
│                      BACKEND CI/CD PIPELINE                     │
└─────────────────────────────────────────────────────────────────┘

┌──────────────┐
│  Push/PR     │  Triggers: main, develop, prod branches
└──────┬───────┘
       │
       ├─────────┐
       │         │
┌──────▼─────────▼──────────────────────────────────────────────┐
│                    PARALLEL JOBS                               │
├────────────────────────────────────────────────────────────────┤
│                                                                │
│  ┌────────────────┐  ┌────────────────┐  ┌─────────────────┐ │
│  │ Backend Tests  │  │  Code Quality  │  │ Security Scan   │ │
│  │ ✓ PostgreSQL   │  │  ✓ Compile     │  │ ✓ Dep Review    │ │
│  │ ✓ Integration  │  │  ✓ Enforcer    │  │ ✓ Dep Check     │ │
│  │ ✓ JaCoCo       │  └────────────────┘  └─────────────────┘ │
│  └────────┬───────┘                                            │
│           │                                                    │
│  ┌────────▼───────┐                                            │
│  │ Backend Build  │                                            │
│  │ ✓ Maven Package│                                            │
│  │ ✓ Upload JAR   │                                            │
│  └────────┬───────┘                                            │
└───────────┼────────────────────────────────────────────────────┘
            │
            ├─────────────────┬──────────────────┐
            │                 │                  │
     ┌──────▼──────┐  ┌───────▼────────┐  ┌─────▼──────────┐
     │   Staging   │  │  Production    │  │   Approval     │
     │  (develop)  │  │  (main/prod)   │  │   Required     │
     │ Auto Deploy │  │ Manual Approve │  │  for Prod      │
     └─────────────┘  └────────────────┘  └────────────────┘
```

### Jobs Breakdown

#### 1. **backend-test** (Primary Job)
- **Runtime**: ~3-5 minutes
- **PostgreSQL Service**: 16-alpine container
- **Steps**:
  1. Checkout code
  2. Setup JDK 21 (Temurin)
  3. Cache Maven packages
  4. Run tests with PostgreSQL
  5. Generate JaCoCo coverage report
  6. Upload coverage artifacts
  7. Check 60% coverage threshold

**Environment Variables**:
```yaml
SPRING_PROFILES_ACTIVE: test
SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/portfolio_test
SPRING_DATASOURCE_USERNAME: test
SPRING_DATASOURCE_PASSWORD: test
```

#### 2. **backend-build** (Depends on: backend-test)
- **Runtime**: ~2-3 minutes
- **Steps**:
  1. Checkout code
  2. Setup JDK 21
  3. Build with Maven (`mvn clean package -DskipTests`)
  4. Upload JAR artifact

**Artifacts**:
- `portfolio-spring-jar` - Deployable JAR file

#### 3. **code-quality** (Depends on: backend-test)
- **Runtime**: ~1-2 minutes
- **Steps**:
  1. Checkout code
  2. Setup JDK 21
  3. Compile code
  4. Run Maven Enforcer plugin

#### 4. **dependency-review** (PRs only)
- **Runtime**: <1 minute
- **Purpose**: Review dependency changes for security risks
- **Uses**: `actions/dependency-review-action@v3`

#### 5. **security-scan** (Depends on: backend-test)
- **Runtime**: ~1-2 minutes
- **Steps**:
  1. Run `mvn dependency:tree`
  2. Run `mvn dependency:analyze`
  3. Check for known vulnerabilities

#### 6. **deploy-staging** (develop branch only)
- **Trigger**: Push to `develop`
- **Requires**: backend-build, code-quality, security-scan
- **Environment**: Staging (Render)
- **Steps**:
  1. Download JAR artifact
  2. Deploy to Render staging via deploy hook

**Secrets Required**:
- `RENDER_STAGING_DEPLOY_HOOK`

#### 7. **deploy-production** (main/prod only + Approval)
- **Trigger**: Push to `main` or `prod`
- **Requires**: backend-build, code-quality, security-scan
- **Environment**: `production` (requires manual approval)
- **URL**: https://portfolio-spring.onrender.com
- **Steps**:
  1. Download JAR artifact
  2. Deploy to Render production via deploy hook
  3. Notify deployment success

**Secrets Required**:
- `RENDER_PROD_DEPLOY_HOOK`

---

### Frontend Pipeline (`portfolio-frontend`)

**Workflow File**: `.github/workflows/ci.yml`

```
┌─────────────────────────────────────────────────────────────────┐
│                     FRONTEND CI/CD PIPELINE                     │
└─────────────────────────────────────────────────────────────────┘

┌──────────────┐
│  Push/PR     │  Triggers: main, prod, develop branches
└──────┬───────┘
       │
┌──────▼─────────────────────────────────────────────────────────┐
│              Frontend Build & Test (Matrix)                    │
│              Node 18.x + 20.x                                  │
│  ✓ Install dependencies (npm ci)                              │
│  ✓ Lint code                                                   │
│  ✓ Build application (npm run build)                          │
│  ✓ Upload artifacts (Node 20.x only)                          │
└──────┬─────────────────────────────────────────────────────────┘
       │
       ├─────────────────┬──────────────────┬───────────────────┐
       │                 │                  │                   │
┌──────▼──────┐  ┌───────▼────────┐  ┌─────▼──────────┐  ┌─────▼────────┐
│  E2E Tests  │  │ Code Quality   │  │  Dep Review    │  │   Staging    │
│  Playwright │  │ Security Audit │  │  (PRs only)    │  │  (develop)   │
│  Chromium   │  │ npm audit      │  └────────────────┘  │ Auto Deploy  │
└──────┬──────┘  └────────────────┘                      └──────────────┘
       │
       │
┌──────▼──────────┐
│   Production    │
│  (main/prod)    │
│ Manual Approve  │
└─────────────────┘
```

### Jobs Breakdown

#### 1. **frontend-build-test** (Matrix: Node 18.x, 20.x)
- **Runtime**: ~3-4 minutes per Node version
- **Steps**:
  1. Checkout code
  2. Setup Node.js (matrix version)
  3. Install dependencies (`npm ci --legacy-peer-deps`)
  4. Lint code (if configured)
  5. Build application
  6. Upload build artifacts (Node 20.x only)

**Artifacts**:
- `frontend-dist` - Built Angular application

#### 2. **e2e-tests** (Depends on: frontend-build-test)
- **Runtime**: ~5-10 minutes
- **Browser**: Chromium only (for speed)
- **Steps**:
  1. Checkout code
  2. Setup Node.js 20.x
  3. Install dependencies
  4. Install Playwright browsers
  5. Start backend service (mock or real)
  6. Run E2E tests (`npm run e2e:chromium`)
  7. Upload test results and videos on failure

**Artifacts**:
- `playwright-report` - HTML test report
- `test-videos` - Failure videos (if tests fail)

#### 3. **code-quality** (Parallel)
- **Runtime**: ~1-2 minutes
- **Steps**:
  1. Run `npm audit` (moderate level)
  2. Check for outdated dependencies

#### 4. **dependency-review** (PRs only)
- **Runtime**: <1 minute
- **Uses**: `actions/dependency-review-action@v3`

#### 5. **deploy-staging** (develop only)
- **Trigger**: Push to `develop`
- **Requires**: frontend-build-test, code-quality
- **Steps**:
  1. Download build artifacts
  2. Deploy to staging (Netlify/Vercel/Render)

**Secrets Required**:
- `STAGING_DEPLOY_TOKEN`

#### 6. **deploy-production** (main/prod + Approval)
- **Trigger**: Push to `main` or `prod`
- **Requires**: frontend-build-test, e2e-tests, code-quality
- **Environment**: `production` (requires manual approval)
- **Steps**:
  1. Download build artifacts
  2. Deploy to production
  3. Notify deployment success

**Secrets Required**:
- `PROD_DEPLOY_TOKEN`

---

## 🔐 Required GitHub Secrets

### Backend Secrets

| Secret Name | Purpose | Example |
|-------------|---------|---------|
| `RENDER_STAGING_DEPLOY_HOOK` | Staging deployment URL | `https://api.render.com/deploy/srv-xxx` |
| `RENDER_PROD_DEPLOY_HOOK` | Production deployment URL | `https://api.render.com/deploy/srv-yyy` |
| `GITHUB_TOKEN` | GitHub API access (auto-provided) | Auto-generated |

### Frontend Secrets

| Secret Name | Purpose | Example |
|-------------|---------|---------|
| `STAGING_DEPLOY_TOKEN` | Staging deployment token | Netlify/Vercel token |
| `PROD_DEPLOY_TOKEN` | Production deployment token | Netlify/Vercel token |

### How to Add Secrets

1. Go to repository **Settings** → **Secrets and variables** → **Actions**
2. Click **New repository secret**
3. Enter secret name and value
4. Click **Add secret**

---

## 🤖 Dependabot Configuration

Both repositories use Dependabot for automated dependency updates.

**Config File**: `.github/dependabot.yml`

### Update Schedule
- **Day**: Monday
- **Time**: 09:00 UTC
- **Frequency**: Weekly

### Backend Updates
- Maven dependencies (10 PRs max)
- GitHub Actions (5 PRs max)

### Frontend Updates
- npm dependencies (10 PRs max)
  - Ignores Angular major version updates
- GitHub Actions (5 PRs max)

### Labels Applied
- `dependencies`
- `backend` or `frontend`
- `security`
- `github-actions` (for Actions updates)

---

## 🚀 Deployment Workflow

### Staging Deployment (Auto)

1. **Create feature branch** from `develop`
   ```bash
   git checkout -b feature/my-feature develop
   ```

2. **Make changes and commit**
   ```bash
   git add .
   git commit -m "feat: add new feature"
   ```

3. **Push to develop** (triggers staging deploy)
   ```bash
   git push origin feature/my-feature
   # Create PR to develop
   # Merge PR → Auto-deploys to staging
   ```

4. **Staging URL**: Check GitHub Actions logs for deployment URL

---

### Production Deployment (Manual Approval)

1. **Merge to main/prod** from `develop`
   ```bash
   git checkout main
   git merge develop
   git push origin main
   ```

2. **Pipeline runs** all tests and quality checks

3. **Approval required** - Go to GitHub Actions
   - Click on the workflow run
   - Go to **deploy-production** job
   - Click **Review deployments**
   - Click **Approve and deploy**

4. **Production deployed** - Monitor logs for success

---

## 📊 Monitoring & Troubleshooting

### View Pipeline Status

**GitHub Actions Tab**:
```
https://github.com/BernardUriza/portfolio-spring/actions
https://github.com/BernardUriza/portfolio-frontend/actions
```

### Common Issues

#### Backend Tests Failing
```bash
# Check PostgreSQL connection
# Verify test database credentials in ci.yml
SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/portfolio_test
```

#### Frontend E2E Tests Timeout
```bash
# Increase timeout in playwright.config.ts
timeout: 90000  # 90 seconds
```

#### Deployment Hook Fails
```bash
# Verify secret is set correctly
# Check Render dashboard for deploy hook URL
# Ensure deploy hook is enabled
```

### Download Artifacts

1. Go to **Actions** tab
2. Click on workflow run
3. Scroll to **Artifacts** section
4. Download:
   - `jacoco-report` (Coverage)
   - `portfolio-spring-jar` (Backend JAR)
   - `frontend-dist` (Frontend build)
   - `playwright-report` (E2E test results)

---

## 🎯 Pipeline Metrics

### Target Performance

| Metric | Backend | Frontend |
|--------|---------|----------|
| Total Runtime | ~8-10 min | ~10-12 min |
| Test Execution | ~3-5 min | ~5-10 min (E2E) |
| Build Time | ~2-3 min | ~3-4 min |
| Coverage Threshold | 60% | N/A |

### Success Criteria

- ✅ All tests pass
- ✅ Code coverage ≥60% (backend)
- ✅ No critical security vulnerabilities
- ✅ Build artifacts generated
- ✅ Deployment successful

---

## 🔄 Future Enhancements

- [ ] Add Snyk security scanning
- [ ] Implement blue-green deployments
- [ ] Add smoke tests post-deployment
- [ ] Set up monitoring alerts (Sentry, Datadog)
- [ ] Implement rollback mechanism
- [ ] Add performance testing with Lighthouse (frontend)
- [ ] Add load testing with Gatling (backend)
- [ ] Implement canary deployments

---

## 📚 Additional Resources

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Dependabot Documentation](https://docs.github.com/en/code-security/dependabot)
- [Render Deployment Hooks](https://render.com/docs/deploy-hooks)
- [JaCoCo Coverage](https://www.jacoco.org/jacoco/)
- [Playwright CI](https://playwright.dev/docs/ci)

---

**Last Updated**: 2025-10-25 | **Sprint**: Sprint 3 - CI/CD Implementation
**Status**: ✅ Operational | **Pipeline Version**: 1.0
