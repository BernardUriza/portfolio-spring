#!/usr/bin/env bash
set -e

# ==============================================================================
# Test Neon PostgreSQL Connection
# ==============================================================================
# This script tests the connection to Neon PostgreSQL before deploying
# Usage: ./scripts/test-neon-connection.sh "postgres://user:pass@host:5432/db"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "=================================================="
echo "🔍 Testing Neon PostgreSQL Connection"
echo "=================================================="
echo ""

# Check if DATABASE_URL is provided
if [ -z "$1" ]; then
    echo -e "${RED}❌ Error: DATABASE_URL not provided${NC}"
    echo ""
    echo "Usage:"
    echo "  ./scripts/test-neon-connection.sh \"postgres://user:pass@host:5432/db\""
    echo ""
    echo "Or export it first:"
    echo "  export DATABASE_URL=\"postgres://user:pass@host:5432/db\""
    echo "  ./scripts/test-neon-connection.sh \"\$DATABASE_URL\""
    exit 1
fi

DATABASE_URL="$1"

# Extract components from DATABASE_URL
echo -e "${YELLOW}📋 Parsing connection string...${NC}"
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

echo "  Protocol: $proto"
echo "  Host: $host"
echo "  Port: $port"
echo "  Database: $db"
echo "  User: $user"
echo "  Password: ${pass:0:4}****"
echo "  SSL Params: $params"
echo ""

# Test 1: DNS Resolution
echo -e "${YELLOW}🌐 Test 1: DNS Resolution${NC}"
if host "$host" > /dev/null 2>&1; then
    echo -e "${GREEN}✅ DNS resolves successfully${NC}"
    IP=$(host "$host" | grep "has address" | awk '{print $4}' | head -1)
    echo "   IP Address: $IP"
else
    echo -e "${RED}❌ DNS resolution failed${NC}"
    echo "   Host '$host' cannot be resolved"
    exit 1
fi
echo ""

# Test 2: TCP Connection
echo -e "${YELLOW}🔌 Test 2: TCP Connection${NC}"
if command -v nc > /dev/null 2>&1; then
    if timeout 5 nc -zv "$host" "$port" 2>&1 | grep -q succeeded; then
        echo -e "${GREEN}✅ TCP connection successful${NC}"
    else
        echo -e "${RED}❌ TCP connection failed${NC}"
        echo "   Cannot connect to $host:$port"
        exit 1
    fi
elif command -v telnet > /dev/null 2>&1; then
    if timeout 5 bash -c "echo quit | telnet $host $port" 2>&1 | grep -q Connected; then
        echo -e "${GREEN}✅ TCP connection successful${NC}"
    else
        echo -e "${RED}❌ TCP connection failed${NC}"
        exit 1
    fi
else
    echo -e "${YELLOW}⚠️  Skipping (nc/telnet not available)${NC}"
fi
echo ""

# Test 3: PostgreSQL Connection (requires psql)
echo -e "${YELLOW}🐘 Test 3: PostgreSQL Authentication${NC}"
if command -v psql > /dev/null 2>&1; then
    export PGPASSWORD="$pass"
    if psql -h "$host" -p "$port" -U "$user" -d "$db" -c "SELECT version();" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ PostgreSQL authentication successful${NC}"
        VERSION=$(psql -h "$host" -p "$port" -U "$user" -d "$db" -t -c "SELECT version();" 2>/dev/null | xargs)
        echo "   Version: $VERSION"
    else
        echo -e "${RED}❌ PostgreSQL authentication failed${NC}"
        echo "   Check username/password"
        exit 1
    fi
    unset PGPASSWORD
else
    echo -e "${YELLOW}⚠️  Skipping (psql not installed)${NC}"
    echo "   Install with: brew install postgresql (macOS)"
fi
echo ""

# Test 4: SSL Configuration
echo -e "${YELLOW}🔒 Test 4: SSL Configuration${NC}"
if echo "$params" | grep -qi "sslmode=require"; then
    echo -e "${GREEN}✅ SSL mode is 'require' (recommended)${NC}"
elif echo "$params" | grep -qi "sslmode="; then
    MODE=$(echo "$params" | grep -oE "sslmode=[^&]+" | cut -d= -f2)
    echo -e "${YELLOW}⚠️  SSL mode is '$MODE' (consider using 'require')${NC}"
else
    echo -e "${RED}❌ No SSL mode specified${NC}"
    echo "   Add '?sslmode=require' to your connection string"
    exit 1
fi
echo ""

# Summary
echo "=================================================="
echo -e "${GREEN}✅ All tests passed!${NC}"
echo "=================================================="
echo ""
echo "Your Neon connection is ready for deployment."
echo ""
echo "Next steps:"
echo "  1. Add DATABASE_URL to Render environment variables"
echo "  2. Run: git push origin main"
echo "  3. Monitor: render services logs portfolio-backend --tail 100"
echo ""
