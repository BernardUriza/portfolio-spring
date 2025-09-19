# === Build stage ===
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
COPY . .
# Evita tests/spotless en Render (si fallan por DB)
RUN ./mvnw -q -DskipTests -Dspotless.skip package

# === Run stage ===
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=render
COPY --from=build /workspace/target/*SNAPSHOT.jar /app/app.jar
COPY render-entrypoint.sh /app/render-entrypoint.sh
RUN chmod +x /app/render-entrypoint.sh
EXPOSE 8080
ENTRYPOINT ["/app/render-entrypoint.sh"]