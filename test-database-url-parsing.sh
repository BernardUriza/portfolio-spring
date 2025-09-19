#!/usr/bin/env bash
set -euo pipefail

echo "=== Testing DATABASE_URL parsing ==="

# Test cases
test_urls=(
  "postgres://user:pass@dpg-abc123:5432/portfolio_db?sslmode=require"
  "postgres://user:p%40ss%3A12@dpg-abc123:5432/portfolio_db?sslmode=require"
  "postgres://user:pass@dpg-abc123/portfolio_db"
  "postgres://user:pass@localhost:5432/testdb?sslmode=disable"
)

for url in "${test_urls[@]}"; do
  echo ""
  echo "Testing: $url"
  echo "---"

  DATABASE_URL="$url"

  # Parse logic from render-entrypoint.sh
  proto="$(echo "$DATABASE_URL" | sed -E 's,^(.*)://.*,\1,')"
  rest="${DATABASE_URL#*://}"

  creds_host_db="${rest%%\?*}"
  params="${DATABASE_URL#*\?}"
  [[ "$params" == "$DATABASE_URL" ]] && params=""

  creds="${creds_host_db%@*}"
  hostdb="${creds_host_db#*@}"

  user="${creds%%:*}"
  pass="${creds#*:}"

  hostport="${hostdb%%/*}"
  db="${hostdb#*/}"

  host="${hostport%%:*}"
  port="${hostport#*:}"
  [[ "$port" == "$hostport" ]] && port="5432"

  # URL-decode password
  pass="$(printf '%b' "${pass//%/\\x}")"

  # Sanitiza parámetros
  if [[ -z "$params" ]]; then
    params="sslmode=require"
  elif ! echo "$params" | grep -qi 'sslmode='; then
    params="${params}&sslmode=require"
  fi

  JDBC_URL="jdbc:postgresql://${host}:${port}/${db}?${params}"

  echo "  Host: ${host}"
  echo "  Port: ${port}"
  echo "  Database: ${db}"
  echo "  User: ${user}"
  echo "  Password: ${pass}"
  echo "  JDBC URL: ${JDBC_URL}"

  # Validate port is numeric
  if [[ "$port" =~ ^[0-9]+$ ]]; then
    echo "  ✓ Port is valid numeric value"
  else
    echo "  ✗ ERROR: Port is not numeric: $port"
    exit 1
  fi
done

echo ""
echo "All tests passed!"