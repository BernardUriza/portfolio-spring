# Sprint 4 Plan - Testing, QA & Production Readiness

**Sprint Duration**: 2 weeks
**Sprint Goal**: Complete testing coverage, implement CI/CD pipeline, and achieve production readiness
**Start Date**: 2025-10-26
**End Date**: 2025-11-09

---

## Sprint Objectives

1. **Comprehensive Testing Coverage**
   - Backend integration tests
   - Frontend E2E tests
   - Performance validation

2. **CI/CD Pipeline**
   - Automated build and test
   - Deployment automation
   - Quality gates

3. **Production Features**
   - Project History & Rollback
   - Live Narration enablement
   - Documentation updates

---

## Sprint Backlog

### Epic 1: Testing & QA (HIGH Priority)

#### 🧪 PF-TEST-TASK-001: Backend Integration Tests
**ID**: 68fcfb1b9e16bb9fc1644128
**Labels**: Sprint-4, Testing, Backend
**Estimation**: 12-16 hours

**Scope**:
- [ ] Integration tests for GitHub sync flow
- [ ] Integration tests for AI curation
- [ ] Integration tests for admin endpoints
- [ ] Integration tests for portfolio CRUD
- [ ] Test database setup with Testcontainers
- [ ] Mock external services (GitHub API, Claude API)
- [ ] Test data fixtures
- [ ] CI integration

**Acceptance Criteria**:
- 80% code coverage for service layer
- All critical paths tested
- Tests run in <2 minutes
- No flaky tests

---

#### 🧪 PF-TEST-TASK-002: Frontend E2E Tests
**ID**: 68fcfb1d2f280c3349bdbf70
**Labels**: Sprint-4, Testing, Frontend
**Estimation**: 10-14 hours

**Scope**:
- [ ] E2E tests for admin dashboard navigation
- [ ] E2E tests for project management (CRUD)
- [ ] E2E tests for source repository sync
- [ ] E2E tests for field protections
- [ ] E2E tests for completion analysis
- [ ] Cypress setup and configuration
- [ ] Visual regression testing
- [ ] CI integration

**Acceptance Criteria**:
- Core user flows covered
- Tests run in <3 minutes
- Screenshot comparison for critical UI
- No manual testing required for regression

---

### Epic 2: DevOps & CI/CD (HIGH Priority)

#### 🛠️ PF-DEVOPS-TASK-002: CI/CD Pipeline Completo
**ID**: 68fcfb2a1662903593567656
**Labels**: Sprint-4, DevOps, CI/CD
**Estimation**: 16-20 hours

**Scope**:
- [ ] GitHub Actions workflow setup
- [ ] Backend build and test pipeline
- [ ] Frontend build and test pipeline
- [ ] Automated dependency security scanning
- [ ] Code quality gates (SonarCloud)
- [ ] Automated deployment to staging
- [ ] Production deployment workflow
- [ ] Rollback procedures
- [ ] Environment-specific configurations
- [ ] Secrets management

**Pipeline Stages**:
1. **Build**: Compile backend (Maven) and frontend (npm)
2. **Test**: Run unit + integration + E2E tests
3. **Quality**: SonarCloud analysis, code coverage
4. **Security**: Dependency scanning, SAST
5. **Package**: Docker image build
6. **Deploy**: Staging auto-deploy, prod manual approval
7. **Verify**: Smoke tests post-deployment

**Acceptance Criteria**:
- Full pipeline runs in <10 minutes
- Auto-deploy to staging on main branch
- Manual approval for production
- Rollback tested and documented
- All secrets managed securely

---

### Epic 3: Production Features (MEDIUM Priority)

#### 📜 PF-FEATURE-TASK-001: Project History & Rollback
**ID**: 68fcf363a19af1c9bf961cd8 (from backlog)
**Labels**: Sprint-4, Epic, Feature, Versioning
**Estimation**: 16-20 hours

**Scope**:
- [ ] Design project_history schema
- [ ] Create ProjectHistoryJpaEntity
- [ ] Implement ProjectHistoryRepository
- [ ] Add @PostUpdate listener to PortfolioProject
- [ ] Save JSON snapshot on each update
- [ ] Create GET /admin/portfolio/{id}/history endpoint
- [ ] Create POST /admin/portfolio/{id}/rollback/{version} endpoint
- [ ] Implement diff algorithm
- [ ] Create HistoryViewerComponent (Frontend)
- [ ] Add "Ver historial" button to UI
- [ ] Implement timeline modal
- [ ] Test rollback with field protections

**Database Schema**:
```sql
CREATE TABLE project_history (
    id BIGSERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES portfolio_projects(id),
    version INT NOT NULL,
    snapshot_json JSONB NOT NULL,
    changed_by VARCHAR(255),
    change_type VARCHAR(50), -- CREATE, UPDATE, DELETE
    created_at TIMESTAMP NOT NULL,
    UNIQUE(project_id, version)
);
```

**Acceptance Criteria**:
- Every project update creates history entry
- Rollback restores complete project state
- Field protections respected on rollback
- UI shows visual diff between versions
- Performance: history retrieval < 100ms

---

#### 🎯 PF-FEATURE-TASK-002: Live Narration Production Enablement
**ID**: 68fcf2d43e39cc4b37c478ff (from backlog)
**Labels**: Sprint-4, Feature, AI, Production
**Estimation**: 6-8 hours

**Scope**:
- [ ] Review Claude API token budget allocation
- [ ] Configure rate limiting for live narration
- [ ] Optimize caching strategy for narration responses
- [ ] Test narration performance under load
- [ ] Update environment.prod.ts: liveNarrationEnabled = true
- [ ] Add monitoring for narration requests
- [ ] Create fallback UI for API failures
- [ ] Documentation for narration feature

**Prerequisites**:
1. ✅ Claude API token budget sufficient
2. ✅ Rate limiting configured
3. ✅ Caching optimized
4. ✅ UI/UX finalized

**Acceptance Criteria**:
- Narration enabled in production
- Response time < 2 seconds (90th percentile)
- Graceful degradation on API failures
- Metrics tracked in dashboard
- User feedback mechanism implemented

---

### Epic 4: Documentation & Cleanup (LOW Priority)

#### 📚 PF-DOC-TASK-001: Documentation Updates
**Labels**: Sprint-4, Documentation
**Estimation**: 4-6 hours

**Scope**:
- [ ] Update README.md with production setup
- [ ] Document CI/CD pipeline usage
- [ ] Create testing guide
- [ ] Update API documentation
- [ ] Create deployment runbook
- [ ] Document rollback procedures
- [ ] Add troubleshooting guide

---

#### 🗑️ PF-CLEANUP-TASK-001: Codebase Cleanup
**Labels**: Sprint-4, Refactor, Cleanup
**Estimation**: 4-6 hours

**Scope**:
- [ ] Remove dead code identified in previous sprints
- [ ] Remove unused dependencies
- [ ] Fix code quality warnings
- [ ] Update deprecated dependencies
- [ ] Clean up commented code
- [ ] Organize import statements

---

## Sprint Capacity

**Team Size**: 1 developer (Claude Code Agent)
**Working Hours**: ~60 hours total (3 hours/day × 20 days)

**Story Point Distribution**:
- Testing & QA: 26 hours (43%)
- DevOps & CI/CD: 20 hours (33%)
- Production Features: 24 hours (40%)
- Documentation & Cleanup: 10 hours (17%)

**Total Estimated**: ~80 hours
**Buffer for unknowns**: 25%
**Realistic Commitment**: ~60 hours

---

## Definition of Done

### For Testing Tasks:
- [ ] All tests pass in CI
- [ ] Code coverage meets threshold
- [ ] No flaky tests
- [ ] Tests documented

### For DevOps Tasks:
- [ ] Pipeline runs successfully
- [ ] Documentation complete
- [ ] Secrets secured
- [ ] Rollback tested

### For Feature Tasks:
- [ ] Code reviewed
- [ ] Tests written and passing
- [ ] Documentation updated
- [ ] Performance validated
- [ ] User acceptance tested

---

## Risk Management

### High Risks:
1. **CI/CD Complexity**: Pipeline setup may take longer than estimated
   - **Mitigation**: Start with minimal pipeline, iterate
   - **Contingency**: Use existing manual deployment process

2. **E2E Test Flakiness**: Cypress tests may be unstable
   - **Mitigation**: Use best practices, retries, waits
   - **Contingency**: Focus on critical paths only

### Medium Risks:
1. **Project History Storage**: JSONB size may grow quickly
   - **Mitigation**: Implement retention policy (keep last 50 versions)
   - **Contingency**: Add background job to prune old versions

2. **Live Narration Load**: High traffic may exceed token budget
   - **Mitigation**: Implement aggressive caching, rate limiting
   - **Contingency**: Circuit breaker to disable feature under load

---

## Success Metrics

1. **Testing Coverage**:
   - Backend: ≥80% line coverage
   - Frontend: ≥70% line coverage
   - E2E: Top 10 user flows covered

2. **CI/CD**:
   - Pipeline success rate: ≥95%
   - Deploy time: <10 minutes
   - Zero-downtime deployments

3. **Features**:
   - Project history: 100% of updates captured
   - Live narration: <2s response time

4. **Quality**:
   - Zero critical bugs in production
   - Code quality score: A rating
   - Security vulnerabilities: 0 high/critical

---

## Dependencies

### External:
- GitHub Actions (for CI/CD)
- SonarCloud account (for quality gates)
- Docker Hub / Container Registry
- Staging environment provisioned

### Internal:
- Sprint 2 performance optimizations complete
- Sprint 3 AI analysis stable
- All existing tests passing

---

## Sprint Ceremonies

1. **Sprint Planning**: Monday, Oct 26 (this document)
2. **Daily Standups**: Tracked via Trello comments
3. **Mid-Sprint Review**: Wednesday, Nov 2
4. **Sprint Review**: Friday, Nov 9
5. **Sprint Retrospective**: Friday, Nov 9

---

**Created**: 2025-10-25
**Last Updated**: 2025-10-25
**Status**: 📝 Planning
