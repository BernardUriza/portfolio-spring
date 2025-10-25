# 🚀 Portfolio Workspace Guide

## 📋 Overview

This is a professional full-stack development workspace for your Portfolio application, configured with industry best practices for Angular + Spring Boot development on macOS.

**Tech Stack:**
- **Frontend**: Angular 20 + TailwindCSS + TypeScript
- **Backend**: Spring Boot 3.5.0 + Java 25 + PostgreSQL
- **IDE**: VS Code with optimized extensions

---

## 🎯 Quick Start

### 1. Open the Workspace
```bash
# From the backend directory
code portfolio.code-workspace
```

### 2. Install Recommended Extensions
When you open the workspace, VS Code will prompt you to install recommended extensions. Click **"Install All"**.

### 3. Start Development

#### Option A: Use VS Code Tasks (Recommended)
- Press `Cmd+Shift+P` → "Run Task" → "Full Stack: Start All"

#### Option B: Use Debug Configurations
- Press `F5` or go to Debug panel → Select "🚀 Full Stack: Debug All"

#### Option C: Manual Terminal
```bash
# Terminal 1 - Backend
./mvnw spring-boot:run

# Terminal 2 - Frontend
cd ../portfolio-frontend
npm start
```

---

## 🛠️ Available Tasks

Access tasks via `Cmd+Shift+P` → "Run Task":

### 🅰️ Frontend Tasks
- **Frontend: Start Dev Server** - Start Angular dev server (port 4200)
- **Frontend: Build Production** - Build optimized production bundle
- **Frontend: Run Tests** - Execute Jasmine/Karma tests

### ☕ Backend Tasks
- **Backend: Build Maven** - Compile Java code with Maven
- **Backend: Start Spring Boot** - Run Spring Boot server (port 8080)
- **Backend: Run Tests** - Execute JUnit tests
- **Backend: Clean** - Clean Maven build artifacts
- **Backend: Package** - Create JAR file (skip tests)

### 🚀 Full Stack Tasks
- **Full Stack: Start All** - Start both backend and frontend
- **Full Stack: Build All** - Build both projects
- **Full Stack: Test All** - Run all tests

### 🛠️ Utility Tasks
- **Utility: Kill Ports (4200, 8080)** - Free up development ports

---

## 🐛 Debug Configurations

Available in the Debug panel (Cmd+Shift+D):

### Single Project Debugging
- **🅰️ Angular: Chrome** - Debug Angular app in Chrome
- **🅰️ Angular: Edge** - Debug Angular app in Edge
- **☕ Spring Boot: Debug** - Debug Spring Boot with build
- **☕ Spring Boot: Debug (No Build)** - Debug without rebuilding

### Full Stack Debugging
- **🚀 Full Stack: Debug All** - Debug everything at once
- **🎯 Full Stack Debug (Compound)** - Compound debugger for both projects

**Breakpoints**: Set breakpoints in `.java` or `.ts` files, then press `F5`

---

## 📦 Recommended Extensions

### Essential (Auto-installed)
✅ **Angular Language Service** - IntelliSense for Angular templates
✅ **Java Extension Pack** - Complete Java development tools
✅ **Spring Boot Tools** - Spring Boot development support
✅ **TailwindCSS IntelliSense** - Autocomplete for Tailwind classes
✅ **ESLint** - TypeScript/JavaScript linting
✅ **Prettier** - Code formatting
✅ **GitLens** - Enhanced Git capabilities

### Productivity Boosters
📝 **TODO Tree** - Track TODOs across codebase
🎨 **Better Comments** - Colorized code comments
🔄 **Auto Rename Tag** - Rename HTML/XML tags in pairs
🗂️ **Path Intellisense** - Autocomplete file paths

### Database & API
🐘 **PostgreSQL Client** - Browse and query PostgreSQL
📡 **REST Client** - Test APIs from within VS Code

---

## ⚙️ Workspace Settings

### Code Formatting
- **Auto-format on save**: Enabled for all files
- **Auto-organize imports**: On save
- **Trailing whitespace**: Automatically trimmed
- **Final newline**: Automatically added

### Language Specific
| Language   | Tab Size | Formatter                  |
|------------|----------|----------------------------|
| TypeScript | 2 spaces | VS Code TypeScript         |
| JavaScript | 2 spaces | VS Code JavaScript         |
| HTML       | 2 spaces | VS Code HTML               |
| SCSS/CSS   | 2 spaces | VS Code CSS                |
| Java       | 4 spaces | Red Hat Java               |
| JSON       | 2 spaces | VS Code JSON               |

### File Nesting
Related files are grouped together in the Explorer:
- `*.component.ts` groups with `.html`, `.scss`, and `.spec.ts`
- `*.service.ts` groups with `.spec.ts`
- `package.json` groups with `package-lock.json`
- `pom.xml` groups with Maven wrapper files

---

## 🎨 Code Quality Features

### Null Safety (Java)
- Automatic null analysis enabled
- Catches potential NPEs at compile time

### Import Organization (Java)
Imports are organized in this order:
1. `java.*`
2. `javax.*` / `jakarta.*`
3. `org.*`
4. `com.*`

### Hot Code Replace (Java)
- Changes are applied without restarting the debugger
- Enabled by default for faster development

### TypeScript Auto-Imports
- Automatically adds missing imports
- Updates import paths when files move

---

## 📁 Project Structure

```
portfolio-workspace/
├── 🎯 Backend (Spring Boot)/
│   ├── src/main/java/com/portfolio/
│   │   ├── controller/      # REST endpoints
│   │   ├── service/         # Business logic
│   │   ├── model/           # JPA entities
│   │   ├── repository/      # Data access
│   │   ├── dto/             # Data transfer objects
│   │   └── config/          # Spring configurations
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
│
└── ⚡ Frontend (Angular)/
    ├── src/app/
    │   ├── components/      # Reusable UI components
    │   ├── features/        # Feature modules
    │   ├── core/            # Services, guards, interceptors
    │   └── app.routes.ts    # Routing configuration
    ├── src/styles.scss      # Global styles
    ├── angular.json         # Angular configuration
    ├── tailwind.config.js   # TailwindCSS config
    └── package.json
```

---

## 🔍 Terminal Configuration

### Optimized for macOS
- **Default shell**: Zsh
- **Font**: MesloLGS NF (Nerd Font for icons)
- **Scrollback**: 10,000 lines
- **Size**: 13pt

### Recommended Terminal Setup
```bash
# Install Oh My Zsh (if not already installed)
sh -c "$(curl -fsSL https://raw.githubusercontent.com/ohmyzsh/ohmyzsh/master/tools/install.sh)"

# Install Powerlevel10k theme
git clone --depth=1 https://github.com/romkatv/powerlevel10k.git ${ZSH_CUSTOM:-$HOME/.oh-my-zsh/custom}/themes/powerlevel10k
```

---

## 🚦 Common Workflows

### Daily Development
1. Open workspace: `code portfolio.code-workspace`
2. Start full stack: `Cmd+Shift+P` → "Run Task" → "Full Stack: Start All"
3. Open browser: http://localhost:4200
4. Code with hot reload enabled

### Debugging Issues
1. Set breakpoints in code
2. `F5` → Select debug configuration
3. Trigger the problematic flow
4. Inspect variables in Debug panel

### Testing Before Commit
```bash
# Run all tests
Cmd+Shift+P → "Run Task" → "Full Stack: Test All"

# Or manually:
# Backend tests
./mvnw test

# Frontend tests
cd ../portfolio-frontend
npm test
```

### Building for Production
```bash
# Option 1: Use task
Cmd+Shift+P → "Run Task" → "Full Stack: Build All"

# Option 2: Manual
./mvnw clean package
cd ../portfolio-frontend
npm run build
```

---

## 🐛 Troubleshooting

### Port Already in Use
```bash
# Kill ports 4200 and 8080
Cmd+Shift+P → "Run Task" → "Utility: Kill Ports (4200, 8080)"

# Or manually:
lsof -ti:8080 | xargs kill -9
lsof -ti:4200 | xargs kill -9
```

### Java Extensions Not Working
1. `Cmd+Shift+P` → "Java: Clean Java Language Server Workspace"
2. Reload VS Code
3. Wait for Java extension to re-index

### TypeScript Errors in Angular Templates
1. `Cmd+Shift+P` → "Angular: Restart Angular Language Server"
2. Check that `angular.ng-template` extension is installed

### Maven Build Fails
```bash
# Clean and rebuild
./mvnw clean install -DskipTests

# If still failing, check Java version
java -version  # Should be Java 25
```

### PostgreSQL Connection Issues
1. Check if PostgreSQL is running: `pg_isready`
2. Verify credentials in `application.properties`
3. Create database if needed: `createdb portfolio_db`

---

## 📚 Best Practices

### Git Workflow
1. Create feature branch: `git checkout -b feature/my-feature`
2. Make changes and commit frequently
3. Test before pushing: Run "Full Stack: Test All"
4. Push and create PR

### Code Style
- **Java**: Follow Spring Boot conventions, use Lombok for boilerplate
- **TypeScript**: Use standalone components, leverage signals (Angular 20)
- **CSS**: Prefer TailwindCSS utilities over custom CSS
- **Comments**: Use Better Comments syntax (`// TODO:`, `// FIXME:`, `// !`)

### Performance Tips
- Use Angular's built-in change detection wisely
- Lazy load Angular feature modules
- Use Spring Boot's `@Async` for long-running operations
- Implement proper caching strategies

---

## 🎓 Learning Resources

### Angular 20
- [Angular Docs](https://angular.dev)
- [Angular DevTools](https://angular.dev/tools/devtools)

### Spring Boot 3
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/)
- [Spring Guides](https://spring.io/guides)

### TailwindCSS
- [Tailwind Docs](https://tailwindcss.com/docs)
- [Tailwind UI Components](https://tailwindui.com/)

---

## 🙋‍♂️ Getting Help

### Keyboard Shortcuts
- `Cmd+Shift+P`: Command Palette
- `Cmd+P`: Quick Open File
- `Cmd+Shift+F`: Search in files
- `F5`: Start debugging
- `Shift+F5`: Stop debugging
- `Cmd+K Cmd+S`: Keyboard shortcuts reference

### VS Code Tips
- Hover over settings in workspace file to see descriptions
- Use `Cmd+Click` on file paths to navigate
- Multi-cursor editing: `Cmd+Option+↑/↓`
- Rename symbol: `F2`

---

**Made with ❤️ by Bernard Uriza**
*Last updated: 2025-10-22*
