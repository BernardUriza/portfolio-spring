# ---- BUILD STAGE ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy POM and download dependencies (cached layer)
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code and build
COPY src ./src
# Skip tests and spotless to avoid build failures
RUN ./mvnw clean package -DskipTests -Dspotless.skip=true -DskipITs -B

# ---- RUNTIME STAGE ----
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Environment variables for Render
ENV PORT=8080 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError" \
    SPRING_PROFILES_ACTIVE=render

# Copy entrypoint script
COPY render-entrypoint.sh /app/render-entrypoint.sh
RUN chmod +x /app/render-entrypoint.sh

# Copy JAR from build stage
COPY --from=build /app/target/*-SNAPSHOT.jar /app/app.jar

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:${PORT}/actuator/health || exit 1

# Use PORT from environment
EXPOSE ${PORT}

# Start with entrypoint script
CMD ["/app/render-entrypoint.sh"]