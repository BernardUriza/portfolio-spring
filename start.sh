#!/bin/bash
# Render Deployment Startup Script
# Created by Bernard Orozco
# 
# Purpose: Convert Render's DATABASE_URL format to Spring Boot JDBC format
# Render provides: postgres://user:pass@host:port/db
# Spring needs:    jdbc:postgresql://user:pass@host:port/db

set -e  # Exit on error

echo "========================================="
echo "🚀 Portfolio Backend Startup Script"
echo "========================================="
echo ""

# Check if DATABASE_URL is provided by Render
if [ -z "$DATABASE_URL" ]; then
    echo "⚠️  WARNING: DATABASE_URL not found!"
    echo "Using default localhost database configuration"
else
    echo "✅ DATABASE_URL found from Render"
    
    # Convert postgres:// to jdbc:postgresql://
    export SPRING_DATASOURCE_URL=$(echo $DATABASE_URL | sed 's/^postgres:\/\//jdbc:postgresql:\/\//')
    
    echo "✅ Converted to JDBC format"
    echo "   Format: jdbc:postgresql://[host]:[port]/[database]"
    
    # Extract username, password, host, port, and database from URL
    # Format: postgres://user:pass@host:port/db
    DB_CREDS=$(echo $DATABASE_URL | sed -n 's/postgres:\/\/\([^:]*\):\([^@]*\)@\(.*\)/\1 \2 \3/p')
    
    if [ ! -z "$DB_CREDS" ]; then
        export DATABASE_USERNAME=$(echo $DB_CREDS | cut -d' ' -f1)
        export DATABASE_PASSWORD=$(echo $DB_CREDS | cut -d' ' -f2)
        
        echo "✅ Extracted database credentials"
        echo "   Username: ${DATABASE_USERNAME:0:3}***"
    fi
fi

# Set production profile
export SPRING_PROFILES_ACTIVE=prod
echo "✅ Spring Profile: $SPRING_PROFILES_ACTIVE"

# Set server port (Render provides PORT variable)
if [ ! -z "$PORT" ]; then
    echo "✅ Server Port: $PORT (from Render)"
else
    export PORT=8080
    echo "✅ Server Port: $PORT (default)"
fi

# Check required environment variables
echo ""
echo "📋 Environment Variables Check:"
echo "================================"

check_var() {
    local var_name=$1
    local var_value=${!var_name}
    
    if [ -z "$var_value" ]; then
        echo "❌ $var_name: NOT SET"
        return 1
    else
        # Mask sensitive values
        local display_value
        if [[ $var_name == *"TOKEN"* ]] || [[ $var_name == *"PASSWORD"* ]] || [[ $var_name == *"KEY"* ]]; then
            display_value="${var_value:0:6}***"
        else
            display_value="$var_value"
        fi
        echo "✅ $var_name: $display_value"
        return 0
    fi
}

# Track if any required variables are missing
MISSING_VARS=0

# Database configuration
check_var "SPRING_DATASOURCE_URL" || MISSING_VARS=$((MISSING_VARS + 1))
check_var "DATABASE_USERNAME" || MISSING_VARS=$((MISSING_VARS + 1))
check_var "DATABASE_PASSWORD" || MISSING_VARS=$((MISSING_VARS + 1))

# API Keys
check_var "GITHUB_USERNAME" || MISSING_VARS=$((MISSING_VARS + 1))
check_var "GITHUB_TOKEN" || MISSING_VARS=$((MISSING_VARS + 1))
check_var "ANTHROPIC_API_KEY" || MISSING_VARS=$((MISSING_VARS + 1))

# Security
check_var "PORTFOLIO_ADMIN_TOKEN" || MISSING_VARS=$((MISSING_VARS + 1))

echo ""

# Optional: CORS configuration
if [ -z "$CORS_ALLOWED_ORIGINS" ]; then
    echo "⚠️  CORS_ALLOWED_ORIGINS not set, using default"
    export CORS_ALLOWED_ORIGINS="https://bernarduriza.com"
fi

# Warn if any required variables are missing
if [ $MISSING_VARS -gt 0 ]; then
    echo ""
    echo "⚠️  WARNING: $MISSING_VARS required environment variable(s) missing!"
    echo "The application may fail to start or have limited functionality."
    echo ""
fi

# Display Java version
echo "☕ Java Version:"
java -version 2>&1 | head -3

echo ""
echo "========================================="
echo "🚀 Starting Spring Boot Application..."
echo "========================================="
echo ""

# Start the Spring Boot application
# Render expects the command to be in the format: java -jar app.jar
exec java -Dserver.port=$PORT \
          -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE \
          -jar target/*.jar
