#!/usr/bin/env bash
set -euo pipefail

echo "=== Starting Spring Boot application on Render ==="
PORT="${PORT:-8080}"
export SERVER_PORT="$PORT"
export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-render}"

if [[ -n "${DATABASE_URL:-}" ]]; then
  echo "DATABASE_URL detected, converting to Spring/JDBC..."

  # Soporta formatos tipo: postgres://user:pass@host:5432/db?sslmode=require
  proto="$(echo "$DATABASE_URL" | sed -E 's,^(.*)://.*,\1,')"
  rest="${DATABASE_URL#*://}"

  creds_host_db="${rest%%\?*}"     # user:pass@host:port/db
  params="${DATABASE_URL#*\?}"     # sslmode=require ...
  [[ "$params" == "$DATABASE_URL" ]] && params=""

  creds="${creds_host_db%@*}"      # user:pass
  hostdb="${creds_host_db#*@}"     # host:port/db

  user="${creds%%:*}"
  pass="${creds#*:}"

  hostport="${hostdb%%/*}"         # host:port
  db="${hostdb#*/}"                # db

  host="${hostport%%:*}"
  port="${hostport#*:}"
  [[ "$port" == "$hostport" ]] && port="5432"   # si no hay puerto, default 5432

  # URL-decode password (handle %40 -> @, etc)
  pass="$(printf '%b' "${pass//%/\\x}")"

  # Sanitiza parámetros
  if [[ -z "$params" ]]; then
    params="sslmode=require"
  elif ! echo "$params" | grep -qi 'sslmode='; then
    params="${params}&sslmode=require"
  fi

  JDBC_URL="jdbc:postgresql://${host}:${port}/${db}?${params}"
  export SPRING_DATASOURCE_URL="$JDBC_URL"
  export SPRING_DATASOURCE_USERNAME="$user"
  export SPRING_DATASOURCE_PASSWORD="$pass"

  echo "Database configured:"
  echo "  Host: ${host}"
  echo "  Port: ${port}"
  echo "  Database: ${db}"
  echo "  User: ${user}"
  echo "  Params: ${params}"
else
  echo "WARNING: DATABASE_URL not set; using local defaults (may fail on Render)."
fi

# JVM options optimized for Render free tier (512MB)
JAVA_OPTS="${JAVA_OPTS:-} -XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
JAVA_OPTS="${JAVA_OPTS} -Dserver.port=${PORT}"
JAVA_OPTS="${JAVA_OPTS} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE}"
JAVA_OPTS="${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom"

# Health check endpoints
JAVA_OPTS="${JAVA_OPTS} -Dmanagement.endpoints.web.exposure.include=health,info"
JAVA_OPTS="${JAVA_OPTS} -Dmanagement.endpoint.health.probes.enabled=true"

echo "Starting with JAVA_OPTS: ${JAVA_OPTS}"

exec java ${JAVA_OPTS} -jar /app/app.jar