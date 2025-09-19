#!/usr/bin/env bash
set -euo pipefail

echo "=== Starting Spring Boot application on Render ==="
export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-render}"
export PORT="${PORT:-10000}"

if [[ -n "${DATABASE_URL:-}" ]]; then
  echo "DATABASE_URL detected, converting to Spring format..."

  # Normaliza prefijo
  DBURL="${DATABASE_URL/postgres:\/\//postgresql://}"

  # Extrae partes via regex robusto
  # shellcheck disable=SC2001
  ProtoHostPortDb="$(echo "$DBURL" | sed -E 's|^[a-z]+://([^@]+)@([^/]+)/([^?]+).*|\1 \2 \3|')"
  CREDS="$(echo "$ProtoHostPortDb" | awk '{print $1}')"
  HOSTPORT="$(echo "$ProtoHostPortDb" | awk '{print $2}')"
  DBNAME="$(echo "$ProtoHostPortDb" | awk '{print $3}')"

  USER="$(echo "$CREDS" | sed -E 's|:.*$||')"
  PASS_ENC="$(echo "$CREDS" | sed -E 's|^[^:]*:||')"

  HOST="$(echo "$HOSTPORT" | sed -E 's|:.*$||')"
  PORT_DB="$(echo "$HOSTPORT" | sed -E 's|^[^:]*:||')"
  if [[ -z "$PORT_DB" || "$PORT_DB" == "$HOST" ]]; then
    PORT_DB="5432"
  fi

  # Conserva query string (ej. sslmode)
  QRY="$(echo "$DBURL" | sed -n 's/^[^?]*\(\?.*\)$/\1/p')"

  # URL-decode de password (manejo de %xx)
  urldecode() { printf '%b' "${1//%/\\x}"; }
  PASS="$(urldecode "$PASS_ENC")"

  export SPRING_DATASOURCE_URL="jdbc:postgresql://$HOST:$PORT_DB/$DBNAME${QRY:-}"
  export SPRING_DATASOURCE_USERNAME="$USER"
  export SPRING_DATASOURCE_PASSWORD="$PASS"

  echo "Database configured:"
  echo "  Host: $HOST"
  echo "  Port: $PORT_DB"
  echo "  Database: $DBNAME"
  echo "  User: $USER"
else
  echo "WARNING: DATABASE_URL not set. Using local defaults may fail on Render."
fi

# Ajustes JVM/health
JAVA_OPTS="${JAVA_OPTS:-} -XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError -Dserver.port=${PORT} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -Dmanagement.endpoints.web.exposure.include=health,info -Dmanagement.endpoint.health.probes.enabled=true"
export JAVA_OPTS

exec java ${JAVA_OPTS} -jar /app/app.jar