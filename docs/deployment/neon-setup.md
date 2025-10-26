# Configuración de PostgreSQL con Neon (Gratuito)

## ¿Por qué Neon?

Render eliminó su plan gratuito de PostgreSQL. Neon ofrece:
- ✅ **3 GB de almacenamiento** gratis para siempre
- ✅ Conexiones ilimitadas
- ✅ Autosuspensión después de 5 min (se despierta automáticamente)
- ✅ Branching de base de datos
- ✅ Compatible con todas las aplicaciones PostgreSQL

---

## Paso 1: Crear cuenta en Neon

1. Ve a https://console.neon.tech
2. Haz clic en **Sign Up**
3. Usa tu cuenta de GitHub para registro rápido (1 clic)

---

## Paso 2: Crear proyecto de base de datos

1. Una vez dentro del dashboard, haz clic en **Create Project**
2. Configura:
   - **Project Name**: `portfolio`
   - **Region**: `US East (Ohio)` o el más cercano a tu región de Render
   - **PostgreSQL Version**: `16` (última estable)
3. Haz clic en **Create Project**

---

## Paso 3: Obtener el Connection String

Después de crear el proyecto:

1. En el dashboard, ve a la sección **Connection Details**
2. Selecciona la pestaña **Pooled connection**
3. Copia el **Connection string** completo

Debería verse así:
```
postgres://username:password@ep-cool-name-123456.us-east-2.aws.neon.tech/neondb?sslmode=require
```

---

## Paso 4: Configurar variables de entorno en Render

### Opción A: Dashboard Web (Recomendado)

1. Ve a https://dashboard.render.com
2. Selecciona tu servicio **portfolio-backend**
3. Ve a **Environment**
4. Agrega o actualiza la variable:
   - **Key**: `DATABASE_URL`
   - **Value**: (pega el connection string de Neon)
5. Haz clic en **Save Changes**

### Opción B: CLI

```bash
render env set DATABASE_URL="postgres://user:pass@ep-xxx.us-east-2.aws.neon.tech/neondb?sslmode=require" --service portfolio-backend
```

---

## Paso 5: Agregar otras variables de entorno requeridas

En el mismo panel de **Environment** de Render, agrega:

```
GITHUB_USERNAME=TuUsuarioDeGitHub
GITHUB_TOKEN=ghp_tu_token_de_github
ANTHROPIC_API_KEY=sk-ant-tu-api-key-de-claude
PORTFOLIO_ADMIN_TOKEN=un-token-seguro-aleatorio-123456
```

---

## Paso 6: Deploy

### Hacer commit de los cambios:

```bash
cd /Users/bernardurizaorozco/Documents/portfolio-spring
git add render.yaml
git commit -m "feat(deploy): Configure external PostgreSQL with Neon"
git push origin main
```

### Deploy automático:

Render detectará el push a `main` y hará deploy automáticamente.

O fuerza un deploy manual:
```bash
render deploy --service portfolio-backend
```

---

## Paso 7: Ejecutar migraciones de Flyway

Tu aplicación ejecutará automáticamente las migraciones de Flyway al iniciar.

Verifica en los logs de Render:
```bash
render services logs portfolio-backend --tail 100
```

Deberías ver:
```
✅ Flyway migration completed successfully
✅ Started PortfolioSpringApplication
```

---

## Verificación

### Health Check:
```bash
curl https://portfolio-backend.onrender.com/actuator/health
```

Respuesta esperada:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    }
  }
}
```

---

## Troubleshooting

### Error: "Connection timeout"

**Causa**: Neon suspende la DB después de 5 minutos de inactividad.

**Solución**: La primera request después de la suspensión puede tardar 1-2 segundos mientras se despierta. Es normal.

### Error: "SSL connection required"

**Causa**: Falta el parámetro `sslmode=require` en el DATABASE_URL.

**Solución**: Asegúrate de que tu connection string incluya `?sslmode=require` al final.

### Error: "Too many connections"

**Causa**: El plan gratuito de Neon soporta conexiones ilimitadas, pero con pooling.

**Solución**: En `application-render.properties`, verifica:
```properties
spring.datasource.hikari.maximum-pool-size=5
spring.datasource.hikari.minimum-idle=2
```

---

## Monitoreo en Neon

1. Dashboard de Neon → **Monitoring**
2. Puedes ver:
   - Conexiones activas
   - Queries ejecutados
   - Uso de almacenamiento
   - CPU y memoria

---

## Branching (Opcional - Muy útil)

Neon permite crear "ramas" de tu base de datos para testing:

```bash
# 1. En Neon dashboard, crea un branch "staging"
# 2. Obtienes un nuevo connection string
# 3. Úsalo en un servicio de Render separado para staging
```

Esto te permite tener:
- **main branch** → Production
- **staging branch** → Testing
- **dev branch** → Desarrollo local

---

## Costos

**Plan Free de Neon (Forever Free):**
- ✅ 3 GB de almacenamiento
- ✅ 1 proyecto
- ✅ 10 branches
- ✅ Autosuspensión después de 5 min de inactividad
- ✅ Point-in-time recovery (7 días)

**Si necesitas más:**
- Plan Launch: $19/mes (sin autosuspensión, 10 GB)
- Plan Scale: $69/mes (100 GB, soporte prioritario)

Pero para tu portfolio, el **plan gratuito es más que suficiente**.

---

## Alternativas si Neon no funciona

### 1. Supabase
```
URL: https://supabase.com
Plan Free: 500 MB
Pros: Incluye Auth + Storage
```

### 2. Railway
```
URL: https://railway.app
Plan Free: $5 de crédito mensual
Pros: Muy fácil integración
Cons: Requiere tarjeta de crédito
```

### 3. Aiven
```
URL: https://aiven.io
Plan Free: 1 GB
Pros: Buen rendimiento
Cons: Requiere tarjeta de crédito
```

---

## Resumen de Comandos

```bash
# 1. Commit cambios
git add render.yaml
git commit -m "feat(deploy): Configure external PostgreSQL with Neon"
git push origin main

# 2. Configurar DATABASE_URL en Render (Dashboard o CLI)
render env set DATABASE_URL="postgres://..." --service portfolio-backend

# 3. Deploy
render deploy --service portfolio-backend

# 4. Ver logs
render services logs portfolio-backend --tail 100

# 5. Health check
curl https://portfolio-backend.onrender.com/actuator/health
```

---

## Próximos pasos

- [ ] Crear cuenta en Neon
- [ ] Obtener connection string
- [ ] Configurar DATABASE_URL en Render
- [ ] Hacer commit y deploy
- [ ] Verificar health check
- [ ] Configurar otras API keys (GitHub, Anthropic)
- [ ] Probar endpoints principales

---

**Creado por**: Bernard Uriza Orozco
**Fecha**: 2025-01-25
**Documentación de Neon**: https://neon.tech/docs
