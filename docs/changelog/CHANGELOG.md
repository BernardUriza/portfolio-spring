# Changelog

All notable changes to the Portfolio Backend API will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added - 2025-10-24

- 🧠 **Claude AI Analysis Endpoint** - Implemented `POST /api/admin/portfolio/{id}/analyze`
  - Analyzes GitHub repositories using Claude AI
  - Returns structured insights about project technologies, skills, and experiences
  - Includes README content preview and repository metadata
  - Validates project linkage to source repository
  - Comprehensive error handling for unlinked projects and missing data

- 📦 **ClaudeAnalysisResponse DTO** - New response record for AI analysis results
  - `insights`: AI-generated analysis with project overview, technologies, skills, and recommendations
  - `repositoryName`: Full GitHub repository name (owner/repo)
  - `readmeContent`: Raw README markdown for frontend preview

### Technical Details

- **Architecture**: Follows existing SOLID patterns in `PortfolioAdminController`
- **Dependencies**: Integrates with existing `AIServicePort` interface
- **Service Layer**: Reuses `aiService.analyzeRepository()` method
- **Data Flow**: Portfolio Project → Source Repository → AI Analysis → Response DTO
- **Error Handling**:
  - 400 Bad Request: Project not linked to repository
  - 404 Not Found: Project or repository not found
  - 500 Internal Error: AI service failure

### Integration Points

- Frontend modal: `openClaudeAnalysisModal()` in `project-completion-table.component.ts`
- Backend endpoint: `POST /api/admin/portfolio/{id}/analyze`
- Service: `AIServicePort.analyzeRepository()`

---

## [0.0.1-SNAPSHOT] - 2024-XX-XX

### Initial Release
- Portfolio management system
- GitHub repository synchronization
- Project completion tracking
- Admin authentication
- AI-powered project analysis
