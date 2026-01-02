# Simple runtime-only Dockerfile
# Build locally first: ./gradlew public-web-service:build -x test
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY public-web-service/build/libs/public-web-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "app.jar"]
