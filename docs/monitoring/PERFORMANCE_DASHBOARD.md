# Performance Dashboard - Monitoring & Observability Guide

## Overview

The Performance Dashboard provides comprehensive real-time metrics for monitoring system health, performance, and resource utilization. It consolidates database, cache, JVM, and application metrics into a single unified endpoint.

---

## Quick Start

```bash
# Start the application
./mvnw spring-boot:run

# Access the dashboard
curl http://localhost:8080/api/monitoring/performance/dashboard | python3 -m json.tool

# In production (Render)
curl https://portfolio-spring-gmat.onrender.com/api/monitoring/performance/dashboard
```

---

## Endpoint Details

### GET `/api/monitoring/performance/dashboard`

**Returns**: Comprehensive performance metrics in JSON format

**Response Structure**:
```json
{
  "timestamp": "2025-10-26T00:26:16.789643",
  "timestampMs": 1761459976789,
  "database": { ... },
  "cache": { ... },
  "jvm": { ... },
  "application": { ... },
  "status": { ... }
}
```

---

## Metrics Categories

### 1. Database Metrics

Monitors HikariCP connection pool performance and health.

#### Connection Pool Stats

```json
{
  "database": {
    "connectionPool": {
      "activeConnections": 2,
      "idleConnections": 8,
      "totalConnections": 10,
      "threadsAwaitingConnection": 0
    }
  }
}
```

| Metric | Description | Normal Range |
|--------|-------------|--------------|
| `activeConnections` | Currently executing queries | 0-8 |
| `idleConnections` | Available connections | 2-10 |
| `totalConnections` | Total pool size | 10 |
| `threadsAwaitingConnection` | Threads waiting for connection | 0 (should be 0) |

#### Pool Configuration

```json
{
  "poolConfig": {
    "maxPoolSize": 10,
    "minIdle": 10,
    "connectionTimeout": 30000,
    "idleTimeout": 600000,
    "maxLifetime": 1800000
  }
}
```

#### Health Indicators

```json
{
  "health": {
    "poolUtilization": 0.2,
    "hasWaitingThreads": false,
    "status": "healthy"
  }
}
```

| Indicator | Meaning | Thresholds |
|-----------|---------|------------|
| `poolUtilization` | Active connections / Total | < 0.7 healthy, 0.7-0.9 degraded, > 0.9 critical |
| `hasWaitingThreads` | Any threads blocked? | `false` = healthy, `true` = degraded |
| `status` | Overall DB health | `healthy`, `degraded` |

---

### 2. Cache Metrics

Monitors Caffeine cache performance and hit rates.

#### Per-Cache Statistics

```json
{
  "cache": {
    "caches": {
      "portfolio-projects": {
        "size": 15,
        "hitCount": 450,
        "missCount": 50,
        "hitRate": 0.9,
        "missRate": 0.1,
        "evictionCount": 2,
        "averageLoadPenalty": 250000000
      }
    }
  }
}
```

| Metric | Description | Optimal Range |
|--------|-------------|---------------|
| `size` | Cached entries | Varies by cache |
| `hitCount` | Cache hits | Higher is better |
| `missCount` | Cache misses | Lower is better |
| `hitRate` | Hit ratio (0.0-1.0) | > 0.7 excellent, 0.5-0.7 good, < 0.5 poor |
| `evictionCount` | Items evicted | Low is good |
| `averageLoadPenalty` | Avg load time (ns) | < 500ms |

#### Overall Cache Performance

```json
{
  "overall": {
    "totalHits": 1200,
    "totalMisses": 150,
    "totalRequests": 1350,
    "overallHitRate": 0.89
  }
}
```

**Interpretation**:
- **Hit Rate > 0.8**: Excellent caching efficiency
- **Hit Rate 0.5-0.8**: Good, consider increasing TTL
- **Hit Rate < 0.5**: Poor, review cache strategy

---

### 3. JVM Metrics

Monitors Java Virtual Machine performance.

#### Memory Metrics

```json
{
  "jvm": {
    "memory": {
      "heapUsedMB": 256,
      "heapCommittedMB": 512,
      "heapMaxMB": 2048,
      "heapUtilization": 0.125,
      "nonHeapUsedMB": 124,
      "runtimeMaxMB": 2048,
      "runtimeFreeMB": 1792
    }
  }
}
```

| Metric | Description | Healthy Range |
|--------|-------------|---------------|
| `heapUsedMB` | Current heap usage | < 75% of max |
| `heapMaxMB` | Maximum heap size | 2048 MB (production) |
| `heapUtilization` | Usage ratio | < 0.75 healthy, 0.75-0.85 warning, > 0.85 critical |
| `nonHeapUsedMB` | Metaspace + Code cache | < 200 MB |

#### Thread Metrics

```json
{
  "threads": {
    "threadCount": 27,
    "peakThreadCount": 35,
    "daemonThreadCount": 22,
    "totalStartedThreadCount": 120
  }
}
```

**Normal Values**:
- `threadCount`: 20-40 (varies by load)
- `peakThreadCount`: Should stabilize after startup
- Growing `threadCount` may indicate thread leak

#### Garbage Collection

```json
{
  "gc": {
    "collectionCount": 51,
    "collectionTimeMs": 190
  }
}
```

**Interpretation**:
- `collectionTimeMs` should be < 5% of uptime
- Frequent GC (high `collectionCount`) indicates memory pressure

---

### 4. Application Metrics

High-level application health and uptime.

```json
{
  "application": {
    "applicationName": "portfolio-spring",
    "uptimeMs": 3600000,
    "uptimeFormatted": "1h 0m 0s",
    "profiles": ["render"],
    "port": 8080,
    "environment": "render",
    "keepAlive": {
      "successCount": 120,
      "failureCount": 2,
      "successRate": 0.98
    }
  }
}
```

| Metric | Description |
|--------|-------------|
| `uptimeMs` | Milliseconds since startup |
| `profiles` | Active Spring profiles |
| `environment` | `local` or `render` |
| `keepAlive.successRate` | > 0.95 is healthy |

---

### 5. Performance Status

Automated health scoring based on all metrics.

```json
{
  "status": {
    "score": 85,
    "status": "degraded",
    "issues": "JVM heap utilization high (78%). Cache hit rate low (45%).",
    "recommendations": "Increase JVM heap size (-Xmx) or investigate memory leaks. Review cache configuration and TTL settings."
  }
}
```

#### Status Levels

| Score | Status | Meaning |
|-------|--------|---------|
| 90-100 | `healthy` | All systems optimal |
| 70-89 | `degraded` | Performance issues detected |
| < 70 | `critical` | Immediate attention required |

#### Scoring Deductions

| Issue | Deduction | Threshold |
|-------|-----------|-----------|
| High DB pool utilization | -20 points | > 90% |
| Threads waiting for DB | -15 points | > 0 threads |
| Low cache hit rate | -15 points | < 50% (with > 100 requests) |
| High JVM heap | -10 points | 75-85% utilization |
| Critical JVM heap | -25 points | > 85% utilization |

---

## Alert Thresholds

### Critical (Immediate Action)

- **Database Pool Utilization > 90%**: Risk of connection exhaustion
- **JVM Heap Utilization > 85%**: Risk of OutOfMemoryError
- **Threads Awaiting Connection > 0**: Database bottleneck

### Warning (Review Needed)

- **Cache Hit Rate < 50%**: Ineffective caching
- **JVM Heap Utilization 75-85%**: Memory pressure
- **Database Pool Utilization 70-90%**: Approaching limit

### Information (Monitor)

- **Cache Eviction Count Growing**: May need larger cache
- **GC Time > 5% of Uptime**: Tune GC settings
- **Thread Count Growing**: Potential thread leak

---

## Monitoring Strategies

### 1. Real-Time Monitoring

**Setup**: Poll dashboard every 30 seconds

```bash
# Simple monitoring loop
while true; do
  curl -s http://localhost:8080/api/monitoring/performance/dashboard | \
    python3 -c "import sys, json; data=json.load(sys.stdin); print(f\"Status: {data['status']['status']} | Score: {data['status']['score']} | Heap: {data['jvm']['memory']['heapUtilization']:.1%}\")"
  sleep 30
done
```

**Output**:
```
Status: healthy | Score: 100 | Heap: 12.5%
Status: healthy | Score: 95 | Heap: 15.2%
Status: degraded | Score: 85 | Heap: 78.4%
```

### 2. Alerting Integration

**Example**: Slack webhook on degraded status

```bash
#!/bin/bash
DASHBOARD=$(curl -s http://localhost:8080/api/monitoring/performance/dashboard)
STATUS=$(echo $DASHBOARD | python3 -c "import sys, json; print(json.load(sys.stdin)['status']['status'])")

if [ "$STATUS" != "healthy" ]; then
  curl -X POST https://hooks.slack.com/services/YOUR/WEBHOOK/URL \
    -H 'Content-Type: application/json' \
    -d "{\"text\": \"Performance Alert: $STATUS\"}"
fi
```

### 3. Historical Tracking

**Store metrics in time-series database**:

```bash
# Example: Log to file for graphing
curl -s http://localhost:8080/api/monitoring/performance/dashboard | \
  python3 -c "import sys, json, time; data=json.load(sys.stdin); print(f\"{time.time()},{data['status']['score']},{data['jvm']['memory']['heapUtilization']},{data['cache']['overall']['overallHitRate']}\")" \
  >> metrics.csv
```

---

## Optimization Recommendations

### When Database Pool Utilization is High

1. **Increase Pool Size** (in `application.properties`):
   ```properties
   spring.datasource.hikari.maximum-pool-size=20
   ```

2. **Optimize Queries**:
   - Review slow queries
   - Add database indexes
   - Use `@Transactional` appropriately

3. **Add Connection Timeout**:
   ```properties
   spring.datasource.hikari.connection-timeout=20000
   ```

### When Cache Hit Rate is Low

1. **Increase TTL**:
   ```java
   @Cacheable(value = "portfolio-projects",
              cacheManager = "caffeineCacheManager",
              unless = "#result == null")
   ```

2. **Review Cache Eviction Policy**:
   ```properties
   spring.cache.caffeine.spec=maximumSize=500,expireAfterWrite=30m
   ```

3. **Warm Up Cache** on startup:
   ```java
   @PostConstruct
   public void warmUpCache() {
       projectService.getAllProjects();
   }
   ```

### When JVM Heap is High

1. **Increase Heap Size**:
   ```bash
   # Local
   ./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx1024m"

   # Render (environment variable)
   JAVA_TOOL_OPTIONS=-Xmx512m -Xms256m
   ```

2. **Profile Memory Usage**:
   ```bash
   # Enable GC logging
   -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -Xloggc:gc.log
   ```

3. **Check for Memory Leaks**:
   - Use VisualVM or JProfiler
   - Review `@Cacheable` annotations
   - Check for circular references

---

## Integration with Load Testing

Use load tests to validate performance thresholds:

```bash
# Start load test
./scripts/load-test.sh http://localhost:8080

# Monitor dashboard during load
watch -n 5 'curl -s http://localhost:8080/api/monitoring/performance/dashboard | \
  python3 -c "import sys, json; data=json.load(sys.stdin); print(f\"Score: {data[\"status\"][\"score\"]} | DB Pool: {data[\"database\"][\"health\"][\"poolUtilization\"]:.1%} | Heap: {data[\"jvm\"][\"memory\"][\"heapUtilization\"]:.1%}\")"'
```

**Expected Behavior Under Load**:
- DB Pool Utilization: 20-50%
- Cache Hit Rate: > 70% (after warm-up)
- JVM Heap: < 60%
- Status: `healthy` or `degraded` (never `critical`)

---

## Grafana Dashboard (Optional)

For advanced visualization, integrate with Prometheus + Grafana:

1. **Add Prometheus Dependency** (already configured):
   ```xml
   <dependency>
       <groupId>io.micrometer</groupId>
       <artifactId>micrometer-registry-prometheus</artifactId>
   </dependency>
   ```

2. **Access Prometheus Metrics**:
   ```
   http://localhost:8080/actuator/prometheus
   ```

3. **Grafana Queries**:
   ```promql
   # Cache hit rate
   rate(cache_gets_total{result="hit"}[5m]) / rate(cache_gets_total[5m])

   # JVM heap usage
   jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"}

   # DB connection pool
   hikaricp_connections_active / hikaricp_connections_max
   ```

---

## Troubleshooting

### Dashboard Returns 500 Error

**Cause**: HikariCP not configured or DataSource unavailable

**Solution**: Check `application.properties` for database configuration:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/portfolio
spring.datasource.username=postgres
spring.datasource.password=password
```

### Metrics Show "available: false"

**Cause**: DataSource injection failed

**Solution**: The dashboard gracefully handles missing components. Database metrics will show:
```json
{
  "database": {
    "available": false,
    "message": "HikariCP DataSource not available"
  }
}
```

This is normal for H2 in-memory databases or when using non-HikariCP datasources.

### Cache Hit Rate Always 0.0

**Cause**: Caches not yet used or stats not enabled

**Solution**: Ensure cache stats recording is enabled in `CacheConfig.java`:
```java
Caffeine.newBuilder()
    .recordStats()  // IMPORTANT!
    .expireAfterWrite(30, TimeUnit.MINUTES)
    .build();
```

---

## API Comparison

| Endpoint | Purpose | Use Case |
|----------|---------|----------|
| `/api/monitoring/performance/dashboard` | Comprehensive metrics | Full system overview, alerting |
| `/api/monitoring/cache/stats` | Cache-only metrics | Deep cache analysis |
| `/api/monitoring/status` | Server status + keep-alive | Quick health check |
| `/actuator/health` | Basic health | Load balancer checks |
| `/actuator/prometheus` | Prometheus metrics | Grafana integration |

---

## Performance Impact

The dashboard endpoint is designed to be lightweight:

- **Response Time**: < 50ms (local), < 100ms (production)
- **CPU Overhead**: Negligible (JVM MXBeans are optimized)
- **Memory Overhead**: < 1MB per request
- **Safe for Polling**: Can be called every 10-30 seconds

---

## Best Practices

1. **Monitor Continuously**: Poll dashboard every 30 seconds in production
2. **Set Up Alerts**: Trigger notifications on `degraded` or `critical` status
3. **Correlate with Load Tests**: Run load tests and monitor dashboard
4. **Track Trends**: Store metrics over time to detect gradual degradation
5. **Review Recommendations**: Act on automated recommendations
6. **Combine with Logs**: Cross-reference with application logs for root cause analysis

---

## References

- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby)
- [Caffeine Cache Guide](https://github.com/ben-manes/caffeine/wiki)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [JVM Monitoring](https://docs.oracle.com/en/java/javase/21/management/monitoring-and-management-using-jmx-technology.html)
- [Load Testing Guide](../testing/LOAD_TESTING_GUIDE.md)

---

**Author**: Bernard Uriza Orozco
**Date**: 2025-10-26
**Version**: 1.0.0
**Card**: PERF-004 - Performance Metrics Dashboard
