#!/usr/bin/env bash
set -euo pipefail

echo "=== Testing DATABASE_URL parsing ==="

# Use DATABASE_URL from environment or default test URL
: "${DATABASE_URL:=postgres://portfolio_user:p%40ss%3A12@dpg-abc123:5432/portfolio_db_vlfb?sslmode=require}"
echo "Testing with DATABASE_URL=$DATABASE_URL"

# Parse logic from render-entrypoint.sh
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

# Conserva query string
QRY="$(echo "$DBURL" | sed -n 's/^[^?]*\(\?.*\)$/\1/p')"

# URL-decode de password
urldecode() { printf '%b' "${1//%/\\x}"; }
PASS="$(urldecode "$PASS_ENC")"

export SPRING_DATASOURCE_URL="jdbc:postgresql://$HOST:$PORT_DB/$DBNAME${QRY:-}"
export SPRING_DATASOURCE_USERNAME="$USER"
export SPRING_DATASOURCE_PASSWORD="$PASS"

echo "Results:"
echo "  SPRING_DATASOURCE_URL=$SPRING_DATASOURCE_URL"
echo "  SPRING_DATASOURCE_USERNAME=$SPRING_DATASOURCE_USERNAME"
echo "  SPRING_DATASOURCE_PASSWORD=$SPRING_DATASOURCE_PASSWORD"

# Validate
./render-verify.sh && echo "✓ Validation passed" || echo "✗ Validation failed"