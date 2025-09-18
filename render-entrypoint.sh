#!/usr/bin/env bash
set -euo pipefail

echo "=== Starting Spring Boot application on Render ==="
echo "PORT: ${PORT:-8080}"
echo "SPRING_PROFILES_ACTIVE: ${SPRING_PROFILES_ACTIVE:-render}"

# Render provides DATABASE_URL in format: postgres://user:pass@host:5432/dbname
if [[ -n "${DATABASE_URL:-}" ]]; then
  echo "DATABASE_URL detected, converting to Spring format..."

  proto="$(echo "$DATABASE_URL" | sed -E 's|:.*$||')" # "postgres" or "postgresql"

  if [[ "$proto" != "postgres" && "$proto" != "postgresql" ]]; then
    echo "WARNING: DATABASE_URL does not look like postgres: $DATABASE_URL"
  else
    # Extract user:pass
    creds="$(echo "$DATABASE_URL" | sed -E 's|^[a-z]+://([^@]+)@.*$|\1|')"
    DB_USER="${creds%%:*}"
    DB_PASS="${creds#*:}"

    # Extract host:port/db?params
    hostportdb="$(echo "$DATABASE_URL" | sed -E 's|^[a-z]+://[^@]+@([^?]+).*$|\1|')"

    # Split host:port from /db
    hostport="${hostportdb%/*}"
    dbname="${hostportdb#*/}"

    # Split host from :port
    DB_HOST="${hostport%:*}"
    DB_PORT="${hostport#*:}"

    # Handle query parameters (like ?sslmode=require)
    params=""
    if [[ "$DATABASE_URL" == *"?"* ]]; then
      params="?$(echo "$DATABASE_URL" | sed -E 's|^[^?]+\?||')"
    fi

    # Build JDBC URL with SSL mode
    export SPRING_DATASOURCE_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${dbname}${params}"
    export SPRING_DATASOURCE_USERNAME="$DB_USER"
    export SPRING_DATASOURCE_PASSWORD="$DB_PASS"

    echo "Database configured:"
    echo "  Host: ${DB_HOST}"
    echo "  Port: ${DB_PORT}"
    echo "  Database: ${dbname}"
    echo "  User: ${DB_USER}"
    echo "  SSL/Params: ${params:-none}"
  fi
else
  echo "WARNING: DATABASE_URL not set. Database connection may fail."
fi

# Hikari: don't fail fast if DB is not ready yet
export SPRING_DATASOURCE_HIKARI_INITIALIZATION_FAIL_TIMEOUT=0
export SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT=30000
export SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=4
export SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE=1

# Bind to PORT provided by Render
export SERVER_PORT="${PORT:-8080}"

# Set Spring profile
export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-render}"

# JVM options for Render free tier (512MB)
JAVA_OPTS=""
JAVA_OPTS="$JAVA_OPTS -XX:MaxRAMPercentage=75"
JAVA_OPTS="$JAVA_OPTS -XX:+ExitOnOutOfMemoryError"
JAVA_OPTS="$JAVA_OPTS -Dserver.port=$SERVER_PORT"
JAVA_OPTS="$JAVA_OPTS -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE"
JAVA_OPTS="$JAVA_OPTS -Djava.security.egd=file:/dev/./urandom"

# Health check endpoint
JAVA_OPTS="$JAVA_OPTS -Dmanagement.endpoints.web.exposure.include=health,info"
JAVA_OPTS="$JAVA_OPTS -Dmanagement.endpoint.health.probes.enabled=true"

echo "Starting application with:"
echo "  JAVA_OPTS: $JAVA_OPTS"
echo "  JAR: /app/app.jar"

# Start the application
exec java $JAVA_OPTS -jar /app/app.jar