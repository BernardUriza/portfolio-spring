# Render Database Setup - URGENT FIX

## The Problem
Your app is failing because DATABASE_URL is not set. The app is falling back to H2 in-memory database which causes the error:
```
Driver org.postgresql.Driver claims to not accept jdbcUrl, jdbc:h2:mem:...
```

## Quick Fix - Option 1: Manual Database Setup (Fastest)

1. Go to your Render Dashboard
2. Click "New +" → "PostgreSQL"
3. Configure:
   - Name: `portfolio-db`
   - Database: `portfolio_db`
   - User: `portfolio_user`
   - Region: **Oregon** (MUST match your web service)
   - Plan: Free

4. After creation, go to the database dashboard and copy the "Internal Database URL"

5. Go to your Web Service (portfolio-backend) → Environment
6. Add variable:
   ```
   DATABASE_URL = [paste the Internal Database URL here]
   ```

7. Also add these for safety:
   ```
   SPRING_PROFILES_ACTIVE = render
   APP_ADMIN_SECURITY_DISABLED = true
   ```

8. Save and the service will redeploy automatically

## Quick Fix - Option 2: Use render.yaml (Blueprint)

1. In Render Dashboard, delete your current web service
2. Click "New +" → "Blueprint"
3. Connect your GitHub repo
4. Select the `render.yaml` file
5. Render will create both the database and web service together

## Quick Fix - Option 3: External Database

If you have a database elsewhere (Supabase, Neon, etc.):

1. Get your database connection string
2. In Render Dashboard → Your Service → Environment
3. Add:
   ```
   DATABASE_URL = postgres://username:password@host:5432/database?sslmode=require
   ```

## Verify It's Working

After adding DATABASE_URL, your logs should show:
```
DATABASE_URL detected, converting to Spring format...
Database configured:
  Host: xxx.render.com
  Port: 5432
  Database: portfolio_db
  User: portfolio_user
```

Instead of:
```
WARNING: DATABASE_URL not set. Database connection may fail.
```

## Still Having Issues?

If DATABASE_URL is set but still failing:

1. Check the database is in the SAME region as your web service
2. Ensure the database is actually created and active
3. Try the external connection string (with proper SSL params)
4. Check if the database allows connections from your web service

## Emergency Fallback

As a last resort, you can use a free PostgreSQL from:
- https://www.elephantsql.com/ (Tiny Turtle - Free)
- https://supabase.com/ (Free tier)
- https://neon.tech/ (Free tier)

Then use their connection string as DATABASE_URL.