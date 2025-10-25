# Portfolio Spring Backend - Quick Reference Summary

## Overview
- **Framework**: Spring Boot 3.5.0
- **Java Version**: 21
- **Architecture**: Hexagonal Architecture + Domain-Driven Design
- **Database**: PostgreSQL (primary) / H2 (development)
- **AI Integration**: Claude API (Anthropic)
- **Key Feature**: Two-phase sync pipeline (GitHub ingestion + AI curation)

## Critical Paths

### GitHub Integration
- **Entry Point**: `GitHubSourceRepositoryService.syncStarredRepositories()`
- **Resilience**: Retry (3x), RateLimit (30/60s), CircuitBreaker, TimeLimiter (12s)
- **Storage**: `SourceRepositoryJpaEntity` with sync status tracking

### Claude AI Integration
- **Entry Point**: `AIServiceImpl.analyzeRepository()` via `PortfolioService.curateFromSource()`
- **Token Budget**: Daily 100k token limit with 80% warn threshold
- **Fallback**: Mock data when API unavailable
- **Tone Context**: Auto-loaded from frontend (index.html + i18n service)

### Two-Phase Sync Pipeline
```
Phase 1: GitHub Source Ingest
  ↓ GitHubSourceRepositoryService.syncStarredRepositories()
  ↓ Save to SourceRepositoryJpaEntity (UNSYNCED status)
  
Phase 2: Portfolio Curation
  ↓ AIServiceImpl.analyzeRepository() for each UNSYNCED source
  ↓ PortfolioService.curateFromSource()
  ↓ Create/update PortfolioProjectJpaEntity (respects field protections)
  ↓ Mark SourceRepository as SYNCED/FAILED
```

## Database Entities

### Core Tables
- `portfolio_projects` - Curated portfolio items (with 8 indexes)
- `source_repositories` - GitHub repository metadata (with 8 indexes)
- `skills` - Technical skills
- `experiences` - Work experience records
- `sync_config` - Runtime sync configuration (singleton)
- `reset_audit` - Factory reset audit trail

### Key Relationships
- PortfolioProject → SourceRepository (via sourceRepositoryId, linkType)
- PortfolioProject ⊇ Skills, Experiences (via ID sets)
- SourceRepository tracks sync lifecycle (UNSYNCED → SYNCED/FAILED)

## Controllers & Endpoints

### Public (No Auth)
- `GET /api/sync/projects` - List portfolio projects
- `POST /api/bootstrap/sync-if-empty` - Auto-trigger initial sync
- `GET /api/health` - Health check

### Admin (Token Protected)
- `POST /api/admin/sync-config/run-now` - Manual sync trigger
- `PUT /api/admin/sync-config` - Update sync config (interval, enabled)
- `DELETE /api/admin/source-repositories/{id}` - Delete source
- `GET /api/admin/ai/budget` - Token budget status
- Factory reset, audit logs, analytics, etc.

## Security
- **Method**: Token-based (PORTFOLIO_ADMIN_TOKEN environment variable)
- **Header**: X-Admin-Token
- **Rate Limiting**: Bucket4j with per-IP tracking
- **Feature Flags**: Runtime enable/disable for all major features
- **CORS**: localhost:4200, 127.0.0.1:4200, localhost:5173, 127.0.0.1:5173

## Feature Flags
All configurable via environment variables (application.properties prefixed with `FEATURE_`):
- `FEATURE_AUTO_SYNC_ENABLED` - Automatic sync scheduling
- `FEATURE_MANUAL_SYNC_ENABLED` - Manual sync trigger
- `FEATURE_AI_CURATION_ENABLED` - Claude AI analysis
- `FEATURE_FACTORY_RESET_ENABLED` - Database reset capability
- `FEATURE_ADMIN_ENDPOINTS_ENABLED` - Admin API access
- `FEATURE_RATE_LIMITING_ENABLED` - Rate limit enforcement

## Incomplete Features
1. **No explicit TODOs** - All major features implemented
2. **Placeholder**: `PortfolioProject.hasReadmeMarkdown()` (Line 403) returns true
3. **Database Support**: Only PostgreSQL + H2; missing MySQL, Oracle, SQL Server
4. **Email**: Mock SMTP (localhost:1025) - needs production SMTP

## Missing Integrations
- OAuth2/OpenID Connect
- Centralized logging (ELK, Datadog)
- Distributed caching (Redis)
- Database encryption
- Email notifications
- Message queues (for async jobs)
- Search engine (Elasticsearch)
- Error tracking (Sentry)
- CDN configuration
- Distributed tracing (Jaeger)

## Configuration Files
- **Main Config**: `/Users/bernardurizaorozco/Documents/portfolio-spring/src/main/resources/application.properties`
- **Environment**: `.env` file (copy from `.env.example`)
- **Build**: `pom.xml` (Maven)

## Key Services (18 Total)

### Sync & Integration
- `SyncSchedulerService` - Two-phase orchestration
- `GitHubSourceRepositoryService` - GitHub API integration
- `BootstrapSyncService` - Initial sync on empty portfolio

### Portfolio & AI
- `PortfolioService` - Curation with AI
- `AIServiceImpl` - Claude API adapter
- `ClaudeTokenBudgetService` - Token budget enforcement

### Admin & Security
- `FactoryResetService` - Database cleanup with audit
- `RateLimitingService` - Bucket4j rate limiting
- `AdminTokenAuthenticationFilter` - Token validation

### Monitoring
- `SyncMonitorService` - Sync operation logging
- `JourneyAnalyticsService` - User journey tracking
- `AuditTrailService` - Audit logging
- `CorrelationIdService` - Request tracing

### Utilities
- `FeatureFlagService` - Feature flag management
- `SourceRepositoryService` - Repository CRUD
- `SyncConfigService` - Config management
- `OptimisticLockingService` - Concurrency control

## Important File Locations

### Core Logic
- `/com/portfolio/service/SyncSchedulerService.java` - Main pipeline
- `/com/portfolio/adapter/out/external/ai/AIServiceImpl.java` - Claude integration
- `/com/portfolio/service/GitHubSourceRepositoryService.java` - GitHub integration

### Domain Models
- `/com/portfolio/core/domain/project/PortfolioProject.java` - Portfolio entity
- `/com/portfolio/core/domain/skill/Skill.java` - Skill entity
- `/com/portfolio/core/domain/experience/Experience.java` - Experience entity

### Configuration
- `/com/portfolio/config/AdminSecurityConfig.java` - Security setup
- `/com/portfolio/config/FeatureFlagsConfig.java` - Feature flags
- `/com/portfolio/aspect/RateLimitAspect.java` - Rate limiting

### Persistence
- `/com/portfolio/adapter/out/persistence/jpa/PortfolioProjectJpaEntity.java`
- `/com/portfolio/adapter/out/persistence/jpa/SourceRepositoryJpaEntity.java`

## How to Run

```bash
# Development
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Production
java -jar target/portfolio-spring-0.0.1-SNAPSHOT.jar

# Tests
./mvnw test
```

## Environment Variables Required

```
GITHUB_USERNAME=your-github-username
GITHUB_TOKEN=your-github-personal-access-token
ANTHROPIC_API_KEY=your-claude-api-key
PORTFOLIO_ADMIN_TOKEN=your-secure-admin-token
```

## Metrics Available

Via `/actuator` endpoint:
- `sync.source.total`, `sync.source.synced` (gauges)
- `sync.portfolio.created`, `sync.portfolio.updated`, `sync.portfolio.failed` (counters)
- `sync.pipeline.duration`, `sync.source.ingest.duration`, `sync.portfolio.curation.duration` (timers)
- `claude.tokens.used`, `claude.tokens.remaining`, `claude.budget.usage_percentage` (metrics)
- `github.api.*` - GitHub resilience metrics

---

**Full Report**: See `ANALYSIS_REPORT.md` for detailed analysis with file locations and line numbers.
