# WHO Cloud Microservices Platform

A comprehensive microservices architecture built with **Spring Boot 3.4.1**, **Java 21**, and **Gradle**.

## 🏗️ Architecture

This project implements a microservices architecture with the following services:

- **API Gateway** (Port 8080) - Spring Cloud Gateway for routing
- **Auth Service** (Port 8081) - Authentication & JWT management
- **User Management Service** (Port 8082) - User CRUD operations
- **Information Service A** (Port 8083) - Information management
- **Information Service B** (Port 8084) - Information management
- **Business Service 1** (Port 8085) - Business logic
- **Business Service 2** (Port 8086) - Business logic
- **Business Service 3** (Port 8087) - Business logic
- **Admin Portal Service** (Port 8088) - Admin dashboard
- **Public Web Service** (Port 8089) - Public APIs
- **Common** - Shared library with DTOs and utilities

## 🚀 Technology Stack

- **Java**: 21 (LTS)
- **Spring Boot**: 3.4.1
- **Spring Cloud**: 2024.0.0
- **Build Tool**: Gradle with Kotlin DSL
- **Database**: H2 (in-memory for development)
- **Testing**: JUnit 5

## 📋 Prerequisites

- Java 21 or higher
- Gradle 8.x (or use the included wrapper)

## 🛠️ Building the Project

Build all services:
```bash
./gradlew build
```

Build a specific service:
```bash
./gradlew :api-gateway:build
```

## ▶️ Running Services

Run all services individually:
```bash
# API Gateway
./gradlew :api-gateway:bootRun

# Auth Service
./gradlew :auth-service:bootRun

# User Management Service
./gradlew :user-management-service:bootRun

# ... and so on for other services
```

## 🧪 Running Tests

Run all tests:
```bash
./gradlew test
```

Run tests for a specific service:
```bash
./gradlew :auth-service:test
```

## 📡 API Endpoints

All services are accessible through the API Gateway at `http://localhost:8080`:

- `/api/auth/**` → Auth Service
- `/api/users/**` → User Management Service
- `/api/information/a/**` → Information Service A
- `/api/information/b/**` → Information Service B
- `/api/business/service1/**` → Business Service 1
- `/api/business/service2/**` → Business Service 2
- `/api/business/service3/**` → Business Service 3
- `/api/admin/**` → Admin Portal Service
- `/api/public/**` → Public Web Service

## 📊 Health Monitoring

Each service exposes health endpoints:
- Health: `http://localhost:<port>/actuator/health`
- Info: `http://localhost:<port>/actuator/info`

## 🏢 Project Structure

```
who-cloud/
├── api-gateway/              # API Gateway service
├── auth-service/             # Authentication service
├── user-management-service/  # User management service
├── information-service-a/    # Information service A
├── information-service-b/    # Information service B
├── business-service-1/       # Business service 1
├── business-service-2/       # Business service 2
├── business-service-3/       # Business service 3
├── admin-portal-service/     # Admin portal service
├── public-web-service/       # Public web service
├── common/                   # Shared library
├── build.gradle.kts          # Root build configuration
└── settings.gradle.kts       # Project structure definition
```

## 🔧 Development

### Adding a New Service

1. Add the service to `settings.gradle.kts`
2. Create a new directory with `build.gradle.kts`
3. Implement the Spring Boot application
4. Add routing in API Gateway configuration

### Common Module

The `common` module provides shared functionality:
- `ApiResponse<T>`: Standard API response wrapper
- `GlobalExceptionHandler`: Centralized exception handling

## 📝 License

This project is licensed under the MIT License.