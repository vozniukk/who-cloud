# Runtime stage only - build locally first with: gradlew build -x test
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the pre-built jar
COPY build/libs/*.jar app.jar

# Expose port
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
