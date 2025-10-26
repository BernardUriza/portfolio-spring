#!/usr/bin/env bash
set -euo pipefail

# ==============================================================================
# Load Testing Script for Portfolio Spring Backend
# ==============================================================================
# This script performs load testing on critical endpoints using Apache Benchmark
#
# Usage:
#   ./scripts/load-test.sh [BASE_URL]
#
# Example:
#   ./scripts/load-test.sh http://localhost:8080
#   ./scripts/load-test.sh https://portfolio-spring-gmat.onrender.com
#
# Author: Bernard Uriza Orozco
# Date: 2025-10-26
# ==============================================================================

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BASE_URL="${1:-http://localhost:8080}"
RESULTS_DIR="./load-test-results"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
REPORT_FILE="${RESULTS_DIR}/load-test-report-${TIMESTAMP}.md"

# Test parameters
REQUESTS=1000        # Total requests per endpoint
CONCURRENCY=10       # Concurrent requests
TIMEOUT=30           # Timeout in seconds

# Admin token for protected endpoints (use dev token for local)
ADMIN_TOKEN="${PORTFOLIO_ADMIN_TOKEN:-dev-admin-token-12345-change-in-production}"

# ==============================================================================
# Functions
# ==============================================================================

print_header() {
    echo -e "${BLUE}============================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}============================================${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# Create results directory
mkdir -p "${RESULTS_DIR}"

# ==============================================================================
# Startup
# ==============================================================================

print_header "Portfolio Backend Load Testing"
echo ""
print_info "Base URL: ${BASE_URL}"
print_info "Requests per endpoint: ${REQUESTS}"
print_info "Concurrency: ${CONCURRENCY}"
print_info "Results directory: ${RESULTS_DIR}"
echo ""

# Check if backend is available
print_info "Checking backend health..."
if ! curl -sf "${BASE_URL}/actuator/health" > /dev/null 2>&1; then
    print_error "Backend is not available at ${BASE_URL}"
    print_warning "Please start the backend first:"
    echo "  ./mvnw spring-boot:run"
    exit 1
fi

print_success "Backend is healthy!"
echo ""

# ==============================================================================
# Initialize Report
# ==============================================================================

cat > "${REPORT_FILE}" <<EOF
# Load Testing Report - Performance Baseline

**Date**: $(date)
**Base URL**: ${BASE_URL}
**Requests per endpoint**: ${REQUESTS}
**Concurrency**: ${CONCURRENCY}
**Tool**: Apache Benchmark (ab)

---

## Test Results

EOF

# ==============================================================================
# Test Endpoints
# ==============================================================================

run_test() {
    local NAME="$1"
    local ENDPOINT="$2"
    local METHOD="${3:-GET}"
    local EXTRA_HEADERS="${4:-}"

    print_header "Testing: ${NAME}"
    echo "Endpoint: ${ENDPOINT}"
    echo "Method: ${METHOD}"
    echo ""

    local OUTPUT_FILE="${RESULTS_DIR}/test-${TIMESTAMP}-$(echo ${NAME} | tr ' ' '-' | tr '[:upper:]' '[:lower:]').txt"

    # Build ab command
    local AB_CMD="ab -n ${REQUESTS} -c ${CONCURRENCY} -t ${TIMEOUT} -g ${OUTPUT_FILE}.tsv"

    if [ -n "${EXTRA_HEADERS}" ]; then
        AB_CMD="${AB_CMD} ${EXTRA_HEADERS}"
    fi

    if [ "${METHOD}" = "POST" ]; then
        AB_CMD="${AB_CMD} -p /dev/null -T 'application/json'"
    fi

    AB_CMD="${AB_CMD} \"${BASE_URL}${ENDPOINT}\""

    # Run test
    print_info "Running test..."
    if eval "${AB_CMD}" > "${OUTPUT_FILE}" 2>&1; then
        print_success "Test completed!"

        # Extract key metrics
        local RPS=$(grep "Requests per second" "${OUTPUT_FILE}" | awk '{print $4}')
        local TIME_PER_REQ=$(grep "Time per request" "${OUTPUT_FILE}" | grep -v "across" | awk '{print $4}')
        local P50=$(grep "50%" "${OUTPUT_FILE}" | awk '{print $2}')
        local P95=$(grep "95%" "${OUTPUT_FILE}" | awk '{print $2}')
        local P99=$(grep "99%" "${OUTPUT_FILE}" | awk '{print $2}')
        local FAILED=$(grep "Failed requests" "${OUTPUT_FILE}" | awk '{print $3}')

        echo ""
        echo "Results:"
        echo "  Requests/sec: ${RPS}"
        echo "  Time/request: ${TIME_PER_REQ} ms (mean)"
        echo "  p50: ${P50} ms"
        echo "  p95: ${P95} ms"
        echo "  p99: ${P99} ms"
        echo "  Failed: ${FAILED}"
        echo ""

        # Append to report
        cat >> "${REPORT_FILE}" <<ENDMARKER
### ${NAME}

**Endpoint**: \`${METHOD} ${ENDPOINT}\`

| Metric | Value |
|--------|-------|
| Requests/sec | ${RPS} |
| Time/request (mean) | ${TIME_PER_REQ} ms |
| p50 | ${P50} ms |
| p95 | ${P95} ms |
| p99 | ${P99} ms |
| Failed requests | ${FAILED} |

ENDMARKER
    else
        print_error "Test failed!"
        cat "${OUTPUT_FILE}"
        echo ""

        # Append error to report
        cat >> "${REPORT_FILE}" <<ENDMARKER
### ${NAME}

**Endpoint**: \`${METHOD} ${ENDPOINT}\`

❌ **Test failed** - see logs for details

ENDMARKER
    fi
}

# ==============================================================================
# Run Tests on Critical Endpoints
# ==============================================================================

# Test 1: Health Check (baseline)
run_test "Health Check" "/actuator/health" "GET"

# Test 2: Get Starred Projects
run_test "Get Starred Projects" "/api/projects/starred" "GET"

# Test 3: Get Portfolio Completion (Admin endpoint)
run_test "Get Portfolio Completion" "/api/admin/portfolio/completion" "GET" "-H 'X-Admin-Token: ${ADMIN_TOKEN}'"

# Test 4: Get Source Repositories (Admin endpoint)
run_test "Get Source Repositories" "/api/admin/source-repositories" "GET" "-H 'X-Admin-Token: ${ADMIN_TOKEN}'"

# Test 5: Sync Config Status (Public)
run_test "Sync Config Status" "/api/admin/sync-config/status" "GET"

# ==============================================================================
# Finalize Report
# ==============================================================================

cat >> "${REPORT_FILE}" <<EOF

---

## Summary

All tests completed at $(date)

### Files Generated:
- Report: \`${REPORT_FILE}\`
- Raw results: \`${RESULTS_DIR}/test-${TIMESTAMP}-*.txt\`
- TSV data: \`${RESULTS_DIR}/test-${TIMESTAMP}-*.tsv\`

### Analysis Recommendations:

1. **Response Times**: Check if p95/p99 are acceptable for production
2. **Throughput**: Compare requests/sec with expected load
3. **Failed Requests**: Investigate any failed requests > 0
4. **Cache Hit Rates**: Check application logs for cache effectiveness

### Next Steps:

- Review logs: \`tail -f logs/application.log | grep PERF\`
- Check cache metrics: \`curl ${BASE_URL}/actuator/metrics/cache.gets\`
- Profile slow endpoints with JProfiler or VisualVM
- Consider implementing:
  - Connection pooling optimization
  - Database query optimization
  - CDN for static content
  - Redis for distributed caching

---

**Generated by**: Portfolio Load Testing Script
**Author**: Bernard Uriza Orozco
EOF

# ==============================================================================
# Final Output
# ==============================================================================

echo ""
print_header "Load Testing Complete!"
echo ""
print_success "Report generated: ${REPORT_FILE}"
echo ""
print_info "View report:"
echo "  cat ${REPORT_FILE}"
echo ""
print_info "Or open in browser (if markdown viewer available):"
echo "  open ${REPORT_FILE}"
echo ""
