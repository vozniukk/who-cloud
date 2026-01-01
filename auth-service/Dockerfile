# Runtime stage only - build locally first with: gradlew build -x test
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the pre-built jar
COPY build/libs/auth-service-1.0.0-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
