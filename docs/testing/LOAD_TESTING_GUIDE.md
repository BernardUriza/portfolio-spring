# Load Testing Guide - Portfolio Backend

## Overview

This guide explains how to perform load testing on the Portfolio Spring Boot backend to establish performance baselines and identify bottlenecks.

---

## Quick Start

```bash
# 1. Start backend locally
./mvnw spring-boot:run

# 2. Run load tests
./scripts/load-test.sh

# 3. View report
cat load-test-results/load-test-report-*.md
```

---

## Load Testing Tool

We use **Apache Benchmark (`ab`)** for load testing because:

- ✅ Pre-installed on macOS/Linux
- ✅ Simple command-line interface
- ✅ Generates detailed statistics
- ✅ Supports concurrent requests
- ✅ Export to TSV for graphing

---

## Critical Endpoints Tested

### 1. Health Check
- **Endpoint**: `GET /actuator/health`
- **Purpose**: Baseline test (no business logic)
- **Expected**: < 50ms p95

### 2. Get Starred Projects
- **Endpoint**: `GET /api/projects/starred`
- **Purpose**: Main public endpoint
- **Expected**: < 200ms p95 (with cache)

### 3. Get Portfolio Completion
- **Endpoint**: `GET /api/admin/portfolio/completion`
- **Auth**: Requires `X-Admin-Token` header
- **Purpose**: Complex aggregation with AI analysis
- **Expected**: < 500ms p95

### 4. Get Source Repositories
- **Endpoint**: `GET /api/admin/source-repositories`
- **Auth**: Requires `X-Admin-Token` header
- **Purpose**: List all GitHub repositories
- **Expected**: < 300ms p95

### 5. Sync Config Status
- **Endpoint**: `GET /api/admin/sync-config/status`
- **Purpose**: Public admin endpoint (no auth)
- **Expected**: < 100ms p95

---

## Test Parameters

| Parameter | Value | Rationale |
|-----------|-------|-----------|
| **Requests** | 1,000 | Sufficient sample size for statistics |
| **Concurrency** | 10 | Simulates moderate concurrent users |
| **Timeout** | 30s | Generous timeout for complex queries |

### Adjusting Parameters

Edit `scripts/load-test.sh`:

```bash
REQUESTS=1000        # Total requests per endpoint
CONCURRENCY=10       # Concurrent requests
TIMEOUT=30           # Timeout in seconds
```

---

## Running Tests

### Local Testing

```bash
# Start backend
./mvnw spring-boot:run

# In another terminal
./scripts/load-test.sh http://localhost:8080
```

### Production Testing (Render)

```bash
./scripts/load-test.sh https://portfolio-spring-gmat.onrender.com
```

⚠️ **Warning**: Be careful when load testing production. High load can:
- Trigger rate limits
- Increase hosting costs
- Impact real users

---

## Understanding Results

### Key Metrics

#### 1. Requests per Second (RPS)
- **Higher is better**
- Indicates throughput capacity
- Example: `50 req/s` = can handle 50 concurrent users/sec

#### 2. Time per Request (Mean)
- **Lower is better**
- Average response time
- Example: `200 ms` = typical user sees 200ms delay

#### 3. Percentiles

| Percentile | Meaning |
|------------|---------|
| **p50** | 50% of requests faster than this |
| **p95** | 95% of requests faster than this (good SLA target) |
| **p99** | 99% of requests faster than this (worst case for most users) |

#### 4. Failed Requests
- **Should be 0**
- Any value > 0 indicates errors under load
- Check logs for error details

---

## Baseline Expectations

### Optimal Performance (with cache warm)

| Endpoint | p50 | p95 | p99 | RPS |
|----------|-----|-----|-----|-----|
| Health Check | < 10ms | < 20ms | < 50ms | > 500 |
| Starred Projects | < 50ms | < 150ms | < 300ms | > 100 |
| Portfolio Completion | < 200ms | < 500ms | < 1000ms | > 20 |
| Source Repositories | < 100ms | < 300ms | < 600ms | > 50 |
| Sync Config Status | < 30ms | < 100ms | < 200ms | > 200 |

### Degraded Performance (cold start / no cache)

| Endpoint | p50 | p95 | p99 |
|----------|-----|-----|-----|
| Starred Projects | < 200ms | < 500ms | < 1000ms |
| Portfolio Completion | < 1000ms | < 3000ms | < 5000ms |

---

## Analyzing Results

### 1. Check Cache Hit Rates

```bash
# View cache statistics in logs
grep "Cache hit" logs/application.log

# Or via actuator
curl http://localhost:8080/actuator/metrics/cache.gets | jq
```

### 2. Check Database Query Counts

```bash
# Enable SQL logging in application.properties
spring.jpa.show-sql=true

# Count queries per request
grep "Hibernate:" logs/application.log | wc -l
```

### 3. Profile Slow Endpoints

Use Spring Boot Actuator's metrics:

```bash
# HTTP metrics
curl http://localhost:8080/actuator/metrics/http.server.requests

# JVM metrics
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

---

## Interpreting Performance Issues

### Symptom: High p99 but low p50
- **Cause**: Occasional slow queries or cache misses
- **Solution**:
  - Warm up cache before testing
  - Add database indexes
  - Implement query result caching

### Symptom: High failure rate
- **Cause**: Backend cannot handle concurrency
- **Solution**:
  - Increase connection pool size
  - Add rate limiting
  - Scale horizontally

### Symptom: Low RPS across all endpoints
- **Cause**: Server resource constraint
- **Solution**:
  - Profile JVM (CPU/memory)
  - Increase JVM heap size
  - Optimize blocking operations

---

## Continuous Testing

### Integration with CI/CD

Add to `.github/workflows/performance-test.yml`:

```yaml
name: Performance Tests

on:
  schedule:
    - cron: '0 2 * * 0'  # Weekly on Sunday at 2 AM
  workflow_dispatch:

jobs:
  load-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'

      - name: Start backend
        run: |
          ./mvnw spring-boot:run &
          sleep 30

      - name: Run load tests
        run: ./scripts/load-test.sh

      - name: Upload results
        uses: actions/upload-artifact@v4
        with:
          name: load-test-results
          path: load-test-results/
```

---

## Performance Optimization Checklist

After running load tests, optimize based on results:

- [ ] **Database**
  - [ ] Add indexes on frequently queried columns
  - [ ] Optimize N+1 queries with JOIN FETCH
  - [ ] Enable query result caching

- [ ] **Caching**
  - [ ] Implement Caffeine cache for expensive operations
  - [ ] Set appropriate TTL values
  - [ ] Monitor cache hit rates

- [ ] **Connection Pooling**
  - [ ] Tune HikariCP settings (pool size, timeout)
  - [ ] Monitor connection usage

- [ ] **Rate Limiting**
  - [ ] Add rate limiting for expensive endpoints
  - [ ] Implement circuit breakers for external APIs

- [ ] **Async Processing**
  - [ ] Move long-running tasks to async methods
  - [ ] Use CompletableFuture for parallel operations

---

## Troubleshooting

### Issue: "Connection refused"
**Solution**: Backend is not running. Start with `./mvnw spring-boot:run`

### Issue: "Too many open files"
**Solution**: Increase file descriptor limit
```bash
ulimit -n 10000
```

### Issue: "ab: command not found"
**Solution**: Install Apache Benchmark
```bash
# macOS
brew install httpd

# Ubuntu/Debian
sudo apt-get install apache2-utils
```

---

## Advanced Testing

### Using k6 (Optional)

For more advanced scenarios, install k6:

```bash
brew install k6
```

Create test script (`scripts/load-test.js`):

```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  stages: [
    { duration: '30s', target: 10 },
    { duration: '1m', target: 50 },
    { duration: '30s', target: 0 },
  ],
};

export default function () {
  let res = http.get('http://localhost:8080/api/projects/starred');
  check(res, { 'status is 200': (r) => r.status === 200 });
  sleep(1);
}
```

Run:
```bash
k6 run scripts/load-test.js
```

---

## References

- [Apache Benchmark Documentation](https://httpd.apache.org/docs/2.4/programs/ab.html)
- [Spring Boot Performance Tuning](https://spring.io/guides/gs/spring-boot/)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby)
- [Caffeine Cache](https://github.com/ben-manes/caffeine)

---

**Author**: Bernard Uriza Orozco
**Date**: 2025-10-26
**Version**: 1.0.0
