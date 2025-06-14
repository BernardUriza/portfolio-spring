# Use an official OpenJDK runtime as a parent image
FROM eclipse-temurin:17-jdk

# Set the working directory in the container
WORKDIR /app

# Copy the Maven wrapper and project files
COPY . /app

# Grant execute permissions to mvnw
RUN chmod +x mvnw

# Package the app
RUN ./mvnw clean package -DskipTests

# Run the app
CMD ["java", "-jar", "target/portfolio-spring-0.0.1-SNAPSHOT.jar"]
