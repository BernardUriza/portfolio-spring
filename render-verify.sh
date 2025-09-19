#!/usr/bin/env bash
set -euo pipefail
[[ -n "${SPRING_DATASOURCE_URL:-}" ]] || { echo "Missing SPRING_DATASOURCE_URL"; exit 1; }
echo "$SPRING_DATASOURCE_URL" | grep -E '^jdbc:postgresql://[^:/]+:[0-9]+/[^?]+' >/dev/null || { echo "Bad JDBC URL: $SPRING_DATASOURCE_URL"; exit 1; }
echo "JDBC OK"