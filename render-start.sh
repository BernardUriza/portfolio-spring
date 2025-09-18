#!/bin/bash

# Render Start Script for Spring Boot Portfolio Backend
# This script handles DATABASE_URL conversion and starts the application

echo "Starting Portfolio Backend on Render..."
echo "Active Profile: ${SPRING_PROFILES_ACTIVE:-default}"
echo "Port: ${PORT:-8080}"

# Convert DATABASE_URL to JDBC format if present
if [ -n "$DATABASE_URL" ]; then
    echo "Converting DATABASE_URL to JDBC format..."
    # Convert postgres:// or postgresql:// to jdbc:postgresql://
    export JDBC_DATABASE_URL=$(echo "$DATABASE_URL" | sed -E 's/^postgres(ql)?:/jdbc:postgresql:/')
    echo "JDBC URL configured (host: $(echo $JDBC_DATABASE_URL | sed -E 's/.*@([^:/]+).*/\1/'))"
fi

# Set memory limits for Render free tier (512MB)
JAVA_OPTS="${JAVA_OPTS:--Xmx400m -Xms128m}"

# Add server port configuration
JAVA_OPTS="$JAVA_OPTS -Dserver.port=${PORT:-8080}"

# Enable Spring profile
JAVA_OPTS="$JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-render}"

# Find the JAR file
JAR_FILE=$(find target -name "*.jar" -type f | head -1)

if [ -z "$JAR_FILE" ]; then
    echo "ERROR: No JAR file found in target directory"
    echo "Make sure the build command completed successfully"
    exit 1
fi

echo "Starting application: $JAR_FILE"
echo "Java options: $JAVA_OPTS"

# Start the application
exec java $JAVA_OPTS -jar "$JAR_FILE"