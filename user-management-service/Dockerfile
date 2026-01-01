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
COPY user-management-service user-management-service

# Make gradlew executable and build
RUN chmod +x gradlew && sed -i 's/\r$//' gradlew && ./gradlew :user-management-service:build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the jar from builder
COPY --from=builder /app/user-management-service/build/libs/*.jar app.jar

# Expose port
EXPOSE 8082

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
