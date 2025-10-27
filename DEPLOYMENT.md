# Deployment Guide - Portfolio Backend

**Author:** Bernard Uriza Orozco
**Last Updated:** 2025-10-27
**Trello Card:** [PF-DEVOPS-TASK-001](https://trello.com/c/ERYYLMwE)

## Overview

This guide covers deploying the Spring Boot backend to Render.com and the Angular frontend to Netlify.

## Prerequisites

- GitHub account
- Render.com account (free tier)
- Neon PostgreSQL account (free tier) - https://neon.tech
- Netlify account (free tier)
- GitHub Personal Access Token
- Anthropic API Key

---

## Phase 1: Database Setup (Neon PostgreSQL)

### 1.1 Create Neon Database

1. Go to https://console.neon.tech
2. Create new project: **"portfolio-production"**
3. Region: Choose closest to your users (e.g., US East)
4. Database name: **portfolio_db**
5. Copy the **Connection String**

Example format:
```
postgres://user:pass@ep-xxx-xxx.us-east-2.aws.neon.tech/neondb?sslmode=require
```

### 1.2 Verify Database Connection

Test locally (optional):
```bash
psql "postgres://user:pass@ep-xxx.us-east-2.aws.neon.tech/neondb?sslmode=require"
```

---

## Phase 2: Backend Deployment (Render.com)

### 2.1 Connect GitHub Repository

1. Go to https://dashboard.render.com
2. Click **"New +"** → **"Web Service"**
3. Connect your GitHub account
4. Select repository: **portfolio-spring**
5. Branch: **main**

### 2.2 Configure Service Settings

**Basic Settings:**
- Name: `portfolio-backend`
- Region: Oregon (free tier)
- Branch: `main`
- Runtime: Docker
- Instance Type: Free

**Build Settings:**
- Dockerfile Path: `./Dockerfile`
- Docker Context: `.` (root)

### 2.3 Environment Variables

Click **"Environment"** tab and add these variables:

| Variable | Value | Required | Notes |
|----------|-------|----------|-------|
| `SPRING_PROFILES_ACTIVE` | `render` | ✅ | Auto-set by render.yaml |
| `DATABASE_URL` | `postgres://user:pass@...` | ✅ | From Neon (Step 1.1) |
| `GITHUB_USERNAME` | Your GitHub username | ✅ | e.g., `BernardUriza` |
| `GITHUB_TOKEN` | Your GitHub PAT | ✅ | From https://github.com/settings/tokens |
| `ANTHROPIC_API_KEY` | Your Claude API key | ✅ | From https://console.anthropic.com |
| `PORTFOLIO_ADMIN_TOKEN` | Random secure string | ✅ | Generate: `openssl rand -base64 32` |
| `CORS_ALLOWED_ORIGINS` | See below | ✅ | Update after frontend deployment |
| `PORTFOLIO_ADMIN_SECURITY_ENABLED` | `false` | ⚠️ | Set `true` for production |
| `ENABLE_AI_FEATURES` | `true` | Optional | Enable AI curation |
| `ENABLE_LIVE_NARRATION` | `false` | Optional | Keep false initially |
| `ENABLE_FACTORY_RESET` | `false` | Optional | Only enable if needed |

**CORS Configuration:**
```
https://your-app-name.netlify.app,https://yourdomain.com
```
*(Update this after Step 3.2)*

### 2.4 Generate GitHub Personal Access Token

1. Go to https://github.com/settings/tokens
2. Click **"Generate new token (classic)"**
3. Scopes: `public_repo`, `read:user`
4. Copy token and save to Render environment variables

### 2.5 Deploy Backend

1. Click **"Create Web Service"**
2. Render will automatically:
   - Pull code from GitHub
   - Build Docker image
   - Run Flyway migrations
   - Start application on port 8080
3. Wait 5-10 minutes for first build
4. Check logs for errors

### 2.6 Verify Backend Health

Once deployed, check:
```bash
curl https://portfolio-backend.onrender.com/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

**Save your backend URL:** `https://portfolio-backend.onrender.com`

---

## Phase 3: Frontend Deployment (Netlify)

### 3.1 Prepare Frontend Configuration

Navigate to your frontend repository and verify/create these files:

**File: `netlify.toml`** (see Step 3.2 for content)

**File: `src/environments/environment.prod.ts`**
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://portfolio-backend.onrender.com/api'
};
```

### 3.2 Create netlify.toml

In your **frontend repository** root:

```toml
[build]
  command = "npm run build"
  publish = "dist/portfolio-frontend"

[build.environment]
  NODE_VERSION = "20"

[[redirects]]
  from = "/*"
  to = "/index.html"
  status = 200

[[headers]]
  for = "/*"
  [headers.values]
    X-Frame-Options = "DENY"
    X-XSS-Protection = "1; mode=block"
    X-Content-Type-Options = "nosniff"
    Referrer-Policy = "strict-origin-when-cross-origin"
    Permissions-Policy = "geolocation=(), microphone=(), camera=()"
```

### 3.3 Deploy to Netlify

#### Option A: Netlify UI

1. Go to https://app.netlify.com
2. Click **"Add new site"** → **"Import an existing project"**
3. Connect GitHub → Select frontend repository
4. **Build settings:**
   - Build command: `npm run build`
   - Publish directory: `dist/portfolio-frontend`
5. Click **"Deploy"**

#### Option B: Netlify CLI

```bash
cd /path/to/portfolio-frontend
npm install -g netlify-cli
netlify login
netlify init
netlify deploy --prod
```

### 3.4 Update Backend CORS

After frontend deploys:

1. Copy Netlify URL (e.g., `https://bernard-portfolio.netlify.app`)
2. Go to Render Dashboard → portfolio-backend → Environment
3. Update `CORS_ALLOWED_ORIGINS`:
   ```
   https://bernard-portfolio.netlify.app,https://yourdomain.com
   ```
4. Save changes
5. Render will auto-redeploy backend (~2-3 minutes)

---

## Phase 4: Post-Deployment Verification

### 4.1 Backend Smoke Tests

```bash
BACKEND_URL="https://portfolio-backend.onrender.com"

# Health check
curl $BACKEND_URL/actuator/health

# API endpoints
curl $BACKEND_URL/api/projects/starred

# Rate limit status
curl $BACKEND_URL/api/projects/starred/rate-limit

# Admin health (no auth required)
curl $BACKEND_URL/actuator/info
```

### 4.2 Frontend Smoke Tests

1. Open https://your-app.netlify.app
2. Verify:
   - Homepage loads
   - Projects are fetched from backend
   - No CORS errors in console
   - Admin panel accessible (if enabled)

### 4.3 Integration Tests

Test end-to-end flow:
1. Navigate to projects page
2. Verify starred repos load
3. Test filtering by language
4. Check admin features (if enabled)

---

## Phase 5: Production Hardening

### 5.1 Enable Admin Security

Once verified working:

1. Render → Environment → Set `PORTFOLIO_ADMIN_SECURITY_ENABLED=true`
2. Generate strong admin token: `openssl rand -base64 32`
3. Update `PORTFOLIO_ADMIN_TOKEN` with new token
4. Update frontend to send `X-Admin-Token` header

### 5.2 Configure Custom Domain (Optional)

**Netlify:**
1. Netlify Dashboard → Domain settings
2. Add custom domain: `bernarduriza.com`
3. Configure DNS records (A/CNAME)

**Render:**
1. Render Dashboard → Settings → Custom Domain
2. Add: `api.bernarduriza.com`
3. Update frontend API URL

### 5.3 Enable HTTPS Redirect

Both Netlify and Render auto-enable HTTPS. Verify:
```bash
curl -I http://your-app.netlify.app
# Should return 301 redirect to https://
```

### 5.4 Monitor Application

**Render Logs:**
```bash
# Via dashboard or CLI
render logs portfolio-backend
```

**Netlify Logs:**
- Netlify Dashboard → Deploys → View logs

**Sentry (Optional):**
Configure error tracking:
```bash
# Backend
SENTRY_DSN=https://xxx@sentry.io/xxx
```

---

## Troubleshooting

### Backend won't start

**Check logs:**
```bash
# Render Dashboard → Logs
```

**Common issues:**
- `DATABASE_URL` malformed → Verify format from Neon
- `GITHUB_TOKEN` invalid → Regenerate token
- Flyway migration failed → Check database state

**Fix Flyway:**
```sql
-- Connect to Neon via psql
SELECT * FROM flyway_schema_history;
-- If stuck, reset:
-- TRUNCATE flyway_schema_history; (use with caution)
```

### CORS Errors

**Symptoms:**
```
Access to XMLHttpRequest at 'https://backend...' from origin 'https://frontend...'
has been blocked by CORS policy
```

**Fix:**
1. Verify `CORS_ALLOWED_ORIGINS` includes your Netlify URL
2. Check for trailing slashes (should NOT have them)
3. Redeploy backend after changes

### Health Check Failing

**Render shows "Deploy failed":**
- Health check timeout (30s default)
- Application not binding to `$PORT`
- Database connection failed

**Debug:**
```bash
# Check if port is correct
grep "server.port" src/main/resources/application-render.properties

# Verify entrypoint script
cat render-entrypoint.sh | grep PORT
```

### Frontend API calls failing

**Check:**
1. `environment.prod.ts` has correct backend URL
2. Backend URL does NOT have trailing slash
3. Network tab shows 200 responses (not 404/500)

**Test directly:**
```bash
curl https://portfolio-backend.onrender.com/api/projects/starred
```

---

## Rollback Procedure

### Render Rollback

1. Dashboard → Deploys
2. Find last working deploy
3. Click **"Rollback to this version"**
4. Confirm rollback

### Netlify Rollback

1. Dashboard → Deploys
2. Find previous deploy
3. Click **"Publish deploy"**

### Database Rollback

**Flyway rollback** (not supported out-of-box):
- Restore from Neon backup
- Write custom down-migration scripts

---

## Monitoring & Maintenance

### Weekly Tasks

- [ ] Check Render logs for errors
- [ ] Verify GitHub API rate limit
- [ ] Check database disk usage (Neon dashboard)
- [ ] Review Anthropic token budget

### Monthly Tasks

- [ ] Review and rotate admin tokens
- [ ] Update dependencies: `./mvnw versions:display-dependency-updates`
- [ ] Check for security vulnerabilities: `./mvnw org.owasp:dependency-check-maven`

---

## Environment Variables Reference

### Required Variables

| Variable | Example | Where to Get |
|----------|---------|--------------|
| `DATABASE_URL` | `postgres://user:pass@...` | Neon console |
| `GITHUB_USERNAME` | `BernardUriza` | Your GitHub profile |
| `GITHUB_TOKEN` | `ghp_xxxxx` | https://github.com/settings/tokens |
| `ANTHROPIC_API_KEY` | `sk-ant-xxxxx` | https://console.anthropic.com |
| `PORTFOLIO_ADMIN_TOKEN` | Random 32-char string | `openssl rand -base64 32` |
| `CORS_ALLOWED_ORIGINS` | `https://app.netlify.app` | After Netlify deploy |

### Optional Variables

| Variable | Default | Purpose |
|----------|---------|---------|
| `ENABLE_AI_FEATURES` | `false` | Enable Claude AI curation |
| `ENABLE_LIVE_NARRATION` | `false` | Enable real-time narration |
| `ENABLE_FACTORY_RESET` | `false` | Enable factory reset endpoint |
| `CLAUDE_DAILY_TOKEN_BUDGET` | `100000` | Daily Claude API token limit |

---

## Security Checklist

- [ ] `PORTFOLIO_ADMIN_SECURITY_ENABLED=true` in production
- [ ] Admin token is strong (32+ chars, random)
- [ ] CORS only allows your domains
- [ ] `.env` files NOT committed to git
- [ ] GitHub token has minimal scopes (public_repo only)
- [ ] Database uses SSL (`sslmode=require`)
- [ ] Factory reset disabled in production
- [ ] Rate limiting enabled

---

## Cost Breakdown (Free Tier)

| Service | Free Tier | Limits |
|---------|-----------|--------|
| **Render.com** | Free | 750 hours/month, sleeps after 15min inactivity |
| **Neon PostgreSQL** | Free | 10 GB storage, 1 project |
| **Netlify** | Free | 100 GB bandwidth/month |
| **GitHub** | Free | Public repos unlimited |
| **Total** | $0/month | ✅ Fully free for MVP |

**Upgrade Path:**
- Render Starter: $7/month (no sleep, better performance)
- Neon Pro: $19/month (more storage, higher limits)

---

## Next Steps

After successful deployment:

1. **Custom Domain:** Configure `bernarduriza.com`
2. **Analytics:** Add Google Analytics/Plausible
3. **SEO:** Add meta tags, sitemap.xml
4. **CI/CD:** Setup auto-deploy on push to main
5. **Monitoring:** Configure Sentry for error tracking

---

## Support & Resources

- **Render Docs:** https://render.com/docs
- **Netlify Docs:** https://docs.netlify.com
- **Neon Docs:** https://neon.tech/docs
- **Trello Board:** [AI Portfolio Sprint 1](https://trello.com/b/68fcf05e481843db13204397)
- **GitHub Issues:** Report bugs via GitHub Issues

---

**Deployment Status:** 🟡 Ready for Testing
**Next Review:** After first successful deployment
**Owner:** Bernard Uriza Orozco
