# 🚀 Production Deployment Guide

**Created by Bernard Orozco**
**Last Updated**: 2025-10-25

---

## 📋 Pre-Deployment Checklist

### Required Environment Variables

```bash
# Database Configuration
DATABASE_URL=jdbc:postgresql://your-db-host:5432/portfolio_db
DATABASE_USERNAME=your_db_user
DATABASE_PASSWORD=your_secure_password

# Security
PORTFOLIO_ADMIN_TOKEN=your-secure-admin-token-min-32-chars
ADMIN_RESET_TOKEN=your-factory-reset-token-min-32-chars

# API Keys
GITHUB_TOKEN=ghp_your_github_personal_access_token
ANTHROPIC_API_KEY=sk-ant-your_anthropic_api_key

# CORS Configuration
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://www.yourdomain.com

# Optional Features
ENABLE_AI_FEATURES=true
ENABLE_FACTORY_RESET=false
ENABLE_LIVE_NARRATION=false
```

---

## 🏗️ Building for Production

### 1. Build JAR file

```bash
./mvnw clean package -DskipTests
```

### 2. Verify JAR

```bash
java -jar target/portfolio-spring-0.0.1-SNAPSHOT.jar --version
```

---

## 🐳 Docker Deployment (Recommended)

### Build Docker Image

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/portfolio-spring-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
```

### Run Container

```bash
docker build -t portfolio-backend .

docker run -d \
  --name portfolio-api \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DATABASE_URL=$DATABASE_URL \
  -e DATABASE_USERNAME=$DATABASE_USERNAME \
  -e DATABASE_PASSWORD=$DATABASE_PASSWORD \
  -e PORTFOLIO_ADMIN_TOKEN=$PORTFOLIO_ADMIN_TOKEN \
  -e GITHUB_TOKEN=$GITHUB_TOKEN \
  -e ANTHROPIC_API_KEY=$ANTHROPIC_API_KEY \
  -e CORS_ALLOWED_ORIGINS=https://yourdomain.com \
  portfolio-backend
```

---

## ☁️ Cloud Platform Deployment

### AWS Elastic Beanstalk

1. Package application:
```bash
./mvnw clean package
```

2. Deploy:
```bash
eb init -p corretto-21 portfolio-backend
eb create production-env
eb setenv SPRING_PROFILES_ACTIVE=prod DATABASE_URL=... [other vars]
eb deploy
```

### Heroku

1. Add Procfile:
```
web: java -Dserver.port=$PORT -Dspring.profiles.active=prod -jar target/portfolio-spring-0.0.1-SNAPSHOT.jar
```

2. Deploy:
```bash
heroku create portfolio-backend
heroku addons:create heroku-postgresql:mini
heroku config:set SPRING_PROFILES_ACTIVE=prod
heroku config:set PORTFOLIO_ADMIN_TOKEN=...
git push heroku main
```

### Render

1. Create `render.yaml`:
```yaml
services:
  - type: web
    name: portfolio-backend
    env: java
    buildCommand: "./mvnw clean package -DskipTests"
    startCommand: "java -Dserver.port=$PORT -Dspring.profiles.active=prod -jar target/portfolio-spring-0.0.1-SNAPSHOT.jar"
    envVars:
      - key: SPRING_PROFILES_ACTIVE
        value: prod
      - key: DATABASE_URL
        fromDatabase:
          name: portfolio-db
          property: connectionString
```

---

## 🔒 Security Hardening

### 1. HTTPS Configuration

**Option A: Let's Encrypt with Nginx**

```nginx
server {
    listen 443 ssl http2;
    server_name api.yourdomain.com;

    ssl_certificate /etc/letsencrypt/live/api.yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.yourdomain.com/privkey.pem;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

**Option B: Cloud Provider SSL**
- AWS: Use Application Load Balancer with ACM certificate
- Heroku: Automatic SSL with paid dynos
- Render: Automatic SSL included

### 2. Rate Limiting

Production configuration already includes:
- Admin endpoints: 30 req/min
- Factory reset: 1 req/hour
- Sync operations: 5 req/min
- AI curation: 20 req/min

### 3. Database Security

```sql
-- Create read-only user for monitoring
CREATE USER portfolio_readonly WITH PASSWORD 'secure_password';
GRANT CONNECT ON DATABASE portfolio_db TO portfolio_readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO portfolio_readonly;

-- Enable SSL connections
ALTER DATABASE portfolio_db SET ssl TO on;
```

---

## 📊 Monitoring & Health Checks

### Health Endpoints

| Endpoint | Purpose | Expected Response |
|----------|---------|-------------------|
| `/api/health` | Liveness probe | 200 OK with `status: UP` |
| `/api/ready` | Readiness probe | 200 OK with database check |
| `/actuator/health` | Detailed health | 200 OK (requires auth) |

### Load Balancer Configuration

**Liveness Probe**:
- Path: `/api/health`
- Interval: 30s
- Timeout: 5s
- Unhealthy threshold: 3

**Readiness Probe**:
- Path: `/api/ready`
- Interval: 10s
- Timeout: 3s
- Unhealthy threshold: 2
- Success threshold: 1

### Monitoring Recommendations

1. **Application Performance Monitoring (APM)**:
   - New Relic
   - Datadog
   - Elastic APM

2. **Error Tracking**:
   - Sentry
   - Rollbar
   - Bugsnag

3. **Log Aggregation**:
   - ELK Stack (Elasticsearch, Logstash, Kibana)
   - Splunk
   - CloudWatch (AWS)

---

## 💾 Database Backups

### Automated Backups

**PostgreSQL on AWS RDS**:
- Automatic daily backups
- Retention period: 30 days
- Point-in-time recovery enabled

**Heroku Postgres**:
```bash
heroku pg:backups:schedule --at '02:00 America/New_York' DATABASE_URL
heroku pg:backups:retention DATABASE_URL --num 30
```

**Manual Backup**:
```bash
pg_dump -h localhost -U postgres portfolio_db > backup_$(date +%Y%m%d).sql
```

### Restore from Backup

```bash
psql -h localhost -U postgres -d portfolio_db < backup_20251025.sql
```

---

## 🔄 Rolling Updates

### Zero-Downtime Deployment

1. **Build new version**:
```bash
./mvnw clean package
```

2. **Start new instance**:
```bash
java -jar -Dspring.profiles.active=prod -Dserver.port=8081 target/portfolio-spring-0.0.1-SNAPSHOT.jar
```

3. **Health check new instance**:
```bash
curl http://localhost:8081/api/ready
```

4. **Update load balancer** to route to new instance

5. **Graceful shutdown old instance**:
```bash
kill -SIGTERM <old-pid>
```

---

## 🚨 Troubleshooting

### Application Won't Start

**Check logs**:
```bash
tail -f logs/spring.log
```

**Common issues**:
- Missing environment variables
- Database unreachable
- Port already in use

### Database Connection Errors

**Test connection**:
```bash
psql -h your-db-host -U your-user -d portfolio_db
```

**Check connection pool**:
```bash
curl http://localhost:8080/actuator/health
```

### High Memory Usage

**Monitor JVM**:
```bash
jstat -gc <pid> 1000
```

**Adjust heap size**:
```bash
java -Xms512m -Xmx1024m -jar app.jar
```

### API Rate Limiting

**Check rate limit headers**:
```bash
curl -I https://api.github.com
```

**Verify Resilience4j circuit breaker**:
```bash
curl http://localhost:8080/actuator/circuitbreakers
```

---

## 📝 Post-Deployment Verification

### 1. Health Checks

```bash
# Liveness
curl https://api.yourdomain.com/api/health

# Readiness
curl https://api.yourdomain.com/api/ready
```

### 2. Admin Endpoints

```bash
# Test authentication
curl -H "X-Admin-Token: $PORTFOLIO_ADMIN_TOKEN" \
  https://api.yourdomain.com/api/admin/portfolio/completion
```

### 3. GitHub Sync

```bash
# Trigger manual sync
curl -X POST -H "X-Admin-Token: $PORTFOLIO_ADMIN_TOKEN" \
  https://api.yourdomain.com/api/admin/source-repositories/sync
```

### 4. Database Connectivity

```bash
# Check database health
curl https://api.yourdomain.com/api/ready | jq '.checks.database'
```

---

## 🔐 Security Best Practices

✅ **Implemented**:
- Environment-based secrets (no hardcoded tokens)
- Restrictive CORS in production
- Rate limiting on all endpoints
- Admin token authentication
- Database connection pooling
- Health check endpoints

⚠️ **Recommended**:
- Enable HTTPS redirect (nginx/load balancer level)
- Implement Web Application Firewall (WAF)
- Set up DDoS protection (Cloudflare, AWS Shield)
- Enable audit logging for admin actions
- Implement backup encryption
- Set up SSL certificate auto-renewal

---

## 📞 Support & Maintenance

### Runbook

**High CPU Usage**:
1. Check active connections: `/actuator/metrics/jdbc.connections.active`
2. Review slow queries in database
3. Scale horizontally if needed

**High Memory Usage**:
1. Check heap dump: `jmap -heap <pid>`
2. Analyze with VisualVM or similar
3. Adjust -Xmx if needed

**Database Connection Exhaustion**:
1. Check connection pool settings
2. Review long-running queries
3. Increase `spring.datasource.hikari.maximum-pool-size`

---

## 🎯 Performance Optimization

### JVM Tuning

```bash
java -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -Xms512m \
     -Xmx1024m \
     -jar app.jar
```

### Database Indexing

```sql
-- Add indexes for common queries
CREATE INDEX idx_portfolio_status ON portfolio_projects(status, updated_at);
CREATE INDEX idx_source_repo_sync ON source_repositories(sync_status, last_sync_attempt);
```

### Caching Strategy

Production uses Caffeine with:
- Max size: 1000 entries
- TTL: 30 minutes
- Write-through invalidation

---

## 📚 Additional Resources

- [Spring Boot Production Ready Features](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [PostgreSQL Performance Tuning](https://www.postgresql.org/docs/current/performance-tips.html)
- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Let's Encrypt](https://letsencrypt.org/)

---

**Next Steps**: Deploy to staging environment for testing before production rollout.
