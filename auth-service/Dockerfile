# Multi-stage build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Gradle files
COPY gradle gradle
COPY gradlew .
COPY settings.gradle.kts .
COPY build.gradle.kts .

# Copy all modules
COPY common common
COPY auth-service auth-service

# Build the application
RUN ./gradlew :auth-service:build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the jar from builder
COPY --from=builder /app/auth-service/build/libs/*.jar app.jar

# Expose port
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
