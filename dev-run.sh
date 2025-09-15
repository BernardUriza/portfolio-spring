#!/usr/bin/env bash
set -euo pipefail

load_env() {
  local file=".env"
  [[ -f "$file" ]] || return 0
  echo "Loading env from $file"
  while IFS= read -r line || [[ -n "$line" ]]; do
    line="${line%%$'\r'}"
    [[ -z "$line" || "$line" =~ ^# ]] && continue
    if [[ "$line" =~ ^([^=]+)=(.*)$ ]]; then
      key="${BASH_REMATCH[1]}"; val="${BASH_REMATCH[2]}"
      # strip quotes
      if [[ "$val" =~ ^\".*\"$ || "$val" =~ ^\'.*\'$ ]]; then
        val="${val:1:${#val}-2}"
      fi
      export "$key"="${val}"
    fi
  done < "$file"
}

cd "$(dirname "$0")"
load_env
export PORTFOLIO_ADMIN_SECURITY_ENABLED="${PORTFOLIO_ADMIN_SECURITY_ENABLED:-true}"

token_len=${#PORTFOLIO_ADMIN_TOKEN:-0}
echo "Admin security: $PORTFOLIO_ADMIN_SECURITY_ENABLED, token length: $token_len"

exec ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

