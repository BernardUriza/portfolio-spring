# Render Deployment Setup Guide

This guide provides step-by-step instructions for deploying the Spring Boot backend to Render.com.

## Prerequisites

- Render.com account (free tier works)
- GitHub repository connected to Render
- GitHub Personal Access Token
- Anthropic API Key (for AI features)

## Architecture

The deployment uses:
- **Dockerfile**: Multi-stage build with JDK 21
- **render-entrypoint.sh**: Intelligent startup script that:
  - Converts Render's `DATABASE_URL` format to Spring Boot JDBC format
  - Extracts credentials automatically
  - Handles URL decoding and SSL parameters
  - Optimizes JVM for Render's 512MB free tier
- **application-render.properties**: Render-specific Spring configuration

## Environment Variables Configuration

### Required Variables

Configure these in Render Dashboard → Service → Environment:

```bash
# ==========================================
# GitHub API Configuration
# ==========================================
GITHUB_USERNAME=BernardUriza
GITHUB_TOKEN=ghp_your_token_here

# ==========================================
# Anthropic Claude API
# ==========================================
ANTHROPIC_API_KEY=sk-ant-api03-your_key_here

# ==========================================
# Admin Security
# ==========================================
PORTFOLIO_ADMIN_TOKEN=your-secure-admin-token
PORTFOLIO_ADMIN_SECURITY_ENABLED=true

# ==========================================
# CORS Configuration
# ==========================================
# Replace with your actual Netlify frontend URL
CORS_ALLOWED_ORIGINS=https://your-app.netlify.app,https://bernarduriza.com

# ==========================================
# Feature Flags (Optional - defaults shown)
# ==========================================
ENABLE_AI_FEATURES=false
ENABLE_LIVE_NARRATION=false
ENABLE_FACTORY_RESET=false
ADMIN_RESET_TOKEN=

# Auto Sync Features
FEATURE_AUTO_SYNC_ENABLED=true
FEATURE_MANUAL_SYNC_ENABLED=true
FEATURE_SCHEDULED_SYNC_ENABLED=false

# AI Curation Features
FEATURE_AI_CURATION_ENABLED=false
FEATURE_MANUAL_CURATION_ENABLED=true
```

### Auto-Configured Variables

Render automatically provides these (do NOT set manually):

- `DATABASE_URL`: Auto-generated when you add a PostgreSQL database
- `PORT`: Dynamically assigned by Render
- `SPRING_PROFILES_ACTIVE`: Set to `render` by Dockerfile

## Database Configuration

### Option 1: Using Render Blueprint (render.yaml)

The included `render.yaml` automatically provisions:
- PostgreSQL database (`portfolio-db`)
- Web service with correct environment linkage

Deploy via dashboard: "New" → "Blueprint" → Select this repository

### Option 2: Manual Setup

1. Create PostgreSQL database:
   - Dashboard → New → PostgreSQL
   - Name: `portfolio-db`
   - Plan: Free
   - Region: Oregon (match web service)

2. Create Web Service:
   - Dashboard → New → Web Service
   - Connect GitHub repository
   - Runtime: Docker
   - Branch: `develop` (staging) or `main` (production)
   - Health Check Path: `/actuator/health`

3. Link database:
   - In Web Service settings → Environment
   - Add `DATABASE_URL` from database
   - Or use Render's auto-linking feature

## How DATABASE_URL Conversion Works

### Input (Render format):
```
postgres://user:p@ssw0rd@dpg-abc123.oregon-postgres.render.com:5432/mydb?sslmode=require
```

### Output (Spring Boot format):
```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://dpg-abc123.oregon-postgres.render.com:5432/mydb?sslmode=require
SPRING_DATASOURCE_USERNAME=user
SPRING_DATASOURCE_PASSWORD=p@ssw0rd
```

The `render-entrypoint.sh` script handles this conversion automatically, including:
- URL-decoding special characters (e.g., `%40` → `@`)
- Default SSL mode injection
- Password escaping
- Port defaulting to 5432 if not specified

## Deployment Steps

### Initial Deploy

1. **Push code to GitHub**:
   ```bash
   git add .
   git commit -m "feat: Add Render deployment configuration"
   git push origin develop
   ```

2. **Configure Environment Variables** (see Required Variables above)

3. **Trigger Deploy**:
   - Automatic: Push to `develop` or `main` branch
   - Manual: Dashboard → Service → "Manual Deploy"

4. **Monitor Logs**:
   ```
   Render Dashboard → Service → Logs
   ```

   Look for:
   ```
   === Starting Spring Boot application on Render ===
   DATABASE_URL detected, converting to Spring/JDBC...
   Database configured:
     Host: dpg-abc123.oregon-postgres.render.com
     Port: 5432
     Database: mydb
     User: portfolio_user
   Starting with JAVA_OPTS: ...
   ```

### Verify Deployment

1. **Health Check**:
   ```bash
   curl https://your-service.onrender.com/actuator/health
   ```

   Expected response:
   ```json
   {
     "status": "UP",
     "groups": ["liveness", "readiness"]
   }
   ```

2. **Check Database Connection**:
   Look in logs for:
   ```
   HikariPool-1 - Start completed.
   Flyway baseline migration applied
   Started PortfolioApplication in X.XXX seconds
   ```

## Common Issues

### Issue 1: Application Not Starting (Timeout)

**Symptoms**:
- Health check fails after 90+ seconds
- Logs show no Spring Boot startup messages

**Solution**:
1. Check all required environment variables are set
2. Verify `DATABASE_URL` is correctly linked
3. Check Flyway migrations in logs
4. Review `render-entrypoint.sh` output in logs

### Issue 2: Database Connection Failed

**Symptoms**:
- Logs show: `Failed to configure a DataSource`
- `Communications link failure`

**Solution**:
1. Verify `DATABASE_URL` is set
2. Check database is in same region as web service
3. Ensure SSL mode is set (automatically done by entrypoint)
4. Verify database is not suspended (free tier limitations)

### Issue 3: Flyway Migration Errors

**Symptoms**:
- `Flyway baseline migration failed`
- `Validate failed: Detected applied migration not resolved locally`

**Solution**:
1. Check migration files in `src/main/resources/db/migration`
2. Verify `spring.flyway.baseline-on-migrate=true` in config
3. Manually baseline if needed (see Flyway docs)

### Issue 4: 502 Bad Gateway

**Symptoms**:
- Health check returns 502
- Application starts but doesn't respond

**Solution**:
1. Verify `SERVER_PORT` uses Render's `$PORT` variable
2. Check application binds to `0.0.0.0` not `localhost`
3. Review JAVA_OPTS in logs
4. Increase health check timeout in Render dashboard

## Performance Optimization

### Free Tier Limitations

- **RAM**: 512 MB
- **CPU**: Shared
- **Cold Start**: Service sleeps after 15 min inactivity
- **Build Time**: 15 min max

### JVM Tuning (Already Applied)

The `render-entrypoint.sh` sets:
```bash
-XX:MaxRAMPercentage=75       # Use 75% of 512MB ≈ 384MB
-XX:+ExitOnOutOfMemoryError   # Fail fast on OOM
```

### Connection Pool (Already Applied)

`application-render.properties`:
```properties
spring.datasource.hikari.maximum-pool-size=5     # Reduced from default 10
spring.datasource.hikari.minimum-idle=2          # Reduced from default 5
spring.datasource.hikari.connection-timeout=20s
```

## Monitoring

### Health Endpoints

- **Liveness**: `https://your-service.onrender.com/actuator/health/liveness`
- **Readiness**: `https://your-service.onrender.com/actuator/health/readiness`
- **Full Health**: `https://your-service.onrender.com/actuator/health`
- **Info**: `https://your-service.onrender.com/actuator/info`

### Logs

Access via:
1. Render Dashboard → Service → Logs (web UI)
2. `render` CLI (if installed):
   ```bash
   render logs -s your-service-id --tail
   ```

## Updating Deployment

### Code Changes

```bash
git add .
git commit -m "your message"
git push origin develop
```

Render auto-deploys on push to tracked branch.

### Environment Variables

Changes to environment variables trigger automatic redeploy.

### Manual Redeploy

Dashboard → Service → "Manual Deploy" → "Clear build cache & deploy"

## Security Checklist

- [ ] Change default `PORTFOLIO_ADMIN_TOKEN`
- [ ] Set `PORTFOLIO_ADMIN_SECURITY_ENABLED=true`
- [ ] Use strong GitHub Personal Access Token (scope: `repo`)
- [ ] Rotate Anthropic API key if compromised
- [ ] Update `CORS_ALLOWED_ORIGINS` with actual frontend URL
- [ ] Enable `HTTPS` redirect in Render (auto-enabled)
- [ ] Review `ENABLE_FACTORY_RESET` (keep `false` unless needed)

## Resources

- [Render Docs](https://render.com/docs)
- [Spring Boot on Render](https://render.com/docs/deploy-spring-boot)
- [PostgreSQL on Render](https://render.com/docs/databases)
- [Dockerfile Best Practices](https://docs.docker.com/develop/develop-images/dockerfile_best-practices/)

## Support

If you encounter issues:

1. Check logs first: Dashboard → Logs
2. Review this guide's "Common Issues" section
3. Verify all environment variables
4. Test locally with Docker:
   ```bash
   docker build -t portfolio-backend .
   docker run -p 8080:8080 \
     -e DATABASE_URL="postgres://localhost:5432/portfolio_db" \
     -e GITHUB_TOKEN="your-token" \
     portfolio-backend
   ```

---

**Created by**: Bernard Uriza Orozco  
**Last Updated**: 2025-01-26  
**Version**: 1.0.0
