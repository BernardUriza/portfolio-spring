#!/bin/sh
set -e

echo "Starting Spring Boot application on Render..."
echo "PORT: ${PORT:-8080}"
echo "Profile: ${SPRING_PROFILES_ACTIVE:-render}"

# Convert DATABASE_URL to JDBC format if present
if [ -n "$DATABASE_URL" ]; then
    echo "Converting DATABASE_URL to JDBC format..."

    # Extract components from DATABASE_URL
    # Format: postgres://user:pass@host:port/database?params

    # Remove postgres:// or postgresql:// prefix
    DB_URL_NO_SCHEME=$(echo "$DATABASE_URL" | sed -E 's|^postgres(ql)?://||')

    # Extract user:pass
    USER_PASS=$(echo "$DB_URL_NO_SCHEME" | sed -E 's|@.*||')
    DB_USER=$(echo "$USER_PASS" | cut -d: -f1)
    DB_PASS=$(echo "$USER_PASS" | cut -d: -f2)

    # Extract host:port/database?params
    HOST_PORT_DB=$(echo "$DB_URL_NO_SCHEME" | sed -E 's|^[^@]+@||')

    # Build JDBC URL
    export JDBC_DATABASE_URL="jdbc:postgresql://${HOST_PORT_DB}"

    # Set individual components for Spring
    export SPRING_DATASOURCE_URL="${JDBC_DATABASE_URL}"
    export SPRING_DATASOURCE_USERNAME="${DB_USER}"
    export SPRING_DATASOURCE_PASSWORD="${DB_PASS}"

    echo "Database configured: ${JDBC_DATABASE_URL%%\?*}" # Hide params for security
    echo "Database user: ${DB_USER}"
fi

# Set JVM options for Render (512MB free tier)
JAVA_OPTS="${JAVA_OPTS:--Xmx400m -Xms128m}"
JAVA_OPTS="${JAVA_OPTS} -XX:MaxRAMPercentage=75"
JAVA_OPTS="${JAVA_OPTS} -XX:+ExitOnOutOfMemoryError"
JAVA_OPTS="${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom"

# Set Spring profile
JAVA_OPTS="${JAVA_OPTS} -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-render}"

# Set server port (critical for Render)
JAVA_OPTS="${JAVA_OPTS} -Dserver.port=${PORT:-8080}"

# Add health check endpoint
JAVA_OPTS="${JAVA_OPTS} -Dmanagement.endpoints.web.exposure.include=health"
JAVA_OPTS="${JAVA_OPTS} -Dmanagement.endpoint.health.show-details=always"

echo "Starting with JAVA_OPTS: ${JAVA_OPTS}"

# Start the application
exec java ${JAVA_OPTS} -jar /app/app.jar