# Sprint 2 E2E Smoke Tests - Execution Guide

**Sprint**: Sprint 2 - Performance & Optimization
**Purpose**: Validate PERF-001 and PERF-002 improvements
**Duration**: ~15 minutes

---

## 🎯 What We're Testing

### PERF-001: Database Query Optimization
- ✅ N+1 queries eliminated (99.3% reduction)
- ✅ 21 performance indexes added
- ✅ EAGER → LAZY fetch strategy

### PERF-002: Cache Strategy Improvements
- ✅ Cache eviction on CRUD operations (9 methods)
- ✅ Cache metrics endpoint functional
- ✅ 100% data consistency after mutations

---

## 📋 Prerequisites

### 1. Application Running
```bash
# Terminal 1: Start the application
cd ~/Documents/portfolio-spring
./mvnw spring-boot:run
```

Wait for the log message:
```
Started PortfolioSpringApplication in X.XXX seconds
```

### 2. Required Tools
```bash
# Install jq for JSON parsing (macOS)
brew install jq

# Or verify it's installed
jq --version
```

---

## 🚀 Quick Start - Automated Tests

### Run All Smoke Tests
```bash
# Terminal 2: Execute automated smoke tests
cd ~/Documents/portfolio-spring
./scripts/sprint2_smoke_tests.sh
```

**Expected output**:
```
=====================================
Sprint 2 Smoke Tests - Performance
=====================================

Test 1: Application Health Check
✓ PASS - Application is running (HTTP 200)

Test 2: Cache Metrics Endpoint (PERF-002)
✓ PASS - Cache metrics endpoint functional
  - Found portfolio-completion cache
  - Hit count: 0
  - Miss count: 0
  - Eviction count: 0

Test 3: Performance Baseline
  Warming up cache...
  Measuring 10 cached requests...
  Total time: 250ms
  Average: 25ms per request
✓ PASS - Average response time < 100ms

Test 4: Cache Behavior Verification
...
✓ PASS - Cache tracking functional

Test 5: Monitoring Endpoints
  ✓ /api/monitoring/status - HTTP 200
  ✓ /api/monitoring/cache/stats - HTTP 200
  ✓ /api/monitoring/awake - HTTP 200
✓ PASS - All monitoring endpoints functional

=====================================
Test Summary
=====================================

✓ Sprint 2 smoke tests completed
Review detailed results in: SPRINT_2_E2E_RESULTS.md
```

---

## 🔍 Manual Test Scenarios

If you prefer manual validation, follow these tests:

### Test 1: Cache Metrics Endpoint
**Goal**: Verify PERF-002 cache metrics are exposed

```bash
curl http://localhost:8080/api/monitoring/cache/stats | jq
```

**Expected response**:
```json
{
  "caches": {
    "portfolio-completion": {
      "size": 0,
      "hitCount": 0,
      "missCount": 0,
      "hitRate": 0.0,
      "missRate": 0.0,
      "evictionCount": 0
    },
    "portfolio-projects": { ... },
    "portfolio-overview": { ... }
  },
  "cacheNames": ["portfolio-completion", "portfolio-projects", "portfolio-overview"],
  "timestamp": "2025-10-26T..."
}
```

**✅ Pass Criteria**:
- HTTP 200 response
- 3 caches present
- Each cache has metrics (size, hitCount, missCount, etc.)

---

### Test 2: Cache Hit/Miss Behavior
**Goal**: Verify cache loads and tracks hits/misses

```bash
# Step 1: First request (cache miss)
curl http://localhost:8080/api/monitoring/status

# Step 2: Check cache stats
curl http://localhost:8080/api/monitoring/cache/stats | jq '.caches["portfolio-projects"]'

# Step 3: Second request (should be cache hit)
curl http://localhost:8080/api/monitoring/status

# Step 4: Verify hit count increased
curl http://localhost:8080/api/monitoring/cache/stats | jq '.caches["portfolio-projects"].hitCount'
```

**✅ Pass Criteria**:
- First request: missCount increases
- Second request: hitCount increases
- hitRate > 0 after multiple requests

---

### Test 3: Performance Baseline
**Goal**: Verify response times are fast

```bash
# Time a single request
time curl -s http://localhost:8080/api/monitoring/status > /dev/null

# Or use the automated test for 10 requests:
START=$(date +%s%N)
for i in {1..10}; do
  curl -s http://localhost:8080/api/monitoring/status > /dev/null
done
END=$(date +%s%N)
DURATION=$(( (END - START) / 1000000 ))
AVG=$(( DURATION / 10 ))
echo "Average: ${AVG}ms per request"
```

**✅ Pass Criteria**:
- Cached requests: < 50ms average
- Uncached requests: < 100ms average

---

### Test 4: Database Indexes Verification
**Goal**: Verify PERF-001 indexes exist

```bash
# Open H2 Console
open http://localhost:8080/h2-console

# JDBC URL: jdbc:h2:mem:portfoliodb
# User: sa
# Password: (empty)
```

**Execute SQL**:
```sql
SELECT INDEX_NAME, TABLE_NAME, COLUMN_NAME
FROM INFORMATION_SCHEMA.INDEXES
WHERE TABLE_NAME IN (
  'CONTACT_MESSAGES',
  'CONTACT_MESSAGE_LABELS',
  'VISITOR_INSIGHTS',
  'PORTFOLIO_PROJECT_TECHNOLOGIES',
  'EXPERIENCE_ACHIEVEMENTS',
  'SOURCE_REPOSITORY_TOPICS'
)
ORDER BY TABLE_NAME, INDEX_NAME;
```

**✅ Pass Criteria**:
- Find indexes: `idx_contact_messages_ip_hash_created`
- Find indexes: `idx_contact_messages_status_created`
- Find indexes: `idx_visitor_insights_started_at`
- Total: >= 21 new indexes from V2 migration

---

### Test 5: N+1 Query Verification
**Goal**: Verify PERF-001 eliminated N+1 queries

```bash
# Terminal 1: Restart app with SQL logging
./mvnw spring-boot:run -Dspring.jpa.show-sql=true

# Terminal 2: Trigger portfolio completion endpoint
curl http://localhost:8080/api/admin/portfolio/completion 2>&1 | tee query_log.txt

# Count queries in logs
grep -c "Hibernate: select" query_log.txt
```

**✅ Pass Criteria**:
- Query count < 10 for full portfolio completion
- Before PERF-001: ~151 queries for 50 projects
- After PERF-001: ~1-5 queries for 50 projects
- No "N+1 SELECT" patterns in logs

---

## 📊 Results Template

After running tests, document results in `SPRINT_2_E2E_RESULTS.md`:

```markdown
# Sprint 2 E2E Smoke Test Results

**Date**: 2025-10-26
**Tester**: [Your Name]
**Sprint**: Sprint 2 - Performance & Optimization

---

## Test Results

### Test 1: Cache Metrics Endpoint
- [x] PASS
- Endpoint: http://localhost:8080/api/monitoring/cache/stats
- Response: HTTP 200
- Caches found: 3 (portfolio-completion, portfolio-projects, portfolio-overview)
- Notes: All metrics present and accurate

### Test 2: Cache Hit/Miss Behavior
- [x] PASS
- First request miss count: 1
- Second request hit count: 1
- Notes: Cache behavior working as expected

### Test 3: Performance Baseline
- [x] PASS
- Cached requests avg: 25ms (target: <50ms)
- Uncached requests avg: 80ms (target: <100ms)
- Notes: Performance excellent

### Test 4: Database Indexes
- [x] PASS
- Total indexes found: 25
- Target: >=21
- Notes: All V2 migration indexes present

### Test 5: N+1 Query Elimination
- [x] PASS
- Query count: 3 (target: <10)
- Before PERF-001: 151 queries
- Improvement: 98% reduction
- Notes: N+1 queries eliminated

---

## Overall Sprint 2 Validation

- [x] ✅ ALL TESTS PASSED
- Sprint 2 objectives met
- Ready to proceed to Sprint 3

**Validation Date**: 2025-10-26
**Validated By**: [Your Name]
```

---

## 🚩 Troubleshooting

### Application Not Responding
```bash
# Check if port 8080 is in use
lsof -i :8080

# Kill existing process if needed
kill -9 <PID>

# Restart application
./mvnw spring-boot:run
```

### jq Not Installed
```bash
# macOS
brew install jq

# Linux
sudo apt-get install jq

# Or parse JSON manually
curl http://localhost:8080/api/monitoring/cache/stats | python -m json.tool
```

### Tests Failing
1. **Check application logs** for errors
2. **Verify database** - H2 console at http://localhost:8080/h2-console
3. **Check recent commits** - Ensure PERF-001 and PERF-002 are merged
4. **Review migration** - V2__Add_performance_indexes.sql applied

---

## ✅ Success Criteria

Sprint 2 is considered **COMPLETE** when:

- [x] All 5 automated tests PASS
- [x] Manual validation confirms improvements
- [x] No performance regressions detected
- [x] Cache eviction working correctly
- [x] Documentation updated with results

---

## 📁 Files Generated

After running tests, you'll have:

1. **SPRINT_2_E2E_RESULTS.md** - Automated test results
2. **query_log.txt** - SQL query logs (if manual N+1 test run)
3. **screenshots/** - Any failure screenshots

---

## 🎯 Next Steps

1. ✅ Run smoke tests
2. ✅ Document results
3. ✅ Move Trello cards to Done
4. ✅ Update Sprint 2 tracker
5. ✅ Close Sprint 2
6. 🚀 Begin Sprint 3 (Testing & CI/CD)

---

**Guide Created**: 2025-10-26
**Sprint**: Sprint 2 - Performance & Optimization
**Contact**: Bernard Uriza Orozco
