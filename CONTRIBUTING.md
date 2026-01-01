# Contributing to WHO Cloud Microservices

Thank you for your interest in contributing to WHO Cloud! This document provides guidelines and instructions for contributing.

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Coding Standards](#coding-standards)
- [Testing](#testing)
- [Submitting Changes](#submitting-changes)
- [Reporting Bugs](#reporting-bugs)

## 🤝 Code of Conduct

- Be respectful and inclusive
- Welcome newcomers and encourage diverse perspectives
- Focus on constructive feedback
- Follow the project's technical standards

## 🚀 Getting Started

### Prerequisites

1. **Install Required Tools**:
   - Java 21 LTS (Eclipse Temurin)
   - Gradle 8.11+ (via wrapper)
   - Docker Desktop
   - Git
   - IDE (IntelliJ IDEA, VS Code, or Eclipse)

2. **Fork and Clone**:
   ```bash
   # Fork the repository on GitHub
   git clone https://github.com/YOUR_USERNAME/who-cloud.git
   cd who-cloud
   ```

3. **Set Up Development Environment**:
   ```bash
   # Build all modules
   ./gradlew build -x test
   
   # Start PostgreSQL
   docker-compose up -d postgres
   
   # Run a service
   ./gradlew :api-gateway:bootRun
   ```

### Project Structure

```
who-cloud/
├── common/                  # Shared library (edit carefully!)
├── api-gateway/            # Spring Cloud Gateway
├── auth-service/           # Authentication service
├── user-management-service/ # User CRUD
├── information-service-a/  # Information endpoints
├── information-service-b/  # Information endpoints
├── business-service-*/     # Business logic services
├── admin-portal-service/   # Admin dashboard
└── public-web-service/     # Public APIs
```

## 🔄 Development Workflow

### 1. Create a Branch

```bash
# Create feature branch
git checkout -b feature/amazing-feature

# Or bugfix branch
git checkout -b fix/issue-123
```

**Branch Naming**:
- `feature/` - New features
- `fix/` - Bug fixes
- `docs/` - Documentation updates
- `refactor/` - Code refactoring
- `test/` - Test additions/improvements
- `chore/` - Maintenance tasks

### 2. Make Changes

- Write clean, readable code
- Follow existing patterns
- Add comments for complex logic
- Update documentation as needed

### 3. Test Your Changes

```bash
# Run unit tests
./gradlew test

# Run specific module tests
./gradlew :auth-service:test

# Build all modules
./gradlew build

# Test with Docker
./gradlew build -x test
docker-compose up -d --build
```

### 4. Commit Your Changes

```bash
# Stage changes
git add .

# Commit with descriptive message
git commit -m "feat: add user profile endpoint"
```

**Commit Message Format**:
```
<type>: <description>

[optional body]

[optional footer]
```

**Types**:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting)
- `refactor`: Code refactoring
- `test`: Test additions/changes
- `chore`: Maintenance tasks

**Examples**:
```
feat: implement JWT authentication in auth-service

Added JWT token generation and validation
- JwtUtil class for token operations
- SecurityConfig for endpoint protection
- Updated AuthController with token endpoints

Closes #123
```

### 5. Push and Create Pull Request

```bash
# Push to your fork
git push origin feature/amazing-feature

# Create Pull Request on GitHub
```

## 📝 Coding Standards

### Java Style Guide

1. **Code Formatting**:
   - Use 4 spaces for indentation
   - Maximum line length: 120 characters
   - Use braces even for single-line blocks

2. **Naming Conventions**:
   ```java
   // Classes: PascalCase
   public class UserController { }
   
   // Methods/Variables: camelCase
   public void getUserById(Long userId) { }
   
   // Constants: UPPER_SNAKE_CASE
   private static final String JWT_SECRET = "secret";
   
   // Packages: lowercase
   package com.whocloud.auth.controller;
   ```

3. **Annotations**:
   ```java
   @RestController
   @RequestMapping("/api/users")
   @Slf4j  // Lombok for logging
   public class UserController {
       
       @GetMapping("/{id}")
       @PreAuthorize("hasRole('USER')")
       public ResponseEntity<ApiResponse<User>> getUser(@PathVariable Long id) {
           // Implementation
       }
   }
   ```

### Project-Specific Patterns

1. **API Response Format**:
   ```java
   // Always use ApiResponse wrapper from common module
   return ApiResponse.success(data);
   return ApiResponse.error("Error message");
   ```

2. **Exception Handling**:
   ```java
   // Use GlobalExceptionHandler from common module
   throw new ResourceNotFoundException("User not found");
   ```

3. **Logging**:
   ```java
   @Slf4j
   public class MyService {
       public void doSomething() {
           log.info("Processing request");
           log.debug("Debug details: {}", details);
           log.error("Error occurred", exception);
       }
   }
   ```

4. **Configuration**:
   ```java
   // Use @Value or @ConfigurationProperties
   @Value("${jwt.secret}")
   private String jwtSecret;
   ```

### Gradle Build Files

- Use Kotlin DSL (`build.gradle.kts`)
- Keep dependencies organized and commented
- Use dependency management from parent

## ✅ Testing

### Unit Tests

```java
@SpringBootTest
class UserServiceTest {
    
    @Autowired
    private UserService userService;
    
    @MockBean
    private UserRepository userRepository;
    
    @Test
    void getUserById_ShouldReturnUser() {
        // Given
        User user = new User(1L, "John Doe");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        // When
        User result = userService.getUserById(1L);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("John Doe");
    }
}
```

### Integration Tests

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class UserControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void getUser_ShouldReturn200() {
        ResponseEntity<String> response = restTemplate
            .getForEntity("/api/users/1", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
```

### Testing Checklist

- [ ] Unit tests for business logic
- [ ] Integration tests for endpoints
- [ ] Test edge cases and error scenarios
- [ ] All tests pass: `./gradlew test`
- [ ] No test warnings or errors

## 📤 Submitting Changes

### Pull Request Process

1. **Update Documentation**:
   - Update README.md if adding features
   - Add/update JavaDoc comments
   - Update API documentation

2. **Create Pull Request**:
   - Clear title describing the change
   - Detailed description of what and why
   - Reference related issues
   - Add screenshots if UI changes

3. **PR Template**:
   ```markdown
   ## Description
   Brief description of changes
   
   ## Type of Change
   - [ ] Bug fix
   - [ ] New feature
   - [ ] Breaking change
   - [ ] Documentation update
   
   ## Testing
   - [ ] Unit tests added/updated
   - [ ] Integration tests added/updated
   - [ ] All tests pass
   - [ ] Manual testing completed
   
   ## Checklist
   - [ ] Code follows project style
   - [ ] Self-review completed
   - [ ] Comments added to complex code
   - [ ] Documentation updated
   - [ ] No new warnings generated
   
   ## Related Issues
   Closes #123
   ```

4. **Review Process**:
   - Maintainers will review your PR
   - Address feedback promptly
   - Make requested changes
   - Keep PR updated with main branch

### After Merge

```bash
# Sync your fork
git checkout main
git pull upstream main
git push origin main

# Delete feature branch
git branch -d feature/amazing-feature
git push origin --delete feature/amazing-feature
```

## 🐛 Reporting Bugs

### Before Reporting

1. Check existing issues
2. Verify it's reproducible
3. Test with latest version
4. Gather relevant information

### Bug Report Template

```markdown
## Bug Description
Clear description of the bug

## Steps to Reproduce
1. Step one
2. Step two
3. Step three

## Expected Behavior
What should happen

## Actual Behavior
What actually happens

## Environment
- OS: Windows 11 / macOS 13 / Ubuntu 22.04
- Java Version: 21
- Docker Version: 24.0.7
- Branch/Commit: main / abc123

## Logs
```
Paste relevant logs here
```

## Screenshots
If applicable

## Additional Context
Any other relevant information
```

## 💡 Feature Requests

### Suggesting Features

1. Check if feature already requested
2. Ensure it aligns with project goals
3. Provide detailed use case
4. Consider implementation complexity

### Feature Request Template

```markdown
## Feature Description
Clear description of the feature

## Problem It Solves
What problem does this address?

## Proposed Solution
How should it work?

## Alternatives Considered
What other approaches were considered?

## Additional Context
Mockups, diagrams, examples
```

## 🎨 UI/UX Contributions

Currently backend-only, but UI contributions welcome:
- Design mockups for admin portal
- API documentation improvements
- User experience enhancements

## 📚 Documentation

### Documentation Types

1. **Code Documentation**:
   - JavaDoc for public APIs
   - Inline comments for complex logic
   - README in each module

2. **User Documentation**:
   - Setup guides
   - API documentation
   - Troubleshooting guides

3. **Developer Documentation**:
   - Architecture decisions
   - Design patterns used
   - Development guides

### Writing Style

- Clear and concise
- Use examples
- Keep updated
- Test instructions

## 🏆 Recognition

Contributors will be:
- Listed in CONTRIBUTORS.md
- Credited in release notes
- Mentioned in project documentation

## 📞 Getting Help

- **GitHub Issues**: Bug reports and feature requests
- **GitHub Discussions**: Questions and ideas
- **Pull Request Comments**: Code-specific questions

## 📄 License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

Thank you for contributing to WHO Cloud! 🎉
