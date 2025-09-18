# Portfolio Backend API

**Spring Boot REST API for Portfolio Management**

A robust RESTful API built with Java 21 and Spring Boot 3.5.0 that powers portfolio applications with project management, skills tracking, experience documentation, and GitHub integration capabilities.

---

## 📖 **Overview**

This backend service provides a comprehensive API for managing professional portfolios. It features automatic GitHub repository synchronization, AI-powered content generation, and dynamic configuration capabilities. Built following clean architecture principles with production-ready features like rate limiting, audit trails, and real-time updates via SSE.

### **Tech Stack**
- **Java 21** with Spring Boot 3.5.0
- **Spring Data JPA** with Hibernate
- **H2 Database** (development) / **PostgreSQL** (production)
- **GitHub API** integration
- **Anthropic Claude API** for AI features
- **Server-Sent Events (SSE)** for real-time updates
- **Maven** build system

---

## 🚀 **Getting Started**

### **Prerequisites**
- Java 21 or higher
- Maven 3.8+
- Git

### **Quick Start**

1. Clone the repository:
```bash
git clone https://github.com/BernardUriza/portfolio-backend.git
cd portfolio-backend
```

2. Copy environment configuration:
```bash
cp .env.example .env
```

3. Configure your `.env` file:
```properties
# Required
GITHUB_USERNAME=your-github-username
GITHUB_TOKEN=your-github-personal-access-token

# Optional AI features
ANTHROPIC_API_KEY=your-claude-api-key

# Admin features
ENABLE_FACTORY_RESET=false
ADMIN_RESET_TOKEN=your-secure-token
```

4. Run the application:
```bash
./mvnw spring-boot:run
```

The API will be available at: **http://localhost:8080**

---

## ⚙️ **Configuration**

### **Application Profiles**

- `default` - Production configuration
- `dev` - Development with H2 console enabled
- `test` - Testing configuration

Run with specific profile:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### **Database Access**

H2 Console (dev only): **http://localhost:8080/h2-console**
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: _(empty)_

### **Environment Variables**

| Variable | Description | Required |
|----------|-------------|----------|
| `GITHUB_USERNAME` | Your GitHub username | Yes |
| `GITHUB_TOKEN` | GitHub Personal Access Token | Yes |
| `ANTHROPIC_API_KEY` | Claude API key for AI features | No |
| `ENABLE_FACTORY_RESET` | Enable admin reset functionality | No |
| `ADMIN_RESET_TOKEN` | Security token for admin operations | No |

---

## 🔗 **API Endpoints**

### **Projects**

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/projects` | List all projects with pagination |
| GET | `/api/projects/{id}` | Get project by ID |
| POST | `/api/projects` | Create new project |
| PUT | `/api/projects/{id}` | Update project |
| DELETE | `/api/projects/{id}` | Delete project |
| GET | `/api/projects/languages` | Get available languages |
| GET | `/api/projects/by-language` | Filter projects by language |

**Example Request:**
```bash
curl -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Portfolio Website",
    "description": "Professional portfolio showcase",
    "link": "https://github.com/user/portfolio",
    "createdDate": "2024-01-15"
  }'
```

### **Skills**

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/skills` | List all skills |
| POST | `/api/skills` | Create new skill |
| PUT | `/api/skills/{id}` | Update skill |
| DELETE | `/api/skills/{id}` | Delete skill |

**Example Request:**
```bash
curl -X POST http://localhost:8080/api/skills \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Spring Boot",
    "category": "BACKEND",
    "level": "EXPERT"
  }'
```

### **Experience**

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/experience` | List all experiences |
| POST | `/api/experience` | Create new experience |
| PUT | `/api/experience/{id}` | Update experience |
| DELETE | `/api/experience/{id}` | Delete experience |

### **GitHub Sync**

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/sync/manual` | Trigger manual sync |
| GET | `/api/sync/status` | Get sync status |
| GET | `/api/sync/progress/{jobId}` | SSE sync progress stream |
| GET | `/api/sync/rate-limit` | GitHub API rate limit |

### **Admin Operations**

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/sync-config` | Get sync configuration |
| PUT | `/api/admin/sync-config` | Update sync settings |
| POST | `/api/admin/sync-config/run-now` | Trigger immediate sync |
| POST | `/api/admin/factory-reset` | Reset database (requires token) |
| GET | `/api/admin/factory-reset/audit` | View reset history |

---

## 🔑 **Authentication & Security**

### **Admin Endpoints Protection**

Admin endpoints require authentication token in header:
```bash
curl -X POST http://localhost:8080/api/admin/factory-reset \
  -H "X-Admin-Reset-Token: your-secure-token" \
  -H "X-Admin-Confirm: DELETE"
```

### **Rate Limiting**

- GitHub API sync: Respects GitHub rate limits
- Factory reset: 1 attempt per 10 minutes per IP
- Automatic retry with exponential backoff

### **CORS Configuration**

Default CORS allows:
- Origin: `http://localhost:4200` (Angular dev server)
- Methods: GET, POST, PUT, DELETE, OPTIONS
- Headers: Content-Type, Authorization

---

## 📦 **Deployment**

### **Building for Production**

```bash
# Clean build
./mvnw clean package

# Run JAR
java -jar target/portfolio-backend-1.0.0.jar
```

### **Docker Deployment**

```dockerfile
FROM openjdk:21-slim
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

Build and run:
```bash
docker build -t portfolio-backend .
docker run -p 8080:8080 --env-file .env portfolio-backend
```

### **Environment-Specific Configuration**

Production requires PostgreSQL configuration:
```properties
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

---

## 🧪 **Testing**

Run all tests:
```bash
./mvnw test
```

Run specific test class:
```bash
./mvnw test -Dtest=ProjectControllerTest
```

Generate coverage report:
```bash
./mvnw jacoco:report
```

---

## 📚 **API Documentation**

### **Swagger UI**
Available at: **http://localhost:8080/swagger-ui.html** (when configured)

### **Response Format**

Success Response:
```json
{
  "data": { ... },
  "message": "Success",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

Error Response:
```json
{
  "error": "Validation failed",
  "message": "Title is required",
  "path": "/api/projects",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### **Pagination**

Paginated endpoints support:
- `page`: Page number (0-indexed)
- `size`: Items per page
- `sort`: Sort field and direction (e.g., `title,desc`)

Example:
```
GET /api/projects?page=0&size=10&sort=createdDate,desc
```

---

## 🛠️ **Development Tools**

### **Useful Maven Commands**

```bash
# Skip tests
./mvnw clean install -DskipTests

# Update dependencies
./mvnw versions:display-dependency-updates

# Clean build
./mvnw clean compile
```

### **IDE Setup**

1. Import as Maven project
2. Install Lombok plugin
3. Enable annotation processing
4. Set Java 21 as SDK

### **Database Migrations**

Flyway migrations location: `src/main/resources/db/migration/`

---

## 🐛 **Troubleshooting**

### **Common Issues**

**Port already in use:**
```bash
# Change port in application.properties
server.port=8081
```

**GitHub rate limit exceeded:**
- Check limits: `GET /api/sync/rate-limit`
- Wait for reset or use different token

**Database connection issues:**
- Verify DATABASE_URL format
- Check credentials
- Ensure database is running

---

## 📄 **License**

MIT License - Created by Bernard Orozco

---

## 🤝 **Contributing**

1. Fork the repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

---

## 📞 **Support**

For issues and questions:
- GitHub Issues: [portfolio-backend/issues](https://github.com/BernardUriza/portfolio-backend/issues)
- Documentation: See `/docs` folder