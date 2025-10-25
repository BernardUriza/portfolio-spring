# Sprint 4 Tracker - Testing, QA & Production Readiness

**Sprint**: Sprint 4
**Duration**: Oct 26, 2025 - Nov 9, 2025 (2 weeks)
**Goal**: Complete testing coverage, implement CI/CD pipeline, and achieve production readiness

---

## Sprint Status

**Overall Progress**: 0% (0/80 hours completed)
**Sprint Health**: 🟢 On Track
**Blockers**: None
**Last Updated**: 2025-10-25

---

## Epic 1: Testing & QA (26 hours)

### 🧪 PF-TEST-TASK-001: Backend Integration Tests
**Card ID**: 68fcfb1b9e16bb9fc1644128
**Status**: ✅ Ready
**Labels**: Sprint-4, Testing, Backend
**Estimation**: 12-16 hours
**Progress**: 0%

**Tasks**:
- [ ] Setup Testcontainers for PostgreSQL
- [ ] Integration tests for GitHub sync flow
- [ ] Integration tests for AI curation
- [ ] Integration tests for admin endpoints (CRUD)
- [ ] Integration tests for portfolio CRUD
- [ ] Mock external services (GitHub API, Claude API)
- [ ] Create test data fixtures
- [ ] CI integration
- [ ] Code coverage report

**Acceptance**: 80% service layer coverage, tests run in <2 minutes

---

### 🧪 PF-TEST-TASK-002: Frontend E2E Tests
**Card ID**: 68fcfb1d2f280c3349bdbf70
**Status**: ✅ Ready
**Labels**: Sprint-4, Testing, Frontend
**Estimation**: 10-14 hours
**Progress**: 0%

**Tasks**:
- [ ] Cypress setup and configuration
- [ ] E2E: Admin dashboard navigation
- [ ] E2E: Project management (CRUD)
- [ ] E2E: Source repository sync flow
- [ ] E2E: Field protections workflow
- [ ] E2E: Completion analysis
- [ ] Visual regression testing setup
- [ ] CI integration
- [ ] Flaky test prevention

**Acceptance**: Core user flows covered, tests run in <3 minutes

---

## Epic 2: DevOps & CI/CD (20 hours)

### 🛠️ PF-DEVOPS-TASK-002: CI/CD Pipeline Completo
**Card ID**: 68fcfb2a1662903593567656
**Status**: ✅ Ready
**Labels**: Sprint-4, DevOps, CI/CD
**Estimation**: 16-20 hours
**Progress**: 0%

**Tasks**:
- [ ] GitHub Actions workflow setup
- [ ] Backend pipeline (Maven build + test)
- [ ] Frontend pipeline (npm build + test)
- [ ] Dependency security scanning (Snyk/Dependabot)
- [ ] Code quality gates (SonarCloud)
- [ ] Docker image build
- [ ] Staging auto-deployment
- [ ] Production manual approval workflow
- [ ] Rollback procedures
- [ ] Environment-specific configurations
- [ ] Secrets management (GitHub Secrets)
- [ ] Smoke tests post-deployment

**Pipeline Stages**:
1. ✅ Build (Maven + npm)
2. ✅ Test (unit + integration + E2E)
3. ✅ Quality (SonarCloud analysis)
4. ✅ Security (dependency scanning, SAST)
5. ✅ Package (Docker image)
6. ✅ Deploy (staging auto, prod manual)
7. ✅ Verify (smoke tests)

**Acceptance**: Pipeline runs in <10 minutes, auto-deploy to staging

---

## Epic 3: Production Features (24 hours)

### 📜 PF-FEATURE-TASK-001: Project History & Rollback
**Card ID**: 68fcf363a19af1c9bf961cd8
**Status**: ✅ Ready
**Labels**: Sprint-4, Epic, Feature, Versioning
**Estimation**: 16-20 hours
**Progress**: 0%

**Tasks**:

**Backend** (10-12 hours):
- [ ] Design project_history schema (Flyway migration)
- [ ] Create ProjectHistoryJpaEntity
- [ ] Implement ProjectHistoryRepository
- [ ] Add @PostUpdate listener to PortfolioProject
- [ ] Save JSON snapshot on each update
- [ ] Create GET /admin/portfolio/{id}/history endpoint
- [ ] Create POST /admin/portfolio/{id}/rollback/{version} endpoint
- [ ] Implement diff algorithm (jsondiff library)
- [ ] Add retention policy (keep last 50 versions)
- [ ] Unit tests for history service

**Frontend** (6-8 hours):
- [ ] Create HistoryViewerComponent
- [ ] Add "Ver historial" button to ProjectFormComponent
- [ ] Implement timeline modal
- [ ] Show diff visualization
- [ ] Rollback confirmation dialog
- [ ] Integration with admin service
- [ ] UI tests

**Acceptance**: Every update captured, rollback restores complete state, field protections respected

---

### 🎯 PF-FEATURE-TASK-002: Live Narration Production Enablement
**Card ID**: 68fcf2d43e39cc4b37c478ff
**Status**: ✅ Ready
**Labels**: Sprint-4, Feature, AI, Production
**Estimation**: 6-8 hours
**Progress**: 0%

**Tasks**:
- [ ] Review Claude API token budget allocation
- [ ] Configure rate limiting for live narration
- [ ] Optimize caching strategy for narration responses
- [ ] Load testing for narration endpoint
- [ ] Update environment.prod.ts: liveNarrationEnabled = true
- [ ] Add monitoring for narration requests
- [ ] Create fallback UI for API failures
- [ ] Documentation for narration feature
- [ ] User feedback mechanism

**Acceptance**: Enabled in production, <2s response time (90th percentile), graceful degradation

---

## Epic 4: Documentation & Cleanup (10 hours)

### 📚 PF-DOC-TASK-001: Documentation Updates
**Labels**: Sprint-4, Documentation
**Estimation**: 4-6 hours
**Progress**: 0%

**Tasks**:
- [ ] Update README.md with production setup
- [ ] Document CI/CD pipeline usage
- [ ] Create testing guide
- [ ] Update API documentation (Swagger/OpenAPI)
- [ ] Create deployment runbook
- [ ] Document rollback procedures
- [ ] Add troubleshooting guide
- [ ] Update architecture diagrams

---

### 🗑️ PF-CLEANUP-TASK-001: Codebase Cleanup
**Labels**: Sprint-4, Refactor, Cleanup
**Estimation**: 4-6 hours
**Progress**: 0%

**Tasks**:
- [ ] Remove dead code (identified in Sprint 2)
- [ ] Remove unused dependencies (pom.xml + package.json)
- [ ] Fix SonarCloud code quality warnings
- [ ] Update deprecated dependencies
- [ ] Clean up commented code
- [ ] Organize import statements
- [ ] Remove console.log statements

---

## Sprint Velocity

**Planned**: 80 hours
**Committed**: 60 hours (with buffer)
**Completed**: 0 hours

**Daily Burn Rate**: 3 hours/day (target)

### Burn Down Chart
```
Day 1  (Oct 26): 60h remaining
Day 2  (Oct 27): 57h remaining
Day 3  (Oct 28): 54h remaining
...
Day 14 (Nov 9):  0h remaining (target)
```

---

## Risk Register

| Risk | Severity | Mitigation | Status |
|------|----------|------------|--------|
| CI/CD pipeline complexity | HIGH | Start with minimal pipeline, iterate | 🟡 Monitoring |
| E2E test flakiness | MEDIUM | Use best practices, retries | 🟡 Monitoring |
| Project history storage growth | MEDIUM | Retention policy (50 versions) | 🟢 Mitigated |
| Live narration API load | MEDIUM | Aggressive caching, circuit breaker | 🟢 Mitigated |

---

## Blockers & Dependencies

**Current Blockers**: None

**Dependencies**:
- ✅ Sprint 2 performance optimizations complete
- ✅ Sprint 3 AI analysis stable
- ⏳ GitHub Actions enabled on repository
- ⏳ SonarCloud account setup
- ⏳ Staging environment provisioned

---

## Sprint Ceremonies

| Ceremony | Date | Status |
|----------|------|--------|
| Sprint Planning | Oct 25, 2025 | ✅ Done |
| Daily Standup #1 | Oct 26, 2025 | ⏳ Pending |
| Mid-Sprint Review | Nov 2, 2025 | ⏳ Pending |
| Sprint Review | Nov 9, 2025 | ⏳ Pending |
| Sprint Retrospective | Nov 9, 2025 | ⏳ Pending |

---

## Success Metrics

### Code Coverage
- **Backend**: Target ≥80% | Current: TBD
- **Frontend**: Target ≥70% | Current: TBD
- **E2E**: Top 10 flows | Current: 0/10

### CI/CD
- **Pipeline Success Rate**: Target ≥95% | Current: N/A
- **Deploy Time**: Target <10 min | Current: N/A
- **Zero-Downtime**: Target 100% | Current: N/A

### Quality
- **Critical Bugs**: Target 0 | Current: 0
- **Code Quality**: Target A | Current: TBD
- **Security Vulns**: Target 0 high/critical | Current: TBD

---

## Daily Progress Log

### Friday, Oct 25, 2025
- ✅ Sprint 4 planning completed
- ✅ Plan document created
- ✅ Tracker document created
- ✅ Trello cards labeled and organized
- ✅ Cards moved from backlog to Ready

**Tomorrow's Focus**: Start backend integration tests setup

---

**Last Updated**: 2025-10-25 17:45 MST
**Next Review**: 2025-10-26 09:00 MST
