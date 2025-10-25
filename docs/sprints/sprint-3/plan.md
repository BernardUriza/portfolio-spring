# 📊 SPRINT 3 PLAN: Testing & CI/CD

**Sprint**: Testing & CI/CD Automation
**Duración**: 3 días (2025-10-28 a 2025-10-30)
**Compromiso**: 3h/día × 3 días = 9h total
**Velocity esperado**: 0.55 (basado en Sprint 2)

---

## 🎯 OBJETIVOS DEL SPRINT

### Tema Principal: **Testing & Continuous Integration**

Después de optimizar el performance en Sprint 2, ahora es el momento de asegurar la calidad del código mediante testing automatizado y CI/CD pipeline.

### Metas del Sprint
1. ✅ **Completar suite de tests backend** (70% coverage)
2. ✅ **Implementar E2E tests críticos** (flujos principales)
3. ✅ **Establecer CI/CD pipeline** (GitHub Actions)
4. 📊 **Mantener calidad de código** (automated checks)

---

## 📋 TARJETAS SELECCIONADAS DE TRELLO

### P0 - Critical (Must Complete)

#### 1. 🧪 PF-TEST-TASK-001: Backend Integration Tests
**ID Trello**: `68fcfb1b9e16bb9fc1644128`
**URL**: https://trello.com/c/2fmVjVFk/52-%F0%9F%A7%AA-pf-test-task-001-backend-integration-tests
**Estimado**: 12-16h → **8h ajustado** (velocity 0.55)
**Prioridad**: P0

**Scope para Sprint 3**:
- Setup TestContainers para PostgreSQL
- MockGitHubApiService con respuestas realistas
- MockClaudeService con respuestas simuladas
- Test AdminPortfolioController (CRUD completo)
- Test GitHubSourceRepositoryService (sync flow)
- Test AIServiceImpl (prompt injection, token budget)
- Coverage report con JaCoCo

**Criterios de Aceptación**:
- [ ] ≥70% coverage en services
- [ ] ≥60% coverage en controllers
- [ ] Tests pasan en CI/CD
- [ ] Mock services funcionando correctamente

**Distribución**:
- Day 1: 3h (Setup + Controllers)
- Day 2: 3h (Services + AI)
- Day 3: 2h (Coverage + Documentation)

---

#### 2. 🧪 PF-TEST-TASK-002: Frontend E2E Tests
**ID Trello**: `68fcfb1d2f280c3349bdbf70`
**URL**: https://trello.com/c/lk9F0XDe/53-%F0%9F%A7%AA-pf-test-task-002-frontend-e2e-tests
**Estimado**: 10-12h → **6h ajustado** (velocity 0.55)
**Prioridad**: P0

**Scope para Sprint 3** (flujos críticos):
- Instalar Playwright
- Configurar playwright.config.ts
- Test: Admin authentication flow
- Test: Manual sync trigger + status polling
- Test: Repository linking workflow
- Test: Project CRUD operations
- CI/CD integration

**Criterios de Aceptación**:
- [ ] Playwright instalado y configurado
- [ ] 4-5 flujos críticos cubiertos
- [ ] Tests ejecutando en CI/CD
- [ ] Screenshots/videos en caso de fallos

**Distribución**:
- Day 2: 2h (Setup + Auth flow)
- Day 3: 4h (Sync + CRUD + CI/CD)

---

### P1 - Important (Complete if Time Allows)

#### 3. 🛠️ PF-DEVOPS-TASK-002: CI/CD Pipeline Completo
**ID Trello**: `68fcfb2a1662903593567656`
**URL**: https://trello.com/c/KbMGC9RH/60-%F0%9F%9B%A0%EF%B8%8F-pf-devops-task-002-ci-cd-pipeline-completo
**Estimado**: 10-14h → **3h ajustado** (SCOPE REDUCIDO para Sprint 3)
**Prioridad**: P1

**Scope para Sprint 3** (MVP Pipeline):
- Crear .github/workflows/ci.yml
- Setup Maven build (mvn clean package)
- Setup npm build (npm run build:prod)
- Integrar tests unitarios (mvn test, npm test)
- Integrar E2E tests de TASK-002
- Badges de build status en README

**Criterios de Aceptación**:
- [ ] CI workflow ejecutando en cada push
- [ ] Backend tests integrados
- [ ] Frontend tests integrados
- [ ] Build passing badge visible

**Distribución**:
- Day 2: 1h (Setup workflow)
- Day 3: 2h (Test integration + badges)

**SCOPE DIFERIDO a Sprint 4**:
- Security scanning (Snyk, Dependabot)
- Deploy automático a staging
- Production deployment workflow

---

## 📅 PLAN DÍA POR DÍA

### DÍA 1: Backend Testing Foundation (3h)
**Focus**: Setup + Controller Tests

**Tasks**:
1. Setup TestContainers (30min)
2. MockGitHubApiService implementation (45min)
3. MockClaudeService implementation (45min)
4. AdminPortfolioController tests - CRUD (60min)

**Deliverable**: Foundation tests running with mocks

---

### DÍA 2: Backend Services + E2E Setup (6h total)
**Focus**: Service Tests + E2E Foundation

**Backend (3h)**:
5. GitHubSourceRepositoryService tests (90min)
6. AIServiceImpl tests (60min)
7. Coverage report generation (30min)

**Frontend E2E (2h)**:
8. Install Playwright + config (30min)
9. Admin auth flow test (45min)
10. Manual sync test (45min)

**CI/CD (1h)**:
11. GitHub Actions workflow setup (60min)

**Deliverable**: Backend tests complete, E2E framework ready, CI pipeline draft

---

### DÍA 3: E2E Tests + CI/CD Integration (6h total)
**Focus**: Complete E2E + Pipeline

**E2E (4h)**:
12. Repository linking workflow test (90min)
13. Project CRUD operations test (90min)
14. E2E CI/CD integration (60min)

**CI/CD (2h)**:
15. Backend test integration (45min)
16. Frontend test integration (45min)
17. Build badges + documentation (30min)

**Deliverable**: Complete testing suite + CI/CD pipeline operational

---

## 📊 MÉTRICAS & OBJETIVOS

### Coverage Targets
| Component | Target | Stretch Goal |
|-----------|--------|--------------|
| Backend Services | 70% | 80% |
| Backend Controllers | 60% | 70% |
| Frontend E2E Flows | 5 flows | 8 flows |

### Sprint Completion
| Priority | Tasks | Estimado | Ajustado (v0.55) |
|----------|-------|----------|------------------|
| P0 | 2 tasks | 22-28h | 14h |
| P1 | 1 task | 10-14h | 3h |
| **Total** | **3 tasks** | **32-42h** | **17h** |

**Daily Budget**: 3h/day × 3 days = 9h
**Planned Work**: 17h adjusted = ~9h real (perfect fit!)

### Success Criteria
- [ ] ≥70% backend test coverage
- [ ] 5+ critical E2E flows covered
- [ ] CI pipeline running on all PRs
- [ ] Build passing badge visible
- [ ] All P0 tasks completed
- [ ] Documentation updated

---

## 🚩 RIESGOS & MITIGACIÓN

### Riesgo 1: TestContainers puede ser complejo
**Probabilidad**: Media
**Impacto**: Alto
**Mitigación**:
- Usar H2 in-memory como fallback
- Setup template desde documentación oficial
- Dedicar tiempo Day 1 a resolver issues

### Riesgo 2: E2E tests pueden ser flaky
**Probabilidad**: Alta
**Impacto**: Medio
**Mitigación**:
- Usar waitFor strategies correctamente
- Implementar retry logic en Playwright
- Screenshots para debugging

### Riesgo 3: CI/CD puede consumir más tiempo
**Probabilidad**: Media
**Impacto**: Medio
**Mitigación**:
- Scope MVP reducido para Sprint 3
- Diferir security scanning a Sprint 4
- Usar templates de GitHub Actions

---

## 📦 SCOPE DIFERIDO

### Para Sprint 4: CI/CD Advanced
- Security scanning (Snyk, Dependabot)
- Staging deployment automation
- E2E tests en staging environment
- Production deployment con approval
- Environment secrets management

### Para Sprint 5: Testing Advanced
- Performance testing integration
- Visual regression testing
- Contract testing (API)
- Mutation testing
- Test data factories

---

## 🔄 DEPENDENCIAS

### De Sprint 2 (Completados)
- ✅ PERF-001: Database optimizations (tests más rápidos)
- ✅ PERF-002: Cache eviction (tests de cache behavior)

### Para Sprint 4
- 🚀 Production Hardening (actualmente en Testing)
- 🔒 Security features
- 📊 Advanced monitoring

---

## 📚 REFERENCIAS

### Documentación
- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Playwright Documentation](https://playwright.dev/docs/intro)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [TestContainers Documentation](https://www.testcontainers.org/)

### Tarjetas Trello
1. Backend Tests: https://trello.com/c/2fmVjVFk
2. E2E Tests: https://trello.com/c/lk9F0XDe
3. CI/CD Pipeline: https://trello.com/c/KbMGC9RH

---

## ✅ CHECKLIST PRE-SPRINT

- [ ] Mover PF-TEST-TASK-001 a "Ready"
- [ ] Mover PF-TEST-TASK-002 a "Ready"
- [ ] Mover PF-DEVOPS-TASK-002 a "Ready"
- [ ] Crear SPRINT_3_TRACKER.md
- [ ] Review velocity de Sprint 2 (0.55)
- [ ] Confirmar tiempo disponible (9h)
- [ ] Setup TestContainers prerequisitos
- [ ] Instalar Playwright en frontend repo

---

## 🎯 DEFINICIÓN DE DONE

### Para cada tarea:
- [ ] Código implementado y funcionando
- [ ] Tests pasando (si aplica)
- [ ] Documentación actualizada
- [ ] Code review completado (self-review)
- [ ] Commit con mensaje descriptivo
- [ ] Tarjeta Trello movida a Done

### Para el Sprint:
- [ ] Todos los P0 tasks completados
- [ ] Coverage targets alcanzados
- [ ] CI pipeline operacional
- [ ] README actualizado con badges
- [ ] Sprint retrospective documentada
- [ ] Sprint 4 planeado

---

**Plan creado**: 2025-10-26
**Autor**: Bernard Uriza Orozco
**Basado en**: Tarjetas existentes en Trello
**Velocity base**: 0.55 (Sprint 2 average)
**Próximo inicio**: 2025-10-28
