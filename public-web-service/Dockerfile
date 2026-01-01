# Multi-stage build
# Runtime stage only - build locally first with: gradlew build -x test
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY build/libs/public-web-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "app.jar"]
