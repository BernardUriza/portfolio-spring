# Render Environment Variables Setup

## Quick Setup - Copy & Paste to Render Dashboard

Navigate to your Render service dashboard → Environment tab and add these variables:

### Required Variables

```bash
# Profile activation
SPRING_PROFILES_ACTIVE=render

# Database (Option A: Using DATABASE_URL from Render Postgres)
# Render provides DATABASE_URL automatically if using Render Postgres
# Add this to convert it to JDBC format:
JDBC_DATABASE_URL=${DATABASE_URL}

# Database (Option B: Manual configuration)
# Use this if you have an external database:
# SPRING_DATASOURCE_URL=jdbc:postgresql://your-db-host:5432/your-db-name?sslmode=require
# SPRING_DATASOURCE_USERNAME=your-username
# SPRING_DATASOURCE_PASSWORD=your-password

# Admin Security (Choose one approach)
# Option 1: Disable admin security temporarily (for testing)
APP_ADMIN_SECURITY_DISABLED=true

# Option 2: Set an admin token (recommended for production)
# ADMIN_TOKEN=your-secure-admin-token-here
# APP_ADMIN_SECURITY_DISABLED=false
```

### Optional Variables (Production Recommended)

```bash
# CORS Configuration (update with your frontend domains)
CORS_ALLOWED_ORIGINS=https://your-frontend.onrender.com,https://yourdomain.com

# Database Pool Settings (optimized for Render free tier)
DB_POOL_SIZE=3
DB_MIN_IDLE=1
DB_CONNECTION_TIMEOUT=30000

# Logging
JPA_SHOW_SQL=false

# GitHub Integration (if using sync features)
GITHUB_USERNAME=YourGitHubUsername
GITHUB_TOKEN=ghp_your_github_personal_access_token

# AI Features (if using)
ANTHROPIC_API_KEY=your-anthropic-api-key
ENABLE_AI_FEATURES=false

# Mail Configuration (if using contact form)
MAIL_HOST=smtp.your-provider.com
MAIL_PORT=587
MAIL_FROM=noreply@yourdomain.com
MAIL_TO=contact@yourdomain.com

# Feature Flags
ENABLE_FACTORY_RESET=false
FEATURE_AUTO_SYNC_ENABLED=true
FEATURE_SCHEDULED_SYNC_ENABLED=true
```

## Build Command

In your Render service settings:

**Build Command:**
```bash
./mvnw clean package -DskipTests
```

Or if mvnw is not executable:
```bash
chmod +x mvnw && ./mvnw clean package -DskipTests
```

**Start Command:**
```bash
java -jar -Dserver.port=$PORT target/*.jar
```

## Database URL Format Conversion

If Render provides `DATABASE_URL` in format:
```
postgres://username:password@hostname:5432/database?sslmode=require
```

The application needs it as:
```
jdbc:postgresql://hostname:5432/database?sslmode=require
```

The `application-render.properties` file handles this conversion automatically via `JDBC_DATABASE_URL`.

## Health Check Configuration

In Render dashboard:
- **Health Check Path:** `/actuator/health`
- **Port:** Leave empty (uses $PORT automatically)

## Troubleshooting

### Database Connection Issues
1. Ensure SSL mode is enabled: `?sslmode=require` in URL
2. Check that database credentials are correct
3. Verify database is accessible from Render (not localhost)

### Port Binding Issues
- The app automatically uses `$PORT` environment variable
- Don't hardcode port 8080 in start command

### Admin Token Warning
- Set `ADMIN_TOKEN` with a secure value, OR
- Temporarily set `APP_ADMIN_SECURITY_DISABLED=true` for testing

### Memory Issues (Free Tier)
Add to start command if needed:
```bash
java -Xmx256m -jar -Dserver.port=$PORT target/*.jar
```

## Security Checklist

Before going to production:
- [ ] Set strong `ADMIN_TOKEN`
- [ ] Enable `APP_ADMIN_SECURITY_DISABLED=false`
- [ ] Update `CORS_ALLOWED_ORIGINS` with your domains only
- [ ] Disable `JPA_SHOW_SQL`
- [ ] Set `ENABLE_FACTORY_RESET=false`
- [ ] Configure proper mail settings
- [ ] Set GitHub token if using sync features

## Sample .env for Local Testing

Create a `.env` file in project root for local testing:

```bash
SPRING_PROFILES_ACTIVE=render
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/portfolio_db?sslmode=disable
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=ADMIN
PORT=8080
APP_ADMIN_SECURITY_DISABLED=true
```

Then run:
```bash
./mvnw spring-boot:run
```