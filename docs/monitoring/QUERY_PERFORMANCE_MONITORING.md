# Query Performance Monitoring Guide

## Overview

Query Performance Monitoring (PERF-005) provides comprehensive SQL query tracking, slow query detection, and performance analytics for database operations.

---

## Quick Start

### View Query Statistics

```bash
# All query statistics
curl http://localhost:8080/api/monitoring/query/statistics

# Top 10 slowest queries
curl http://localhost:8080/api/monitoring/query/slow?limit=10

# Queries exceeding threshold
curl http://localhost:8080/api/monitoring/query/above-threshold

# Reset statistics
curl -X POST http://localhost:8080/api/monitoring/query/reset
```

---

## Configuration

### application.properties

```properties
# SQL Query Logging
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
logging.level.org.hibernate.orm.jdbc.bind=TRACE

# JPA Configuration
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.use_sql_comments=true
spring.jpa.properties.hibernate.generate_statistics=true

# Query Performance Monitoring
portfolio.query.slow-query-threshold-ms=100
portfolio.query.enable-slow-query-logging=true
portfolio.query.enable-query-statistics=true
portfolio.query.log-large-result-sets=true
portfolio.query.large-result-set-threshold=1000
```

### Environment Variables (.env)

```bash
# Slow Query Detection
SLOW_QUERY_THRESHOLD_MS=100
ENABLE_SLOW_QUERY_LOGGING=true
ENABLE_QUERY_STATISTICS=true

# Large Result Set Detection
LOG_LARGE_RESULT_SETS=true
LARGE_RESULT_SET_THRESHOLD=1000
```

---

## Features

### 1. Slow Query Detection

**Threshold**: 100ms (configurable)

**Automatic Logging**:
- Queries exceeding threshold logged with `WARN` level
- Repository methods logged with execution time
- Transactional methods monitored for N+1 queries

**Example Log Output**:
```
WARN  SLOW QUERY DETECTED (245ms > 100ms threshold): SELECT * FROM portfolio_project WHERE ...
WARN  SLOW REPOSITORY METHOD (180ms > 100ms threshold): ProjectRepository.findAll()
WARN  POTENTIAL N+1 QUERY ISSUE: PortfolioService.getAllProjects() executed 25 queries in 340ms
```

### 2. Query Statistics Tracking

**Metrics Collected Per Query**:
- Execution count
- Total execution time
- Average execution time
- Min/Max execution time
- Last execution timestamp

**Global Metrics**:
- Total queries executed
- Slow queries count
- Slow query rate
- Total unique query patterns

### 3. N+1 Query Detection

**Automatic Detection**:
- Monitors @Transactional methods
- Counts queries per transaction
- Warns if > 10 queries in single transaction

**Example Alert**:
```
WARN  POTENTIAL N+1 QUERY ISSUE: PortfolioProjectServiceImpl.getAllProjects() executed 25 queries in 340ms
```

### 4. Query Normalization

**Purpose**: Group similar queries for analysis

**Normalization Rules**:
- Replace string literals with `'?'`
- Replace numbers with `?`
- Normalize whitespace
- Convert to uppercase

**Example**:
```sql
-- Original
SELECT * FROM portfolio_project WHERE id = 123 AND name = 'Test'

-- Normalized
SELECT * FROM PORTFOLIO_PROJECT WHERE ID = ? AND NAME = '?'
```

---

## API Endpoints

### GET `/api/monitoring/query/statistics`

Returns comprehensive query statistics.

**Response**:
```json
{
  "available": true,
  "timestamp": "2025-10-26T00:45:00",
  "overall": {
    "totalQueries": 1250,
    "slowQueries": 15,
    "slowQueryThresholdMs": 100,
    "slowQueryRate": 0.012
  },
  "queries": {
    "SELECT * FROM PORTFOLIO_PROJECT WHERE ID = ?": {
      "executionCount": 450,
      "totalExecutionTimeMs": 12500,
      "averageExecutionTimeMs": 27,
      "minExecutionTimeMs": 5,
      "maxExecutionTimeMs": 180,
      "lastExecutionTime": "2025-10-26T00:44:55"
    }
  },
  "totalUniqueQueries": 25
}
```

### GET `/api/monitoring/query/slow?limit=10`

Returns top N slowest queries.

**Parameters**:
- `limit` (optional): Number of queries to return (default: 10)

**Response**:
```json
{
  "available": true,
  "timestamp": "2025-10-26T00:45:00",
  "slowQueryThresholdMs": 100,
  "limit": 10,
  "slowQueries": {
    "SELECT P.*, S.* FROM PORTFOLIO_PROJECT P JOIN ...": {
      "executionCount": 25,
      "averageExecutionTimeMs": 245,
      "maxExecutionTimeMs": 450
    }
  },
  "count": 5
}
```

### GET `/api/monitoring/query/above-threshold`

Returns queries exceeding slow threshold.

**Response**:
```json
{
  "available": true,
  "timestamp": "2025-10-26T00:45:00",
  "slowQueryThresholdMs": 100,
  "queriesAboveThreshold": {
    "SELECT * FROM SOURCE_REPOSITORY WHERE ...": {
      "executionCount": 12,
      "averageExecutionTimeMs": 180,
      "maxExecutionTimeMs": 350,
      "exceedsThresholdBy": 80
    }
  },
  "count": 3
}
```

### POST `/api/monitoring/query/reset`

Resets all query performance statistics.

**Response**:
```json
{
  "status": "success",
  "message": "Query performance statistics have been reset",
  "timestamp": "2025-10-26T00:45:00"
}
```

---

## Architecture

### Components

**1. QueryPerformanceInterceptor**
- Implements Hibernate's `StatementInspector`
- Tracks query execution start time
- Records query statistics
- Detects slow queries

**2. QueryPerformanceAspect**
- AOP @Aspect for repository methods
- Measures execution time
- Detects N+1 queries in @Transactional methods
- Logs warnings for slow operations

**3. MonitoringController**
- Exposes REST endpoints
- Provides query analytics
- Allows statistics reset

### Data Flow

```
SQL Query Execution
        ↓
QueryPerformanceInterceptor.inspect()
        ↓
Store start time
        ↓
Execute query
        ↓
QueryPerformanceAspect @Around
        ↓
Calculate execution time
        ↓
Record statistics
        ↓
Check threshold → Log if slow
        ↓
Update metrics
```

---

## Use Cases

### 1. Development - Find Slow Queries

```bash
# Start app with monitoring
./mvnw spring-boot:run

# Use application normally
# ...

# Check for slow queries
curl http://localhost:8080/api/monitoring/query/slow?limit=5
```

### 2. Identify N+1 Query Problems

**Check Logs**:
```bash
tail -f logs/application.log | grep "POTENTIAL N+1"
```

**Interpretation**:
```
WARN POTENTIAL N+1 QUERY ISSUE: getAllProjects() executed 25 queries
```
→ Likely fetching related entities in a loop
→ Solution: Use JOIN FETCH or @EntityGraph

### 3. Performance Testing

```bash
# Reset statistics before test
curl -X POST http://localhost:8080/api/monitoring/query/reset

# Run load test
./scripts/load-test.sh

# Analyze results
curl http://localhost:8080/api/monitoring/query/statistics > results.json
```

### 4. Production Monitoring

**Check slow query rate**:
```bash
curl http://localhost:8080/api/monitoring/query/statistics | \
  jq '.overall.slowQueryRate'
```

**Alert if > 5% slow queries**:
```bash
SLOW_RATE=$(curl -s http://localhost:8080/api/monitoring/query/statistics | \
  jq '.overall.slowQueryRate')

if (( $(echo "$SLOW_RATE > 0.05" | bc -l) )); then
  echo "ALERT: Slow query rate is ${SLOW_RATE}"
fi
```

---

## Optimization Workflow

### 1. Identify Slow Queries

```bash
curl http://localhost:8080/api/monitoring/query/above-threshold
```

### 2. Analyze Query Pattern

Look for:
- Missing indexes
- Cartesian products (missing JOIN conditions)
- N+1 queries (multiple SELECTs in loop)
- Large result sets without pagination

### 3. Apply Fixes

**Missing Indexes**:
```sql
CREATE INDEX idx_project_starred ON portfolio_project(starred);
```

**N+1 Queries**:
```java
// Before (N+1)
@Query("SELECT p FROM PortfolioProject p")
List<PortfolioProject> findAll();

// After (single query)
@Query("SELECT p FROM PortfolioProject p LEFT JOIN FETCH p.sourceRepository")
List<PortfolioProject> findAllWithRepository();
```

**Pagination**:
```java
Page<PortfolioProject> findAll(Pageable pageable);
```

### 4. Verify Improvements

```bash
# Reset stats
curl -X POST http://localhost:8080/api/monitoring/query/reset

# Test again
# ...

# Compare
curl http://localhost:8080/api/monitoring/query/statistics
```

---

## Thresholds

### Slow Query (Default: 100ms)

| Category | Threshold | Severity |
|----------|-----------|----------|
| Fast | < 50ms | Good |
| Acceptable | 50-100ms | OK |
| Slow | 100-500ms | Warning |
| Very Slow | > 500ms | Critical |

### N+1 Detection (Default: 10 queries)

| Queries | Status |
|---------|--------|
| 1-5 | Normal |
| 6-10 | Acceptable |
| 11-20 | Warning (potential N+1) |
| > 20 | Critical (definite N+1) |

---

## Troubleshooting

### Issue: No statistics showing

**Cause**: Monitoring not enabled

**Solution**: Check configuration
```properties
portfolio.query.enable-query-statistics=true
```

### Issue: Too much logging

**Cause**: TRACE level for bind parameters

**Solution**: Disable bind logging
```properties
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=INFO
```

### Issue: False positive N+1 alerts

**Cause**: Batch processing legitimate queries

**Solution**: Increase threshold in code or use @BatchSize

---

## Best Practices

1. **Development**: Enable full logging including bind parameters
2. **Staging**: Enable slow query detection with 100ms threshold
3. **Production**:
   - Enable statistics collection
   - Set threshold to 200ms (higher for production load)
   - Disable bind parameter logging (TRACE level)
4. **Monitoring**: Review slow queries weekly
5. **Alerts**: Set up alerts for slow query rate > 5%

---

## Integration with Load Testing

```bash
# 1. Reset statistics
curl -X POST http://localhost:8080/api/monitoring/query/reset

# 2. Run load test
./scripts/load-test.sh

# 3. Get slow queries
curl http://localhost:8080/api/monitoring/query/slow?limit=10 > slow-queries.json

# 4. Check N+1 patterns in logs
grep "POTENTIAL N+1" logs/application.log
```

---

## References

- [Hibernate Performance Tuning](https://docs.jboss.org/hibernate/orm/current/userguide/html_single/Hibernate_User_Guide.html#performance)
- [N+1 Query Problem](https://vladmihalcea.com/n-plus-1-query-problem/)
- [Query Optimization Guide](../testing/LOAD_TESTING_GUIDE.md)
- [Performance Dashboard](./PERFORMANCE_DASHBOARD.md)

---

**Author**: Bernard Uriza Orozco
**Date**: 2025-10-26
**Version**: 1.0.0
**Card**: PERF-005 - Query Logging & Slow Query Detection
