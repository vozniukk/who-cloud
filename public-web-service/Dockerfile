# Multi-stage build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY gradle gradle
COPY gradlew .
COPY settings.gradle.kts .
COPY build.gradle.kts .
COPY common common
COPY public-web-service public-web-service
RUN ./gradlew :public-web-service:build -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/public-web-service/build/libs/*.jar app.jar
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "app.jar"]
