# CLAUDE.md

This file provides guidance to Claude Code when working with this Spring Boot backend repository.

## Agile Workflow with Trello

### Trello CLI Tool v2.0

- **Executable**: `trello` (globally available via PATH)
- **Project**: `/Users/bernardurizaorozco/Documents/trello-cli-python/`
- **Documentation**: `~/Documents/trello-cli-python/CLAUDE_INTEGRATION.md`
- **Backward Compatibility**: `trello-cli.py` alias works

**Quick Reference:**

```bash
trello boards                          # List all boards
trello lists <board_id>                # Show lists
trello cards <list_id>                 # Show cards
trello show-card <card_id>             # Card details
trello move-card <card_id> <list_id>   # Move card
trello add-comment <card_id> "text"    # Add comment
trello add-label <card_id> "color" "name"  # Add label
```

### Daily Workflow

1. **Start of Day**: List available cards to work on

   ```bash
   trello boards
   trello lists <board_id>
   trello cards <list_id>
   ```

2. **Select a Card**: Show card details to understand the task

   ```bash
   trello show-card <card_id>
   ```

3. **Move to In Progress**: When starting work on a card

   ```bash
   trello move-card <card_id> <in_progress_list_id>
   ```

4. **Update Progress**: Add comments as you complete milestones

   ```bash
   trello add-comment <card_id> "Completed endpoint implementation"
   ```

5. **Move to Testing**: When implementation is done

   ```bash
   trello move-card <card_id> <testing_list_id>
   ```

6. **Move to Done**: After testing and verification

   ```bash
   trello move-card <card_id> <done_list_id>
   ```

### Board: AI Portfolio Sprint 1 (ID: 68fcf05e481843db13204397)

**Key Lists**:

- `68fcff45adca2da863178ce6` - ✅ Ready (cards ready to work on)
- `68fcff46fa7dbc9cc069eaef` - 📝 To Do (Sprint)
- `68fcff465168c13a1c3edf87` - ⚙️ In Progress (active work)
- `68fcff475ac54cc9b01204af` - 🧪 Testing (testing phase)
- `68fcff48a96dfec5ea4c4f6d` - ✅ Done (completed work)

### Agile Best Practices

- Always move cards through the workflow (Ready → In Progress → Testing → Done)
- Add comments to document progress and decisions
- Update card descriptions if requirements change
- Keep only ONE card in "In Progress" at a time
- Move cards to "Done" only after verification

## Development Commands

- `./mvnw spring-boot:run` - Start Spring Boot application (port 8080)
- `./mvnw clean compile` - Clean and compile project
- `./mvnw test` - Run unit tests
- `./mvnw clean package` - Build JAR file
- `./mvnw spring-boot:run -Dspring-boot.run.profiles=dev` - Run with dev profile

## Project Architecture

This is a Spring Boot portfolio backend API with the following structure:

### Core Structure

- **Controllers**: REST endpoints in `src/main/java/com/portfolio/controller/`
- **Services**: Business logic in `src/main/java/com/portfolio/service/`
- **Models**: JPA entities in `src/main/java/com/portfolio/model/`
- **Repositories**: Data access layer in `src/main/java/com/portfolio/repository/`
- **DTOs**: Data transfer objects in `src/main/java/com/portfolio/dto/`
- **Config**: Configuration classes in `src/main/java/com/portfolio/config/`

### Key Features

- **Starred Projects**: GitHub API integration with automated sync service
- **Contact Form**: Email handling with validation
- **CORS**: Configured for frontend integration (localhost:4200)
- **H2 Database**: In-memory database for local development
- **Scheduling**: Background tasks enabled with @EnableScheduling

### Database Configuration

- H2 in-memory database for development
- JPA with Hibernate
- Console available at `/h2-console`
- Auto-create tables with `spring.jpa.hibernate.ddl-auto=create-drop`

### GitHub Integration

- Requires `GITHUB_TOKEN` environment variable
- Configure `github.username` in application.properties
- **Auto Sync**: Configurable scheduled synchronization (replaces fixed 5-minute interval)
- Rate limiting and error handling implemented

### API Endpoints

- `/api/projects/starred` - Get all starred repositories
- `/api/projects/starred/language/{language}` - Filter by language
- `/api/projects/starred/sync` - Manual sync trigger
- `/api/projects/starred/stats` - Repository statistics
- `/api/projects/starred/rate-limit` - GitHub API rate limit status
- `/api/admin/factory-reset` - Factory reset endpoint (destructive)
- `/api/admin/factory-reset/stream/{jobId}` - SSE progress streaming
- `/api/admin/factory-reset/audit` - Factory reset audit history

### Auto Sync Configuration

The application now features a dynamic, configurable auto-sync system that replaces the legacy fixed @Scheduled approach:

**Core Features:**

- **Admin-controlled**: Enable/disable via UI checkbox
- **Flexible intervals**: 1-168 hours (configurable at runtime)
- **Dynamic rescheduling**: No restart required for config changes
- **Concurrency protection**: Prevents overlapping sync executions
- **Persistent configuration**: Settings stored in database with audit trail

**API Endpoints:**

- `GET /api/admin/sync-config` - Retrieve current sync configuration
- `PUT /api/admin/sync-config` - Update sync settings (enabled, intervalHours)
- `GET /api/admin/sync-config/status` - Get real-time sync status and timing
- `POST /api/admin/sync-config/run-now` - Trigger immediate sync execution

**Architecture:**

- `SyncConfigJpaEntity`: JPA entity with validation constraints
- `SyncConfigService`: Business logic for CRUD operations
- `SyncSchedulerService`: ThreadPoolTaskScheduler with dynamic task management
- `SyncConfigAdminController`: REST endpoints with validation and error handling

**Security & Validation:**

- Input validation: 1-168 hours range enforcement
- Concurrency control: AtomicBoolean prevents race conditions
- Error handling: Graceful failure with detailed logging
- Admin-only access: Secured endpoints (future enhancement)

## Important Claude Code Lessons Learned

### Directory Navigation

**CRITICAL**: The Bash tool does NOT persist directory changes between commands. Each `cd` command only affects that single execution.

**Solution**: Always prefix commands with directory change using `&&`:

```bash
cd "C:\\Users\\Bernard\\Documents\\GitHub\\portfolio-backend" && git status
```

**Wrong approach** (doesn't work):

```bash
# Command 1
cd /path/to/backend
# Command 2 - this will be in the original directory!
git status
```

### Git Operations

- Always verify you're in the correct repository before git commands
- Use `pwd` to confirm current directory
- Check branch with `git branch` before operations
- Handle merge conflicts and stash operations carefully

### File Structure Verification

- Use `ls` commands to verify file structure exists before assuming paths
- Check if directories exist before trying to access files
- Backend structure: `src/main/java/com/portfolio/`
- Frontend structure: `src/app/`

### Environment Setup

#### Using .env File (Recommended)

1. Copy `.env.example` to `.env`: `cp .env.example .env`
2. Configure the following variables in your `.env` file:
   - `GITHUB_USERNAME=your-actual-github-username`
   - `GITHUB_TOKEN=your-github-personal-access-token`
   - `ANTHROPIC_API_KEY=your-claude-api-key`
   - `ENABLE_FACTORY_RESET=true` (optional, for factory reset functionality)
   - `ADMIN_RESET_TOKEN=your-secure-token` (required if factory reset enabled)

#### Using Environment Variables

Alternatively, set environment variables directly:

1. `GITHUB_USERNAME` with your GitHub username
2. `GITHUB_TOKEN` with GitHub personal access token
3. `ANTHROPIC_API_KEY` with your Claude API key
4. Frontend CORS origin in application.properties if different from localhost:4200
5. For Factory Reset: `ENABLE_FACTORY_RESET=true` and `ADMIN_RESET_TOKEN=your-secure-token`

### Claude API Integration

The system now includes semantic analysis that:

- Calls Claude API after each GitHub sync
- Transforms starred repositories into Skills, Experiences, and Projects
- Uses intelligent prompt engineering to extract meaningful data
- Handles API failures gracefully with comprehensive logging
- Only processes new repos or those with significant changes (description, language, topics)

### Factory Reset Feature

A comprehensive admin feature for database cleanup with:

- **Security**: Token validation, confirmation headers, rate limiting
- **Async Processing**: Server-Sent Events (SSE) for progress streaming
- **Database Support**: PostgreSQL (TRUNCATE) and H2 (repository-based)
- **Audit Trail**: Complete logging of all reset attempts with timing
- **Hexagonal Architecture**: Domain-driven design with clean separation

The implementation includes:

- `ResetAudit` domain entity with lifecycle methods
- `FactoryResetService` with database-specific strategies
- `AdminResetController` with comprehensive security validation
- SSE streaming for real-time progress updates
- Rate limiting (1 attempt per 10 minutes per IP)
- Comprehensive unit tests for domain and service layers

When making changes, follow Spring Boot conventions for package structure, use Lombok annotations for boilerplate reduction, and implement proper error handling with try-catch blocks and logging.
