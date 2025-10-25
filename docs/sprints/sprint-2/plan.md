# 🚀 SPRINT 2: Performance & Optimization

**Proyecto**: Portfolio Backend
**Sprint**: Performance & Optimization
**Duración**: 3 días × 3h/día = **9h disponibles**
**Velocity Factor**: 0.70 (basado en Sprint 1)
**Capacidad real**: ~13h estimadas de trabajo
**Fecha inicio**: 2025-10-25
**Fecha fin**: 2025-10-27

---

## 📊 Análisis de Velocidad (Sprint 1)

Sprint anterior completó Production Hardening con **velocity 0.72**:
- 9h estimadas → 6.5h reales
- 4 fases completadas (100%)
- 0 red flags
- Adherencia: 100%

**Factor conservador para Sprint 2**: **0.70**

---

## 🎯 Capacidad del Sprint

```
Tiempo disponible:    3 días × 3h/día = 9h
Con velocity 0.70:    9h / 0.70 ≈ 13h estimadas
Buffer estratégico:   Apuntar a 11-12h para margen
```

**Estrategia**: Seleccionar 11-12h de trabajo estimado para garantizar completitud.

---

## 📋 Tasks Seleccionadas (Priorización)

### P0 - Críticas (Must Have)

**PERF-001: Database Query Optimization**
- Estimado: 3h
- Audit de queries actuales
- Añadir índices faltantes
- Query profiling con EXPLAIN ANALYZE
- Optimizar N+1 queries
- **Output**: Documento de optimizaciones + índices añadidos

**PERF-002: Cache Strategy Refinement**
- Estimado: 2.5h
- Audit de estrategia actual (Caffeine)
- Configurar cache para endpoints críticos
- Implementar cache eviction policies
- Add cache hit/miss metrics
- **Output**: Cache configurado + métricas visibles

**PERF-003: Performance Baseline Testing**
- Estimado: 2h
- Setup JMeter/Gatling
- Crear test scenarios (CRUD operations)
- Ejecutar baseline tests
- Documentar resultados
- **Output**: Performance baseline report

### P1 - Importantes (Should Have)

**PERF-004: Performance Metrics Dashboard**
- Estimado: 2.5h
- Exponer métricas via Actuator
- Configurar Micrometer para response times
- Add custom metrics (cache hits, query times)
- Create simple metrics endpoint
- **Output**: /actuator/metrics con métricas custom

**PERF-005: Query Logging & Monitoring**
- Estimado: 1.5h
- Enable query logging en producción
- Log slow queries (>100ms threshold)
- Add query execution time to MDC
- **Output**: Query monitoring configurado

### P2 - Nice to Have (Could Have)

**PERF-006: Connection Pool Tuning**
- Estimado: 1h
- Review HikariCP configuration
- Tune pool size based on load tests
- Add connection pool metrics
- **Output**: Optimized HikariCP config

---

## 📊 Total Estimado

```
P0 Tasks:  3h + 2.5h + 2h = 7.5h
P1 Tasks:  2.5h + 1.5h = 4h
P2 Tasks:  1h
─────────────────────────────
TOTAL:     12.5h estimadas
```

Con velocity 0.70: **12.5h × 0.70 = 8.75h reales** (dentro de 9h disponibles) ✅

**Compromiso mínimo**: Completar **P0 tasks** (7.5h estimadas = ~5.25h reales)

---

## 🗓️ Roadmap Día a Día

### Día 1 (2025-10-25) - 3h
- **09:00-10:30**: PERF-001 (Database query optimization - parte 1)
  - Audit queries actuales
  - Identificar N+1 problems
- **10:30-12:00**: PERF-001 (continuación)
  - Añadir índices
  - Run EXPLAIN ANALYZE
- **Checkpoint**: 1 task avanzada (50%), queries identificadas
- **Commit esperado**: "perf: Add database indexes for query optimization"

### Día 2 (2025-10-26) - 3h
- **09:00-10:00**: PERF-001 (finalizar)
  - Documentar optimizaciones
  - Test performance improvement
- **10:00-11:30**: PERF-002 (Cache strategy refinement)
  - Audit cache actual
  - Configurar cache para endpoints críticos
- **11:30-12:00**: PERF-003 (Load testing - setup)
  - Install JMeter/Gatling
  - Create test scenarios
- **Checkpoint**: PERF-001 completa, PERF-002 50%, PERF-003 setup
- **Commits esperados**:
  - "perf: Optimize database queries and add indexes"
  - "perf: Add cache configuration for critical endpoints"

### Día 3 (2025-10-27) - 3h
- **09:00-10:00**: PERF-003 (Load testing - ejecución)
  - Run baseline tests
  - Documentar resultados
- **10:00-11:30**: PERF-004 (Performance metrics dashboard)
  - Exponer métricas custom
  - Add response time tracking
- **11:30-12:00**: PERF-005 (Query logging)
  - Enable slow query logging
  - Add query time to MDC
- **Checkpoint**: P0 tasks 100%, P1 tasks completadas
- **Commit esperado**: "perf: Add performance metrics and monitoring"

---

## 🔥 SECCIÓN DE ACCOUNTABILITY

### Contrato Inquebrantable

**YO, BERNARD URIZA OROZCO, ME COMPROMETO A**:

1. ✅ Trabajar **3 horas diarias** durante **3 días consecutivos** (2025-10-25 a 2025-10-27)
2. ✅ Completar **TODAS las tareas P0** (PERF-001, PERF-002, PERF-003)
3. ✅ Realizar **mínimo 1 commit por día** con progreso real
4. ✅ Actualizar **SPRINT_2_TRACKER.md** al inicio y fin de cada sesión
5. ✅ Actualizar **Trello card** con comentarios de progreso

### Reglas de Disciplina

**Horario**:
- 🕐 **3 horas diarias** sin excepción
- 🕐 **Inicio recomendado**: 09:00-12:00 (máxima energía)
- 🕐 **Permitido**: 2 bloques de 1.5h con 15min break

**Tracking obligatorio**:
- ✅ Git commits diarios
- ✅ SPRINT_2_TRACKER.md actualizado (inicio/fin sesión)
- ✅ Trello comments (progreso del día)
- ✅ TodoWrite tool (tasks in progress)

**Red Flags** 🚩:
- 🚨 **1 día sin commit** → Alerta amarilla (revisar bloqueo)
- 🚨 **1 día sin 3h de trabajo** → Alerta roja (replantear compromiso)
- 🚨 **2 días sin progreso** → Peligro crítico (sprint en riesgo)
- 🚨 **3 días sin actividad** → Sprint fallido (aplicar consecuencias)

### Bloqueo de Excusas Comunes

| Excusa | Respuesta Brutal |
|--------|-----------------|
| "No tengo tiempo" | Sprint 1 hiciste 6.5h en 1 día. Ahora pides 3h/día × 3 días. Es MENOS trabajo. |
| "Estoy cansado" | 3h/día es MENOS que tu tiempo en redes sociales. Prioridad = acción. |
| "No sé cómo hacerlo" | Google, StackOverflow, Claude. Si no sabes, aprende. Si no aprendes, no es prioritario. |
| "Surgió algo urgente" | Si algo es más urgente que este sprint 3 días seguidos, este proyecto NO es importante para ti. |
| "Lo hago mañana" | Mañana dirás lo mismo. Red flag activado. |

### Estrategia Anti-Procrastinación

**Regla de los 2 minutos**:
- Si puedes empezar en <2min, EMPIEZA YA
- Abrir IDE + git pull = 2min → NO hay excusa

**Técnica Pomodoro Modificada**:
- 25 min obligatorios de trabajo
- 5 min break
- Después de 2 pomodoros (1h), evaluar progreso
- Mínimo 6 pomodoros/día (3h)

**Micro-wins**:
- Añadir 1 índice > no añadir ninguno
- 1 test de carga > 0 tests
- Hacer ALGO > hacer nada

**Ritual de Inicio** (2 minutos):
1. Abrir SPRINT_2_TRACKER.md
2. Escribir hora de inicio
3. Leer task del día
4. Ejecutar `git pull`
5. Abrir IDE
6. **EMPEZAR** (no pensar, hacer)

---

## ⚠️ CONSECUENCIAS DE INCUMPLIMIENTO

### Si NO completas las tareas P0 del sprint:

**1. Reconocimiento Brutal**:
- Portfolio backend NO es prioritario en tu vida
- Performance optimization es un "nice to have", no un "must have"
- Estás haciendo **ingeniería cosmética**, no ingeniería real
- El proyecto es **entretenimiento intelectual**, no un producto serio

**2. Acción Correctiva**:
- **OPCIÓN A**: Cerrar el tema de performance indefinidamente
  - Aceptar que el backend "funciona" y performance no importa
  - Documentar en README: "Performance optimization: DEFERRED (not prioritized)"
  - Pasar al siguiente proyecto más importante

- **OPCIÓN B**: Replantear con compromiso REAL
  - Hacer sprint de 1 día × 8h (como Sprint 1)
  - Reducir scope a SOLO P0 tasks
  - O aumentar a 5h/día × 2 días

- **NO EXISTE OPCIÓN C** (continuar sin cambios)

**3. Deuda Técnica Emocional**:
- Documentar en bitácora el "por qué" fallaste
- ¿Falta de skill? → Aprender o delegar
- ¿Falta de tiempo? → Revisar prioridades de vida
- ¿Falta de disciplina? → Reconocer que proyectos personales no son para ti AHORA
- ¿Falta de interés? → Cerrar el proyecto y hacer algo que SÍ te importe

**4. Registro Permanente**:
- Este documento queda como evidencia
- Si fallan 2 sprints consecutivos → PATRÓN confirmado
- Patrón = No ejecutas proyectos personales → Acepta la realidad

---

## 📊 Métricas de Éxito

**Sprint considerado EXITOSO si**:
- ✅ PERF-001, PERF-002, PERF-003 completadas (P0)
- ✅ Mínimo 3 commits (1 por día)
- ✅ SPRINT_2_TRACKER.md actualizado diariamente
- ✅ Performance baseline documentado
- ✅ Índices de BD añadidos
- ✅ Cache configurado y medido

**Sprint considerado PARCIALMENTE EXITOSO si**:
- ⚠️ 2 de 3 tasks P0 completadas
- ⚠️ 2-3 commits
- ⚠️ Tracker parcialmente actualizado
- **Acción**: Sprint 3 debe ser más corto/enfocado

**Sprint considerado FALLIDO si**:
- ❌ <2 tasks P0 completadas
- ❌ <2 commits
- ❌ Tracker no actualizado
- **Acción**: Aplicar consecuencias de incumplimiento

---

## 📝 Outputs Esperados

Al final del sprint tendrás:

1. **Código**:
   - Índices de BD añadidos y documentados
   - Cache configurado para endpoints críticos
   - Performance baseline tests ejecutados

2. **Documentación**:
   - PERF_OPTIMIZATION_REPORT.md con:
     - Query optimizations realizadas
     - Cache hit/miss ratios
     - Load test results (baseline)
     - Recommendations para siguiente sprint

3. **Métricas**:
   - Response time before/after optimization
   - Query execution times
   - Cache effectiveness metrics
   - Load test results (requests/sec, latency p50/p95/p99)

4. **Commits**:
   - Mínimo 3 commits con mensajes descriptivos
   - Tags: `perf: ...` para todas las mejoras

5. **Trello**:
   - Card actualizada con 3 comentarios mínimo
   - Moved to Done si completado exitosamente

---

## 🎯 CONTRATO FORMAL

**YO ME COMPROMETO A**:

- [ ] 3 horas diarias × 3 días (total 9h)
- [ ] Completar tasks P0 (PERF-001, PERF-002, PERF-003)
- [ ] Mínimo 1 commit diario
- [ ] Actualizar SPRINT_2_TRACKER.md diariamente
- [ ] Si fallo 2 días consecutivos, aplicar consecuencias de incumplimiento

**Firma**: Bernard Uriza Orozco
**Fecha**: 2025-10-25
**Testigo**: Claude (Sonnet 4.5)

**Este documento es evidencia**. No existe "lo intenté". Solo existe "lo hice" o "no lo hice".

---

## 🔥 Motivación Final (Anti-Motivación)

No te voy a motivar. No te voy a decir "tú puedes".

Te voy a decir la verdad:

- Si completas este sprint, demostrarás que puedes ejecutar proyectos personales.
- Si NO lo completas, demostrarás que NO puedes (ahora).
- Ambas respuestas son válidas. Pero necesitas SABER cuál es.

**3 días × 3 horas = 9 horas de tu vida**.

Si no puedes dedicar 9 horas en 3 días a mejorar un proyecto que dices que es importante, entonces **el proyecto NO es importante para ti**. Y eso está bien. Pero reconócelo y ciérralo.

O ejecuta. Sin excusas.

**Tu elección.**
