FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw .
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw dependency:go-offline

COPY src ./src
RUN ./mvnw clean package -DskipTests

EXPOSE 8080
CMD ["java", "-jar", "target/portfolio-spring-0.0.1-SNAPSHOT.jar"]