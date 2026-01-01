# Multi-stage build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY gradle gradle
COPY gradlew .
COPY settings.gradle.kts .
COPY build.gradle.kts .
COPY common common
COPY information-service-a information-service-a
RUN ./gradlew :information-service-a:build -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/information-service-a/build/libs/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
