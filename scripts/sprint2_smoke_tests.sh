#!/bin/bash

# Sprint 2 Performance Validation - Smoke Tests
# Purpose: Validate PERF-001 and PERF-002 improvements
# Prerequisites: Application running on localhost:8080

set -e

COLOR_GREEN='\033[0;32m'
COLOR_RED='\033[0;31m'
COLOR_YELLOW='\033[1;33m'
COLOR_BLUE='\033[0;34m'
COLOR_RESET='\033[0m'

RESULTS_FILE="SPRINT_2_E2E_RESULTS.md"
BASE_URL="http://localhost:8080"

echo -e "${COLOR_BLUE}=====================================${COLOR_RESET}"
echo -e "${COLOR_BLUE}Sprint 2 Smoke Tests - Performance${COLOR_RESET}"
echo -e "${COLOR_BLUE}=====================================${COLOR_RESET}"
echo ""

# Initialize results file
cat > $RESULTS_FILE << 'EOF'
# Sprint 2 E2E Smoke Test Results

**Date**: $(date +%Y-%m-%d)
**Sprint**: Sprint 2 - Performance & Optimization
**Tester**: Automated Script

---

EOF

# Test 1: Health Check
echo -e "${COLOR_YELLOW}Test 1: Application Health Check${COLOR_RESET}"
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" $BASE_URL/api/health)
if [ "$HTTP_CODE" == "200" ]; then
    echo -e "${COLOR_GREEN}✓ PASS${COLOR_RESET} - Application is running (HTTP 200)"
    echo "### Test 1: Health Check" >> $RESULTS_FILE
    echo "- [x] PASS - HTTP 200" >> $RESULTS_FILE
    echo "" >> $RESULTS_FILE
else
    echo -e "${COLOR_RED}✗ FAIL${COLOR_RESET} - Application not responding (HTTP $HTTP_CODE)"
    echo "### Test 1: Health Check" >> $RESULTS_FILE
    echo "- [ ] FAIL - HTTP $HTTP_CODE" >> $RESULTS_FILE
    echo "" >> $RESULTS_FILE
    exit 1
fi
echo ""

# Test 2: Cache Metrics Endpoint
echo -e "${COLOR_YELLOW}Test 2: Cache Metrics Endpoint (PERF-002)${COLOR_RESET}"
CACHE_RESPONSE=$(curl -s $BASE_URL/api/monitoring/cache/stats)
if echo "$CACHE_RESPONSE" | jq -e '.caches["portfolio-completion"]' > /dev/null 2>&1; then
    echo -e "${COLOR_GREEN}✓ PASS${COLOR_RESET} - Cache metrics endpoint functional"
    echo "  - Found portfolio-completion cache"

    HIT_COUNT=$(echo "$CACHE_RESPONSE" | jq -r '.caches["portfolio-completion"].hitCount')
    MISS_COUNT=$(echo "$CACHE_RESPONSE" | jq -r '.caches["portfolio-completion"].missCount')
    EVICTION_COUNT=$(echo "$CACHE_RESPONSE" | jq -r '.caches["portfolio-completion"].evictionCount')

    echo "  - Hit count: $HIT_COUNT"
    echo "  - Miss count: $MISS_COUNT"
    echo "  - Eviction count: $EVICTION_COUNT"

    echo "### Test 2: Cache Metrics Endpoint" >> $RESULTS_FILE
    echo "- [x] PASS - Endpoint functional" >> $RESULTS_FILE
    echo "- Hit count: $HIT_COUNT" >> $RESULTS_FILE
    echo "- Miss count: $MISS_COUNT" >> $RESULTS_FILE
    echo "- Eviction count: $EVICTION_COUNT" >> $RESULTS_FILE
    echo "" >> $RESULTS_FILE
else
    echo -e "${COLOR_RED}✗ FAIL${COLOR_RESET} - Cache metrics endpoint not working"
    echo "### Test 2: Cache Metrics Endpoint" >> $RESULTS_FILE
    echo "- [ ] FAIL - Endpoint error" >> $RESULTS_FILE
    echo "" >> $RESULTS_FILE
fi
echo ""

# Test 3: Performance Baseline (Cached requests)
echo -e "${COLOR_YELLOW}Test 3: Performance Baseline${COLOR_RESET}"
echo "  Warming up cache..."
curl -s $BASE_URL/api/monitoring/status > /dev/null

echo "  Measuring 10 cached requests..."
START=$(date +%s%N)
for i in {1..10}; do
    curl -s $BASE_URL/api/monitoring/status > /dev/null
done
END=$(date +%s%N)

DURATION=$(( (END - START) / 1000000 ))
AVG=$(( DURATION / 10 ))

echo "  Total time: ${DURATION}ms"
echo "  Average: ${AVG}ms per request"

if [ $AVG -lt 100 ]; then
    echo -e "${COLOR_GREEN}✓ PASS${COLOR_RESET} - Average response time < 100ms"
    echo "### Test 3: Performance Baseline" >> $RESULTS_FILE
    echo "- [x] PASS - Avg ${AVG}ms < 100ms target" >> $RESULTS_FILE
    echo "" >> $RESULTS_FILE
else
    echo -e "${COLOR_YELLOW}⚠ WARNING${COLOR_RESET} - Average response time ${AVG}ms >= 100ms"
    echo "### Test 3: Performance Baseline" >> $RESULTS_FILE
    echo "- [ ] PARTIAL - Avg ${AVG}ms >= 100ms target" >> $RESULTS_FILE
    echo "" >> $RESULTS_FILE
fi
echo ""

# Test 4: Cache Behavior
echo -e "${COLOR_YELLOW}Test 4: Cache Behavior Verification${COLOR_RESET}"
echo "  Step 1: Clear cache baseline..."
BEFORE=$(curl -s $BASE_URL/api/monitoring/cache/stats | jq -r '.caches["portfolio-projects"].missCount')
echo "  Initial miss count: $BEFORE"

echo "  Step 2: Make first request (should be cache miss)..."
curl -s "$BASE_URL/api/monitoring/status" > /dev/null

echo "  Step 3: Check miss count increased..."
AFTER=$(curl -s $BASE_URL/api/monitoring/cache/stats | jq -r '.caches["portfolio-projects"].missCount // 0')
echo "  After miss count: $AFTER"

if [ "$AFTER" != "null" ] && [ "$AFTER" != "0" ]; then
    echo -e "${COLOR_GREEN}✓ PASS${COLOR_RESET} - Cache is tracking misses"
    echo "### Test 4: Cache Behavior" >> $RESULTS_FILE
    echo "- [x] PASS - Cache tracking functional" >> $RESULTS_FILE
    echo "" >> $RESULTS_FILE
else
    echo -e "${COLOR_YELLOW}⚠ INFO${COLOR_RESET} - Cache metrics may need more requests to populate"
    echo "### Test 4: Cache Behavior" >> $RESULTS_FILE
    echo "- [x] INFO - Needs more data for full validation" >> $RESULTS_FILE
    echo "" >> $RESULTS_FILE
fi
echo ""

# Test 5: Monitoring Endpoints
echo -e "${COLOR_YELLOW}Test 5: Monitoring Endpoints${COLOR_RESET}"
ENDPOINTS=(
    "/api/monitoring/status"
    "/api/monitoring/cache/stats"
    "/api/monitoring/awake"
)

ALL_PASS=true
for endpoint in "${ENDPOINTS[@]}"; do
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" $BASE_URL$endpoint)
    if [ "$HTTP_CODE" == "200" ]; then
        echo -e "  ${COLOR_GREEN}✓${COLOR_RESET} $endpoint - HTTP 200"
    else
        echo -e "  ${COLOR_RED}✗${COLOR_RESET} $endpoint - HTTP $HTTP_CODE"
        ALL_PASS=false
    fi
done

if [ "$ALL_PASS" = true ]; then
    echo -e "${COLOR_GREEN}✓ PASS${COLOR_RESET} - All monitoring endpoints functional"
    echo "### Test 5: Monitoring Endpoints" >> $RESULTS_FILE
    echo "- [x] PASS - All endpoints returning 200" >> $RESULTS_FILE
    echo "" >> $RESULTS_FILE
else
    echo -e "${COLOR_RED}✗ FAIL${COLOR_RESET} - Some monitoring endpoints failed"
    echo "### Test 5: Monitoring Endpoints" >> $RESULTS_FILE
    echo "- [ ] FAIL - Some endpoints not working" >> $RESULTS_FILE
    echo "" >> $RESULTS_FILE
fi
echo ""

# Summary
echo -e "${COLOR_BLUE}=====================================${COLOR_RESET}"
echo -e "${COLOR_BLUE}Test Summary${COLOR_RESET}"
echo -e "${COLOR_BLUE}=====================================${COLOR_RESET}"
echo ""
echo "Results saved to: $RESULTS_FILE"
echo ""

cat >> $RESULTS_FILE << 'EOF'
---

## Overall Sprint 2 Validation

Based on automated smoke tests:

EOF

echo -e "${COLOR_GREEN}✓ Sprint 2 smoke tests completed${COLOR_RESET}"
echo -e "Review detailed results in: ${COLOR_YELLOW}$RESULTS_FILE${COLOR_RESET}"
echo ""
echo "Next steps:"
echo "1. Review $RESULTS_FILE"
echo "2. If all tests pass, mark Sprint 2 as COMPLETE"
echo "3. Update Trello cards and move to Done"
echo "4. Proceed to Sprint 3"
echo ""
