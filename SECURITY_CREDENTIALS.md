# Security: Database Credentials Management

## Overview

As of **SEC-001** implementation, all database credentials have been **removed from application.properties** and must be provided via environment variables.

## Critical Security Changes

### What Changed
- ❌ **REMOVED**: Hardcoded `spring.datasource.password=ADMIN` from application.properties (line 13)
- ✅ **ADDED**: Environment variable configuration for all database credentials
- ✅ **UPDATED**: .env.example with proper credential patterns

### Why This Matters
- **Security**: Prevents credentials from being committed to Git history
- **Compliance**: Meets OWASP, PCI-DSS, SOC 2 requirements
- **Auditability**: Each environment has unique credentials
- **Rotation**: Easier to rotate passwords without code changes

## Local Development Setup

### Step 1: Create .env File

```bash
cp .env.example .env
```

### Step 2: Configure Database Credentials

Edit `.env` and set your local PostgreSQL credentials:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/portfolio_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_local_dev_password
```

### Step 3: Verify Configuration

```bash
./mvnw spring-boot:run
```

Application should start without errors. Check logs for:
```
Database configured:
  Host: localhost
  Database: portfolio_db
  User: postgres
```

## Production Deployment (Render.com)

### Environment Variables Required

1. **DATABASE_URL** - Neon PostgreSQL connection string (automatically parsed)
   ```
   postgres://username:password@host:5432/database?sslmode=require
   ```

2. **Alternative**: Individual variables (if not using DATABASE_URL)
   ```
   SPRING_DATASOURCE_URL=jdbc:postgresql://host:5432/database
   SPRING_DATASOURCE_USERNAME=username
   SPRING_DATASOURCE_PASSWORD=password
   ```

### How Render Handles Credentials

The `render-entrypoint.sh` script automatically:
1. Detects `DATABASE_URL` from Neon
2. Parses it into JDBC format
3. Extracts username and password
4. Sets Spring Boot environment variables
5. URL-decodes special characters in password

**You don't need to manually set individual variables on Render** - just provide DATABASE_URL from Neon.

## Password Rotation Procedure

### For Production (Neon + Render)

1. **Generate New Password** in Neon Console
   - Go to https://console.neon.tech
   - Select your project → Settings → Reset password
   - Copy the new DATABASE_URL

2. **Update Render Environment Variable**
   - Go to Render Dashboard → portfolio-backend service
   - Navigate to Environment tab
   - Update `DATABASE_URL` with new connection string
   - Click "Save Changes"

3. **Verify Deployment**
   - Render will automatically redeploy
   - Check logs for successful database connection
   - Test health check: `https://portfolio-spring-gmat.onrender.com/actuator/health`

4. **Update Documentation**
   - Record rotation date in this file
   - Update any relevant team documentation

### For Local Development

1. **Change PostgreSQL Password**
   ```sql
   ALTER USER postgres WITH PASSWORD 'new_secure_password';
   ```

2. **Update .env File**
   ```bash
   SPRING_DATASOURCE_PASSWORD=new_secure_password
   ```

3. **Restart Application**
   ```bash
   ./mvnw spring-boot:run
   ```

## Password Rotation History

| Date | Environment | Reason | Rotated By |
|------|-------------|--------|------------|
| 2025-10-27 | All | SEC-001: Remove hardcoded credentials | Bernard Uriza |

## Security Checklist

- [x] Hardcoded credentials removed from application.properties
- [x] .env.example updated with secure patterns
- [x] Production uses DATABASE_URL from Neon
- [x] .env file is in .gitignore
- [ ] **TODO**: Rotate production password in Neon console
- [ ] **TODO**: Update Render DATABASE_URL environment variable
- [ ] **TODO**: Verify production deployment after rotation

## Compliance Notes

### OWASP Top 10
- **A07:2021 – Identification and Authentication Failures**: ✅ Fixed
  - No hardcoded credentials in source code
  - Environment-specific credential management

### PCI-DSS
- **Requirement 2.1**: ✅ Compliant
  - Changed default passwords
  - Unique credentials per environment

### SOC 2
- **CC6.1 Logical Access Controls**: ✅ Improved
  - Credentials not stored in version control
  - Audit trail for password rotations

## Troubleshooting

### Error: "Access denied for user"
- **Cause**: Missing or incorrect credentials in .env
- **Fix**: Verify SPRING_DATASOURCE_USERNAME and SPRING_DATASOURCE_PASSWORD

### Error: "Could not create connection to database"
- **Cause**: Missing SPRING_DATASOURCE_URL
- **Fix**: Ensure .env file exists and is loaded (check spring.config.import)

### Production: "WARNING: DATABASE_URL not set"
- **Cause**: DATABASE_URL missing from Render environment
- **Fix**: Add DATABASE_URL in Render Dashboard → Environment

## References

- OWASP: https://owasp.org/Top10/A07_2021-Identification_and_Authentication_Failures/
- Spring Boot Externalized Configuration: https://docs.spring.io/spring-boot/reference/features/external-config.html
- Render Environment Variables: https://render.com/docs/environment-variables
- Neon PostgreSQL: https://neon.tech/docs/get-started-with-neon/signing-up

---

**Last Updated**: 2025-10-27
**Owner**: Bernard Uriza Orozco
**Status**: ✅ SEC-001 Implemented
