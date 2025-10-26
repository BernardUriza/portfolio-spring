# Portfolio Backend API

> Spring Boot backend for AI-powered portfolio management with GitHub integration and semantic analysis

[![CI Pipeline](https://github.com/BernardUriza/portfolio-spring/actions/workflows/ci.yml/badge.svg)](https://github.com/BernardUriza/portfolio-spring/actions/workflows/ci.yml)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Test Coverage](https://img.shields.io/badge/coverage-60%25-yellow.svg)](target/site/jacoco/index.html)

## ✨ Features

- 🔗 **GitHub API Integration** - Auto-sync starred repositories with configurable intervals
- 🤖 **AI-Powered Analysis** - Claude API integration for semantic project categorization
- 📊 **Performance Monitoring** - Real-time cache metrics and performance tracking
- 🗄️ **PostgreSQL Database** - Production-ready with Flyway migrations
- ⚡ **Cache Strategy** - Caffeine-based caching with automatic eviction
- 🎯 **RESTful API** - Comprehensive endpoints for portfolio management
- 🔒 **Admin Security** - Token-based authentication for admin operations
- 🏗️ **Hexagonal Architecture** - Clean separation of concerns with domain-driven design

## 🚀 Quick Start

### Prerequisites

- Java 25+
- PostgreSQL 14+
- Maven 3.9+
- Node.js 18+ (for frontend integration)

### One-Command Setup

```bash
./dev.sh
```

This script will:
1. ✅ Verify PostgreSQL is running
2. ✅ Create the database
3. ✅ Start Spring Boot (port 8080)
4. ✅ Start Angular frontend (port 4200)
5. ✅ Notify when ready

### Manual Setup

```bash
# 1. Create database
psql -U postgres -c "CREATE DATABASE portfolio_db;"

# 2. Configure environment variables
cp .env.example .env
# Edit .env with your credentials

# 3. Start the application
./mvnw spring-boot:run
```

### Environment Variables

Required variables (see `.env.example`):

```properties
GITHUB_USERNAME=your-github-username
GITHUB_TOKEN=your-github-personal-access-token
ANTHROPIC_API_KEY=your-claude-api-key
ADMIN_RESET_TOKEN=your-secure-admin-token
```

## 📖 Documentation

### Getting Started
- [Quick Start Guide](docs/getting-started/quick-start.md)
- [Workspace Setup](docs/getting-started/workspace-setup.md)
- [Development Scripts](docs/getting-started/dev-scripts.md)

### Architecture
- [Architecture Overview](docs/architecture/overview.md)
- [Hexagonal Design](docs/architecture/hexagonal-design.md)
- [Database Schema](docs/architecture/database-schema.md)

### Deployment
- [Local Development](docs/deployment/local-development.md)
- [PostgreSQL Migration](docs/deployment/postgresql-migration.md)
- [Production Deployment](docs/deployment/production.md)
- [Render Setup](docs/deployment/render-setup.md)

### API Documentation
- [API Endpoints](docs/api/endpoints.md)
- [Authentication](docs/api/authentication.md)

### Guides
- [AI Agent Integration](docs/guides/ai-agent-integration.md)
- [Trello Workflow](docs/guides/trello-workflow.md)

### Reports & Analysis
- [Analysis Report](docs/reports/analysis-report.md)
- [Quick Reference](docs/reports/analysis-quick-reference.md)

## 🧪 Running Tests

```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report

# Run Sprint 2 smoke tests
./scripts/sprint2_smoke_tests.sh
```

## 📊 API Endpoints

### Health & Monitoring
- `GET /api/health` - Health check
- `GET /api/monitoring/status` - System status
- `GET /api/monitoring/cache/stats` - Cache metrics

### Projects
- `GET /api/projects/starred` - Get all starred repositories
- `GET /api/projects/starred/language/{language}` - Filter by language
- `POST /api/projects/starred/sync` - Manual sync trigger
- `GET /api/projects/starred/stats` - Repository statistics
- `GET /api/projects/starred/rate-limit` - GitHub API rate limit

### Admin Operations
- `POST /api/admin/factory-reset` - Factory reset (requires token)
- `GET /api/admin/factory-reset/stream/{jobId}` - SSE progress streaming
- `GET /api/admin/factory-reset/audit` - Reset audit history
- `GET /api/admin/sync-config` - Get sync configuration
- `PUT /api/admin/sync-config` - Update sync settings
- `POST /api/admin/sync-config/run-now` - Trigger immediate sync

## 🛠️ Tech Stack

### Backend
- **Framework**: Spring Boot 3.5.0
- **Language**: Java 25
- **Database**: PostgreSQL 14
- **Migrations**: Flyway 11.7
- **Caching**: Caffeine 3.1
- **Build Tool**: Maven 3.9

### External Integrations
- **GitHub API** - Repository synchronization
- **Anthropic Claude API** - Semantic analysis
- **Trello API** - Project management

### Testing & Quality
- **JUnit 5** - Unit testing
- **Mockito** - Mocking framework
- **TestContainers** - Integration testing
- **JaCoCo** - Code coverage

## 📊 Project Status

**Current Sprint**: Sprint 3 - Testing & CI/CD
**Status**: 🟢 Active Development

| Sprint | Status | Completion |
|--------|--------|------------|
| Sprint 1 | ✅ Complete | 100% |
| Sprint 2 | ✅ Complete | 100% |
| Sprint 3 | 🟡 In Progress | 0% |

See [Sprint 3 Tracker](docs/sprints/sprint-3/tracker.md) for current progress.

### Recent Achievements (Sprint 2)
- ⚡ **99.3% query reduction** - Eliminated N+1 queries
- 📊 **21 performance indexes** - Optimized database queries
- 🎯 **Cache eviction system** - Automatic cache invalidation
- 📈 **Real-time monitoring** - Cache metrics endpoint
- 🧪 **E2E smoke tests** - Automated validation framework

## 🏗️ Project Structure

```
portfolio-spring/
├── src/
│   ├── main/
│   │   ├── java/com/portfolio/
│   │   │   ├── adapter/        # Hexagonal architecture adapters
│   │   │   ├── application/    # Application layer
│   │   │   ├── config/         # Spring configuration
│   │   │   ├── controller/     # REST controllers
│   │   │   ├── domain/         # Domain models
│   │   │   ├── dto/            # Data transfer objects
│   │   │   ├── model/          # JPA entities
│   │   │   ├── repository/     # Data access layer
│   │   │   └── service/        # Business logic
│   │   └── resources/
│   │       ├── db/migration/   # Flyway migrations
│   │       └── application.properties
│   └── test/                   # Test files
├── docs/                       # Documentation
│   ├── getting-started/
│   ├── architecture/
│   ├── deployment/
│   ├── api/
│   ├── sprints/
│   └── guides/
├── scripts/                    # Utility scripts
├── .env.example               # Environment template
├── CLAUDE.md                  # AI assistant instructions
└── pom.xml                    # Maven configuration
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines.

## 📝 Development Workflow

This project follows an Agile workflow with Trello integration:

1. **Sprint Planning** - Tasks organized in Trello board
2. **Daily Updates** - Track progress in `docs/sprints/sprint-X/tracker.md`
3. **Code Quality** - Regular commits with meaningful messages
4. **Documentation** - Update docs with new features
5. **Testing** - Write tests for all new code

See [Trello Workflow Guide](docs/guides/trello-workflow.md) for details.

## 📈 Performance Metrics

Current performance targets (Sprint 2):

- ✅ Database queries: **99.3% reduction** (151 → 1)
- ✅ Cache hit ratio: **>80%**
- ✅ API response time: **<100ms** (avg 14ms)
- ✅ Test coverage: **>70%** (target)

## 🔧 Development Scripts

```bash
# Start development environment
./dev.sh

# Backend only
./dev.sh --backend-only

# Frontend only
./dev.sh --frontend-only

# Clean ports and restart
./dev.sh --clean

# Stop all services
./stop.sh

# Run smoke tests
./scripts/sprint2_smoke_tests.sh
```

See [DEV_SCRIPTS.md](docs/getting-started/dev-scripts.md) for all available commands.

## 🐛 Troubleshooting

### Common Issues

**Port 8080 already in use**
```bash
./dev.sh --clean
```

**Database connection error**
```bash
# Check PostgreSQL is running
pg_isready -h localhost -p 5432

# Recreate database
psql -U postgres -c "DROP DATABASE IF EXISTS portfolio_db;"
psql -U postgres -c "CREATE DATABASE portfolio_db;"
```

**Flyway migration checksum mismatch**
```bash
# Drop and recreate database (development only)
psql -U postgres -c "DROP DATABASE IF EXISTS portfolio_db; CREATE DATABASE portfolio_db;"
./mvnw spring-boot:run
```

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👤 Author

**Bernard Uriza**

- GitHub: [@bernarduriza](https://github.com/bernarduriza)
- Portfolio: [Your Portfolio URL]

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Anthropic for Claude API
- GitHub for repository integration
- PostgreSQL team for the robust database

## 📞 Support

For questions or issues:

1. Check the [documentation](docs/)
2. Review [troubleshooting](#-troubleshooting)
3. Open an [issue](https://github.com/your-username/portfolio-spring/issues)
4. Contact: your-email@example.com

---

**Status**: Active Development | **Last Updated**: 2025-10-25 | **Version**: 1.0.0-SNAPSHOT
