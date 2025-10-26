# Deployment Guide - Portfolio Spring Backend

## 🎯 Overview

This guide covers deploying the Spring Boot backend to **Render.com** (free tier) with **Neon PostgreSQL** (free tier).

**Total Cost**: $0/month ✅

---

## 📋 Prerequisites

- [x] GitHub account
- [x] Render.com account (free)
- [x] Neon.tech account (free)
- [x] GitHub Personal Access Token
- [x] Anthropic API Key (for AI features)

---

## 🗄️ Database Setup (Neon PostgreSQL)

### Step 1: Create Neon Account

1. Go to https://console.neon.tech
2. Sign up with GitHub (1-click)
3. Create a new project named **"portfolio"**

### Step 2: Get Connection String

1. In Neon dashboard → **Connection Details**
2. Select **Pooled connection**
3. Copy the full connection string:
   ```
   postgres://user:pass@ep-xxx.us-east-2.aws.neon.tech/neondb?sslmode=require
   ```

**📖 Detailed Guide**: See [neon-setup.md](./neon-setup.md)

---

## 🚀 Render Deployment

### Option 1: Deploy via Dashboard (Easiest)

1. Go to https://dashboard.render.com
2. Click **New +** → **Web Service**
3. Connect your GitHub repo: `BernardUriza/portfolio-spring`
4. Configure:
   - **Name**: `portfolio-backend`
   - **Region**: `Oregon (US West)`
   - **Branch**: `main`
   - **Runtime**: `Docker`
   - **Plan**: `Free`

5. Add Environment Variables:
   ```
   DATABASE_URL=postgres://user:pass@ep-xxx.neon.tech/neondb?sslmode=require
   GITHUB_USERNAME=your-github-username
   GITHUB_TOKEN=ghp_your_github_token
   ANTHROPIC_API_KEY=sk-ant-your_anthropic_key
   PORTFOLIO_ADMIN_TOKEN=random-secure-token-123
   ```

6. Click **Create Web Service**

### Option 2: Deploy via CLI

```bash
# 1. Install Render CLI
brew install render

# 2. Login
render login

# 3. Set workspace
render workspace set

# 4. Deploy
render up
```

### Option 3: Deploy via Blueprint (render.yaml)

Already configured! Just push to `main` branch:

```bash
git add .
git commit -m "feat(deploy): Configure Neon PostgreSQL"
git push origin main
```

Render will auto-deploy on push to `main`.

---

## 🔑 Required Environment Variables

Set these in Render Dashboard → **portfolio-backend** → **Environment**:

| Variable | Description | Example |
|----------|-------------|---------|
| `DATABASE_URL` | Neon PostgreSQL connection string | `postgres://user:pass@ep-xxx.neon.tech/neondb?sslmode=require` |
| `GITHUB_USERNAME` | Your GitHub username | `BernardUriza` |
| `GITHUB_TOKEN` | GitHub Personal Access Token | `ghp_xxxxxxxxxxxxx` |
| `ANTHROPIC_API_KEY` | Claude API key (optional) | `sk-ant-xxxxxxxxxxxxx` |
| `PORTFOLIO_ADMIN_TOKEN` | Admin authentication token | `random-secure-token-123` |

### How to Get API Keys:

**GitHub Token:**
1. GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Generate new token with `repo` and `user` scopes
3. Copy and save (you won't see it again!)

**Anthropic API Key:**
1. Go to https://console.anthropic.com/
2. Account Settings → API Keys
3. Create key

---

## ✅ Verification

### 1. Check Deployment Status

```bash
render services list
```

### 2. View Logs

```bash
render services logs portfolio-backend --tail 100
```

### 3. Health Check

```bash
curl https://portfolio-backend.onrender.com/actuator/health
```

Expected response:
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" }
  }
}
```

### 4. Test Endpoints

```bash
# Get starred projects
curl https://portfolio-backend.onrender.com/api/projects/starred

# Sync repositories
curl -X POST https://portfolio-backend.onrender.com/api/projects/starred/sync
```

---

## 🧪 Testing Connection Before Deploy

Use the provided script to test your Neon connection:

```bash
./scripts/test-neon-connection.sh "postgres://user:pass@host:5432/db"
```

This will verify:
- ✅ DNS resolution
- ✅ TCP connectivity
- ✅ PostgreSQL authentication
- ✅ SSL configuration

---

## 🔧 Troubleshooting

### Issue: "UnknownHostException"

**Cause**: Invalid DATABASE_URL or DNS issue

**Solution**:
1. Verify DATABASE_URL in Render environment variables
2. Test connection with script above
3. Check Neon dashboard - DB might be suspended

### Issue: "HikariPool timeout"

**Cause**: Neon DB suspended (after 5 min inactivity)

**Solution**: First request after suspension takes 1-2 seconds (normal behavior)

### Issue: "Flyway migration failed"

**Cause**: Database user lacks permissions

**Solution**: Ensure Neon user has `CREATE TABLE` permissions (default for owner)

### Issue: "CORS error" in frontend

**Cause**: Frontend URL not in CORS allowed origins

**Solution**: Update `render.yaml`:
```yaml
- key: APP_CORS_ALLOWED_ORIGINS
  value: https://your-frontend.netlify.app,https://localhost:4200
```

---

## 📊 Monitoring

### Render Metrics
- Dashboard → portfolio-backend → Metrics
- CPU/Memory usage
- Request count
- Response times

### Neon Metrics
- Dashboard → Monitoring
- Active connections
- Query performance
- Storage usage

### Logs
```bash
# Real-time logs
render services logs portfolio-backend --tail 100 --follow

# Filter errors only
render services logs portfolio-backend | grep ERROR
```

---

## 🔄 CI/CD Workflow

Current setup uses **automatic deploys** on push to `main`:

```bash
# 1. Make changes locally
git add .
git commit -m "feat: Add new feature"

# 2. Push to GitHub
git push origin main

# 3. Render auto-deploys (takes ~5-10 min)

# 4. Monitor deployment
render services logs portfolio-backend --tail 100
```

To disable auto-deploy:
- Render Dashboard → portfolio-backend → Settings → Auto-Deploy → Off

---

## 💰 Cost Breakdown

| Service | Plan | Cost | Limits |
|---------|------|------|--------|
| Render Web Service | Free | $0 | 750 hrs/month, suspends after 15 min |
| Neon PostgreSQL | Free | $0 | 3 GB storage, auto-suspend after 5 min |
| **Total** | | **$0/month** | ✅ |

### Upgrade Options (if needed):

**Render Starter Plan** ($7/month):
- No auto-suspend
- Custom domains
- More resources

**Neon Launch Plan** ($19/month):
- 10 GB storage
- No auto-suspend
- Point-in-time recovery

---

## 🎯 Next Steps

After successful deployment:

- [ ] Test all API endpoints
- [ ] Configure frontend CORS origins
- [ ] Set up monitoring alerts
- [ ] Document API in Swagger/OpenAPI
- [ ] Configure custom domain (optional)
- [ ] Set up staging environment (Neon branch)

---

## 📚 Additional Resources

- [Neon Documentation](https://neon.tech/docs)
- [Render Documentation](https://render.com/docs)
- [Spring Boot Deployment Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [Flyway Migration Guide](https://flywaydb.org/documentation/)

---

## 🆘 Support

If you encounter issues:

1. Check logs: `render services logs portfolio-backend --tail 100`
2. Verify environment variables in Render dashboard
3. Test Neon connection with provided script
4. Check Render status page: https://status.render.com/
5. Check Neon status page: https://neon.tech/status

---

**Created by**: Bernard Uriza Orozco
**Last Updated**: 2025-01-25
**Version**: 1.0.0
