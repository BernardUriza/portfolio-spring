# 🎯 Cómo Iniciar el Portfolio - Guía Rápida

## ⚡ TL;DR - Un Solo Comando

```bash
./dev.sh
```

**Eso es TODO lo que necesitas.** El script:
1. ✅ Verifica PostgreSQL
2. ✅ Crea la base de datos
3. ✅ Inicia Spring Boot (8080)
4. ✅ Inicia Angular (4200)
5. ✅ Te avisa cuando todo está listo

---

## 🎨 Todas tus Opciones

Tienes **3 formas** de iniciar el proyecto. Elige la que prefieras:

### **Opción 1: Script de Terminal** 💻
```bash
./dev.sh                # Inicia todo
./dev.sh --backend-only # Solo backend
./dev.sh --frontend-only # Solo frontend
./dev.sh --clean        # Limpia puertos
./stop.sh               # Detiene todo
```
✅ **Mejor para:** Desarrollo rápido, testing de API, trabajo en terminal

📖 **Documentación completa:** `DEV_SCRIPTS.md`

---

### **Opción 2: VS Code Tasks** 🚀
```
Cmd+Shift+P → "Run Task"

Opciones:
  🚀 Full Stack: Start All
  ☕ Backend: Start Spring Boot
  ⚡ Frontend: Start Dev Server
  🧪 Full Stack: Test All
  📦 Full Stack: Build All
```
✅ **Mejor para:** Trabajo en VS Code, visual feedback, múltiples proyectos

📖 **Documentación completa:** `WORKSPACE_GUIDE.md`

---

### **Opción 3: VS Code Debugging** 🐛
```
F5 → Selecciona configuración

Opciones:
  🚀 Full Stack: Debug All
  🎯 Full Stack Debug (Compound)
  ☕ Spring Boot: Debug
  🅰️ Angular: Chrome
```
✅ **Mejor para:** Debugging, breakpoints, inspección de variables

📖 **Documentación completa:** `WORKSPACE_GUIDE.md` (sección Launch)

---

## 🆚 Comparación Rápida

| Método | Tiempo Inicio | Debugging | Visual Feedback | Mejor Para |
|--------|---------------|-----------|-----------------|------------|
| `./dev.sh` | ⚡ Rápido | ❌ No | ✅ Terminal | Desarrollo diario |
| VS Code Tasks | ⚡ Rápido | ❌ No | ✅ VS Code UI | Workflow integrado |
| VS Code Debug | 🐌 Lento | ✅ Sí | ✅ VS Code UI | Debugging complejo |

---

## 📚 Documentación Completa

| Archivo | Qué Cubre |
|---------|-----------|
| `DEV_SCRIPTS.md` | Scripts de terminal (`./dev.sh`, `./stop.sh`) |
| `WORKSPACE_GUIDE.md` | VS Code workspace, tasks, debugging, extensiones |
| `api-tests.http` | Suite de pruebas de API (REST Client) |
| `CLAUDE.md` | Arquitectura del backend (Spring Boot) |
| `../portfolio-frontend/CLAUDE.md` | Arquitectura del frontend (Angular) |

---

## 🚀 Quick Start para Nuevos Desarrolladores

### Primera Vez
```bash
# 1. Clonar repos (si aún no lo has hecho)
git clone <repo-backend>
git clone <repo-frontend>

# 2. Instalar dependencias frontend
cd portfolio-frontend
npm install

# 3. Configurar PostgreSQL
brew services start postgresql
createdb portfolio_db

# 4. Crear archivo .env en backend
cd ../portfolio-spring
cp .env.example .env
# Edita .env con tus tokens

# 5. Iniciar todo
./dev.sh
```

### Desarrollo Diario
```bash
cd ~/Documents/portfolio-spring
./dev.sh

# Trabajo, trabajo, trabajo...

# Ctrl+C para detener
```

---

## 🎯 URLs Importantes

Después de `./dev.sh`:

| Servicio | URL | Descripción |
|----------|-----|-------------|
| 🌐 **Frontend** | http://localhost:4200 | Aplicación Angular |
| 🔌 **Backend** | http://localhost:8080 | API Spring Boot |
| ❤️ **Health Check** | http://localhost:8080/api/health | Status del backend |
| 📊 **Actuator** | http://localhost:8080/actuator/health | Spring Boot actuator |
| 📁 **API Projects** | http://localhost:8080/api/sync/projects | Portfolio projects |

---

## 🛠️ Troubleshooting Rápido

### "PostgreSQL is not running"
```bash
brew services start postgresql
./dev.sh
```

### "Port already in use"
```bash
./dev.sh --clean
./dev.sh
```

### "npm command not found"
```bash
brew install node
cd ../portfolio-frontend
npm install
cd ../portfolio-spring
./dev.sh
```

### Ver documentación completa
```bash
# Para scripts de terminal
cat DEV_SCRIPTS.md

# Para VS Code
cat WORKSPACE_GUIDE.md
```

---

## 🎓 Próximos Pasos

1. ✅ **Elige tu método favorito** (Script / VS Code Task / Debugging)
2. ✅ **Inicia el proyecto** con el método elegido
3. ✅ **Abre http://localhost:4200** en tu navegador
4. ✅ **Lee la documentación completa** según tu método
5. ✅ **Explora los endpoints** con `api-tests.http`

---

## 💡 Tips Pro

### Alias de Shell (Opcional)
```bash
# Agrega a ~/.zshrc
alias pf-start='cd ~/Documents/portfolio-spring && ./dev.sh'
alias pf-stop='cd ~/Documents/portfolio-spring && ./stop.sh'
alias pf-clean='cd ~/Documents/portfolio-spring && ./dev.sh --clean'

# Uso
pf-start  # Desde cualquier lugar!
```

### VS Code Workspace (Recomendado)
```bash
# Abre el workspace multi-proyecto
code portfolio.code-workspace

# Ahora tienes ambos proyectos en un solo VS Code
# Usa Cmd+Shift+P para acceder a todas las tareas
```

---

**¿Preguntas?** Lee la documentación completa:
- 💻 Scripts: `DEV_SCRIPTS.md`
- 🚀 VS Code: `WORKSPACE_GUIDE.md`
- 🔌 APIs: Abre `api-tests.http` en VS Code

**Happy Coding! 🎉**
