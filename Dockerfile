# === Build stage ===
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .

# Normalizar EOL y permisos antes de ejecutar
RUN find . -type f -name "*.sh" -exec sed -i 's/\r$//' {} \; && \
    sed -i 's/\r$//' mvnw && chmod +x mvnw && \
    chmod +x *.sh 2>/dev/null || true

# Build sin tests ni spotless para rapidez/robustez en Render
RUN ./mvnw -q -DskipTests -Dspotless.skip package

# === Run stage ===
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=render

# Copiar JAR del stage de build
COPY --from=build /workspace/target/*SNAPSHOT.jar /app/app.jar

# Copiar y preparar entrypoint
COPY render-entrypoint.sh /app/render-entrypoint.sh
RUN sed -i 's/\r$//' /app/render-entrypoint.sh && chmod +x /app/render-entrypoint.sh

# Puerto dinámico de Render
EXPOSE 8080

# Ejecutar via entrypoint
ENTRYPOINT ["/app/render-entrypoint.sh"]