# 🚀 Development Scripts Guide

## Quick Start - Un Solo Comando ⚡

```bash
# Inicia AMBOS proyectos (Backend + Frontend)
./dev.sh
```

¡Eso es todo! El script hace todo automáticamente:
- ✅ Verifica PostgreSQL
- ✅ Crea la base de datos si no existe
- ✅ Inicia Spring Boot (puerto 8080)
- ✅ Inicia Angular (puerto 4200)
- ✅ Espera a que ambos estén listos
- ✅ Te muestra URLs y status

---

## 📋 Opciones Disponibles

### 1. Iniciar Todo (Opción por Defecto)
```bash
./dev.sh
```
**Output:**
```
🚀 Portfolio Full Stack Development Server
──────────────────────────────────────────
🐘 Checking PostgreSQL...         ✅
🗄️  Checking database...           ✅
☕ Starting Spring Boot Backend... ✅
⚡ Starting Angular Frontend...    ✅
⏳ Waiting for services...

🎉 ALL SYSTEMS GO! 🎉
🌐 Frontend: http://localhost:4200
🔌 Backend:  http://localhost:8080
❤️  Health:   http://localhost:8080/api/health
```

### 2. Solo Backend
```bash
./dev.sh --backend-only
```
Útil cuando solo necesitas trabajar en el backend o hacer testing de APIs.

### 3. Solo Frontend
```bash
./dev.sh --frontend-only
```
Útil cuando el backend ya está corriendo en otro lugar o trabajas solo en UI.

### 4. Limpiar Puertos
```bash
./dev.sh --clean
```
Mata todos los procesos en los puertos 4200 y 8080. Útil si algo se quedó colgado.

### 5. Ver Ayuda
```bash
./dev.sh --help
```

---

## 🛑 Detener Todo

```bash
# Opción 1: Usar el script de stop
./stop.sh

# Opción 2: Ctrl+C en la terminal donde corre dev.sh

# Opción 3: Limpiar manualmente
./dev.sh --clean
```

---

## 🎯 Casos de Uso

### Desarrollo Diario
```bash
# Morning routine
cd ~/Documents/portfolio-spring
./dev.sh

# Work on your features...

# Ctrl+C cuando termines
```

### Testing de API
```bash
# Inicia solo backend
./dev.sh --backend-only

# En otra terminal, usa curl o el archivo api-tests.http
curl http://localhost:8080/api/health
```

### Desarrollo Solo de UI
```bash
# Si el backend ya está en producción o en otro servidor
./dev.sh --frontend-only
```

### Debugging de Puertos
```bash
# Si ves errores de "port already in use"
./dev.sh --clean

# Luego reinicia
./dev.sh
```

---

## ⚙️ Pre-requisitos

El script verifica automáticamente, pero asegúrate de tener:

### 1. PostgreSQL Instalado y Corriendo
```bash
# Verificar si está instalado
which psql

# Verificar si está corriendo
pg_isready

# Si no está corriendo (macOS con Homebrew):
brew services start postgresql@14

# O la versión que tengas instalada
brew services start postgresql
```

### 2. Node.js y npm
```bash
# Verificar versión
node --version  # Debería ser v18 o superior
npm --version
```

### 3. Java 25
```bash
# Verificar versión
java -version  # Debería ser Java 25
```

### 4. Dependencias del Frontend
```bash
# El script lo hace automáticamente, pero puedes hacerlo manual:
cd ../portfolio-frontend
npm install
```

---

## 🔧 Troubleshooting

### Problema: "PostgreSQL is not running"
```bash
# Solución 1: Iniciar PostgreSQL
brew services start postgresql

# Solución 2: Verificar si está en otro puerto
pg_isready -h localhost -p 5432

# Solución 3: Reinstalar PostgreSQL
brew reinstall postgresql
```

### Problema: "Port 8080 already in use"
```bash
# Ver qué proceso está usando el puerto
lsof -i :8080

# Matar el proceso
./dev.sh --clean

# O manualmente
lsof -ti:8080 | xargs kill -9
```

### Problema: "Database does not exist"
```bash
# El script lo crea automáticamente, pero puedes hacerlo manual:
createdb portfolio_db

# O con psql
psql -c "CREATE DATABASE portfolio_db;"
```

### Problema: "npm command not found"
```bash
# Instalar Node.js con Homebrew
brew install node

# O descargarlo de https://nodejs.org
```

### Problema: Frontend no inicia
```bash
# Verificar node_modules
cd ../portfolio-frontend
ls -la node_modules

# Si no existe, instalar
npm install

# Limpiar cache si hay problemas
npm cache clean --force
npm install
```

---

## 🎨 Personalización

### Cambiar Puertos

Edita los archivos de configuración:

**Backend (Spring Boot):**
```properties
# src/main/resources/application.properties
server.port=8080  # Cambia a otro puerto
```

**Frontend (Angular):**
```json
// angular.json o ejecuta:
ng serve --port 4201
```

Luego actualiza el script `dev.sh`:
```bash
# Cambia las líneas con 8080 y 4200 a tus nuevos puertos
```

### Agregar Variables de Entorno

Edita `dev.sh` y agrega antes de iniciar el backend:
```bash
export GITHUB_TOKEN="tu-token-aquí"
export ANTHROPIC_API_KEY="tu-api-key"
```

O mejor aún, usa un archivo `.env`:
```bash
# En la raíz del backend
cp .env.example .env
# Edita .env con tus valores
```

---

## 📊 Comparación de Métodos

| Método | Ventajas | Desventajas | Recomendado Para |
|--------|----------|-------------|------------------|
| `./dev.sh` | ✅ Simple<br>✅ Automático<br>✅ Checks incluidos | ❌ No debugging | Desarrollo diario |
| VS Code Task | ✅ Integrado IDE<br>✅ Visual | ❌ Requiere VS Code | Trabajo en VS Code |
| VS Code Debug | ✅ Breakpoints<br>✅ Inspector | ❌ Más lento inicio | Debugging |
| Manual | ✅ Control total | ❌ Múltiples terminales | Testing específico |

---

## 🚀 Tips Pro

### 1. Alias de Shell (Opcional)
Agrega a tu `~/.zshrc` o `~/.bashrc`:
```bash
alias portfolio-start='cd ~/Documents/portfolio-spring && ./dev.sh'
alias portfolio-stop='cd ~/Documents/portfolio-spring && ./stop.sh'
alias portfolio-clean='cd ~/Documents/portfolio-spring && ./dev.sh --clean'
```

Luego:
```bash
source ~/.zshrc
portfolio-start  # ¡Desde cualquier lugar!
```

### 2. Tmux Session (Avanzado)
```bash
# Crear sesión tmux
tmux new -s portfolio

# Split horizontal
Ctrl+B %

# Panel izquierdo: Backend
./dev.sh --backend-only

# Panel derecho: Frontend (Ctrl+B + flecha derecha)
./dev.sh --frontend-only

# Detach: Ctrl+B D
# Reattach: tmux attach -t portfolio
```

### 3. Watch Mode con Logs
```bash
# Terminal 1
./dev.sh --backend-only

# Terminal 2
./dev.sh --frontend-only

# Terminal 3 - Monitorear logs del backend
tail -f target/spring-boot-devtools.log

# Terminal 4 - Monitorear health
watch -n 5 'curl -s http://localhost:8080/api/health | jq'
```

---

## 📝 Notas Importantes

1. **Ctrl+C limpia automáticamente** - El script tiene un trap para limpiar procesos al salir
2. **PostgreSQL es requerido** - Sin él, el backend no iniciará
3. **Primer inicio es más lento** - Spring Boot compila y Angular genera bundles
4. **Hot reload está habilitado** - Cambios se reflejan automáticamente
5. **Logs aparecen en la misma terminal** - Para logs separados, usa las opciones `--backend-only` y `--frontend-only`

---

## 🎓 Siguiente Paso

Después de iniciar con `./dev.sh`, abre tu navegador:

1. **Frontend**: http://localhost:4200
2. **Backend Health**: http://localhost:8080/api/health
3. **API Root**: http://localhost:8080/api

Para testing de API, usa:
- **VS Code REST Client**: Abre `api-tests.http` y presiona `Cmd+Alt+R`
- **curl**: `curl http://localhost:8080/api/health`
- **Postman**: Import la colección desde `api-tests.http`

---

**Happy Coding! 🎉**

*Creado por: Bernard Uriza*
*Última actualización: 2025-10-22*
