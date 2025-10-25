# GitHub Sync Resilience & SSE Progress Tracking

## Overview

This document describes the resilience patterns implemented for GitHub API integration, including retry logic, rate limit handling, circuit breakers, and real-time progress tracking via Server-Sent Events (SSE).

**Created by Bernard Orozco**
**Epic**: PF-SYNC-EPIC-001: GitHub Sync & Repository Versioning

---

## Architecture Components

### 1. GitHubResilienceConfig

**Location**: `src/main/java/com/portfolio/config/GitHubResilienceConfig.java`

**Purpose**: Centralized resilience configuration for GitHub API calls

**Features**:
- **Exponential Backoff**: 1s → 2s → 4s → 8s → 16s (max 30s)
- **Max Retry Attempts**: 5
- **Smart Retry Logic**:
  - Retry on 429 (Rate Limit)
  - Retry on 5xx (Server Errors)
  - Retry on network errors (SocketTimeout, IOException)
  - Skip retry on 4xx (except 429)

**Metrics Collected**:
```
github.rate_limit_hit      - Counter for 429 responses
github.server_error        - Counter for 5xx errors
github.network_error       - Counter for network issues
github.retry.attempts      - Counter per retry attempt
github.retry.success       - Counter for successful retries
github.retry.exhausted     - Counter for exhausted retries
```

**Configuration Example**:
```java
RetryConfig.custom()
    .maxAttempts(5)
    .intervalFunction(IntervalFunction.ofExponentialRandomBackoff(
        Duration.ofSeconds(1),    // initial interval
        2.0,                       // multiplier
        Duration.ofSeconds(30)     // max interval
    ))
```

### 2. GitHubSyncProgressService

**Location**: `src/main/java/com/portfolio/service/GitHubSyncProgressService.java`

**Purpose**: Real-time sync progress tracking via Server-Sent Events

**Sync Phases**:
1. `STARTING` - Initialization
2. `FETCHING_REPOS` - Fetching starred repositories from GitHub
3. `PROCESSING_REPOS` - Processing repository data
4. `FETCHING_README` - Fetching README files
5. `AI_CURATION` - AI analysis (future phase)
6. `COMPLETED` - Success
7. `FAILED` - Error state

**SSE Event Structure**:
```json
{
  "syncId": "uuid",
  "phase": "PROCESSING_REPOS",
  "progressPercentage": 65,
  "totalProcessed": 13,
  "successCount": 12,
  "failureCount": 1,
  "message": "Processed 13/20 repositories",
  "errorDetail": null,
  "timestamp": "2025-10-25T17:30:00"
}
```

**Progress Calculation**:
- STARTING: 0%
- FETCHING_REPOS: 10%
- PROCESSING_REPOS: 30-90% (linear based on repos processed)
- COMPLETED: 100%

### 3. SyncProgressController

**Location**: `src/main/java/com/portfolio/controller/SyncProgressController.java`

**Endpoints**:

#### Stream Progress (SSE)
```http
GET /api/admin/sync/progress/stream/{syncId}
Content-Type: text/event-stream
```

**Response**: Server-Sent Events stream

**Example Client (JavaScript)**:
```javascript
const eventSource = new EventSource('/api/admin/sync/progress/stream/' + syncId);

eventSource.addEventListener('sync-progress', (event) => {
  const progress = JSON.parse(event.data);
  console.log('Progress:', progress.progressPercentage + '%');
  console.log('Message:', progress.message);

  if (progress.phase === 'COMPLETED' || progress.phase === 'FAILED') {
    eventSource.close();
  }
});
```

#### Get Progress (Polling Fallback)
```http
GET /api/admin/sync/progress/{syncId}
```

**Response**:
```json
{
  "totalProcessed": 15,
  "successCount": 14,
  "failureCount": 1,
  "progressPercentage": 75,
  "currentPhase": "PROCESSING_REPOS",
  "lastUpdate": "2025-10-25T17:30:15"
}
```

---

## GitHubSourceRepositoryService Integration

### Enhanced Sync Flow

```java
@CacheEvict(value = {"portfolio-projects", "portfolio-overview"}, allEntries = true)
public void syncStarredRepositories() {
    String syncId = UUID.randomUUID().toString();

    // 1. Starting phase
    progressService.broadcastProgress(syncId, new SyncProgressEvent(
        syncId, SyncPhase.STARTING, 0, 0, 0, 0,
        "Starting GitHub sync...", null
    ));

    // 2. Fetch repositories
    progressService.broadcastProgress(syncId, new SyncProgressEvent(
        syncId, SyncPhase.FETCHING_REPOS, 10, 0, 0, 0,
        "Fetching starred repositories...", null
    ));

    List<GitHubRepo> repos = fetchStarredRepositories(); // With resilience

    // 3. Process repositories with progress updates
    for (int i = 0; i < repos.size(); i++) {
        // Process repo...

        // Update every 5 repos
        if (i % 5 == 0) {
            int progress = 30 + (int) ((i * 60.0) / repos.size());
            progressService.broadcastProgress(syncId, new SyncProgressEvent(
                syncId, SyncPhase.PROCESSING_REPOS, progress,
                i + 1, successCount, failureCount,
                String.format("Processed %d/%d repositories", i + 1, repos.size()), null
            ));
        }
    }

    // 4. Completion
    progressService.markCompleted(syncId, totalRepos, successCount, failureCount);
}
```

---

## Resilience4j Configuration

**File**: `src/main/resources/application.properties`

### GitHub API Resilience

```properties
# Retry Configuration
resilience4j.retry.instances.github.max-attempts=3
resilience4j.retry.instances.github.wait-duration=500ms
resilience4j.retry.instances.github.exponential-backoff-multiplier=2
resilience4j.retry.instances.github.randomized-wait-factor=0.1
resilience4j.retry.instances.github.enable-exponential-backoff=true

# Rate Limiter (30 requests per minute)
resilience4j.ratelimiter.instances.github.limit-for-period=30
resilience4j.ratelimiter.instances.github.limit-refresh-period=60s
resilience4j.ratelimiter.instances.github.timeout-duration=1s

# Circuit Breaker
resilience4j.circuitbreaker.instances.github.sliding-window-size=20
resilience4j.circuitbreaker.instances.github.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.github.wait-duration-in-open-state=30s
resilience4j.circuitbreaker.instances.github.permitted-number-of-calls-in-half-open-state=5
resilience4j.circuitbreaker.instances.github.sliding-window-type=count_based

# Timeout
resilience4j.timelimiter.instances.github.timeout-duration=12s
```

---

## Error Recovery Scenarios

### Scenario 1: GitHub Rate Limit (429)

**Behavior**:
1. Request hits rate limit
2. `GitHubResilienceConfig` detects 429 status
3. Exponential backoff kicks in: 1s → 2s → 4s → 8s → 16s
4. Metric `github.rate_limit_hit` incremented
5. Progress updates reflect retry attempts
6. After successful retry, sync continues

**Logs**:
```
WARN: GitHub rate limit hit (429), will retry with backoff
DEBUG: GitHub API retry attempt 1: rate limit exceeded
INFO: GitHub API call succeeded after 2 retries
```

### Scenario 2: GitHub Server Error (5xx)

**Behavior**:
1. GitHub returns 502/503/504
2. Retry with exponential backoff
3. Circuit breaker tracks failure rate
4. If 50% failure rate in 20 calls → circuit opens
5. Wait 30s before half-open state

### Scenario 3: Network Timeout

**Behavior**:
1. Socket timeout after 12s
2. Retry with backoff
3. SSE shows error message
4. After max retries (5), sync fails gracefully

---

## Monitoring & Metrics

### Prometheus Metrics

All GitHub sync metrics are exported via `/actuator/prometheus`:

```prometheus
# Rate limit tracking
github_rate_limit_hit_total

# Retry metrics
github_retry_attempts_total{attempt="1"}
github_retry_success_total
github_retry_exhausted_total

# Error tracking
github_server_error_total
github_network_error_total
```

### Dashboard Queries

**Rate Limit Hit Rate**:
```promql
rate(github_rate_limit_hit_total[5m])
```

**Retry Success Rate**:
```promql
github_retry_success_total / (github_retry_success_total + github_retry_exhausted_total)
```

**Average Retries Per Sync**:
```promql
avg(github_retry_attempts_total) by (attempt)
```

---

## Testing

### Manual Test: Rate Limit Handling

```bash
# 1. Trigger sync
POST /api/admin/source-repositories/sync

# 2. Connect to SSE stream
GET /api/admin/sync/progress/stream/{syncId}

# 3. Monitor metrics
GET /actuator/prometheus | grep github
```

### Simulating Rate Limit

Temporarily reduce rate limiter config:

```properties
# Allow only 5 requests per minute
resilience4j.ratelimiter.instances.github.limit-for-period=5
```

---

## Future Enhancements

- [ ] Add AI_CURATION phase with progress tracking
- [ ] Implement sync priority queue for multiple concurrent syncs
- [ ] Add WebSocket alternative to SSE
- [ ] Persist sync history in database
- [ ] Add Grafana dashboard template
- [ ] Implement sync cancellation endpoint

---

## References

- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [GitHub API Rate Limiting](https://docs.github.com/en/rest/rate-limit)
- [Server-Sent Events Specification](https://html.spec.whatwg.org/multipage/server-sent-events.html)
- [Spring WebFlux Reactive Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)

---

**Last Updated**: 2025-10-25
**Status**: ✅ Implemented
**Epic**: PF-SYNC-EPIC-001
