# Multi-stage build for smaller production image
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy Maven wrapper and POM
COPY .mvn/ .mvn
COPY mvnw .
COPY pom.xml .

# Make mvnw executable and download dependencies
RUN chmod +x mvnw && ./mvnw dependency:go-offline

# Copy source code and build
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Production stage
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Copy configuration files
COPY --from=builder /app/src/main/resources/application*.properties ./config/

# Create entrypoint script to handle DATABASE_URL conversion (as root)
RUN echo '#!/bin/sh\n\
if [ -n "$DATABASE_URL" ]; then\n\
  export JDBC_DATABASE_URL=$(echo "$DATABASE_URL" | sed -E "s/^postgres(ql)?:/jdbc:postgresql:/")\n\
fi\n\
exec java $JAVA_OPTS -Dserver.port=${PORT:-8080} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -jar app.jar' > /app/entrypoint.sh && \
chmod +x /app/entrypoint.sh

# Create non-root user for security
RUN useradd -m -u 1000 spring && chown -R spring:spring /app
USER spring

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT:-8080}/actuator/health || exit 1

# Environment variables defaults
ENV SPRING_PROFILES_ACTIVE=render \
    JAVA_OPTS="-Xmx400m -Xms128m" \
    SERVER_PORT=${PORT:-8080}

# Dynamic port binding (Render will set PORT env var)
EXPOSE ${PORT:-8080}

# Start application with entrypoint script
ENTRYPOINT ["/app/entrypoint.sh"]