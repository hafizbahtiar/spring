# Codebase Structure Documentation

This document provides detailed information about the codebase organization and development guidelines.

## Architecture Overview

The project follows a **Features-First Architecture** (also known as Feature-Driven Development) where code is organized by business domains rather than technical layers.

## Directory Structure Details

### Root Level Structure
```
src/main/java/com/hafizbahtiar/spring/
├── Application.java           # Spring Boot main application class
├── config/                    # Global application configuration
├── security/                  # Security configurations and components
├── common/                    # Shared utilities and common components
└── features/                  # Business feature modules
```

### Feature Module Structure
Each feature follows a consistent structure:

```
features/{feature-name}/
├── config/                    # Feature-specific configuration
├── controller/v1/             # REST API controllers (versioned)
│   └── webhook/               # Webhook controllers (if applicable)
├── service/                   # Business logic services
│   ├── impl/                  # Service implementations
│   └── webhook/               # Webhook processing services (if applicable)
├── repository/                # Data access layer
├── entity/                    # JPA entity classes and domain models
├── exception/                 # Feature-specific exceptions
├── dto/                       # Data Transfer Objects
│   └── webhook/               # Webhook DTOs (if applicable)
├── mapper/                    # MapStruct mappers (if applicable)
├── provider/                  # Provider abstraction (if applicable)
└── webhook/                   # Webhook security utilities (if applicable)
```

## Layer Responsibilities

### Controller Layer (`controller/v1/`)
- HTTP request/response handling
- Input validation and sanitization
- Response formatting
- Error handling at API level
- **Do not** contain business logic

### Service Layer (`service/`)
- Business logic implementation
- Orchestration of multiple operations
- Transaction management
- Complex business rules
- Integration with external services

### Repository Layer (`repository/`)
- Data access and persistence
- Database queries and operations
- Entity management
- Data mapping between entities and DTOs

### Entity Layer (`entity/`)
- JPA/Hibernate entities
- Domain objects
- Entity relationships
- Database mapping annotations

### DTO Layer (`dto/`)
- Request/Response objects
- API contract definitions
- Data transformation objects
- Validation annotations

## Development Guidelines

### Package Naming
- Use lowercase with hyphens for feature names: `user-management`
- Use versioned packages for APIs: `controller.v1`
- Follow Java package naming conventions

### Class Naming
- Controllers: `{Feature}Controller.java`
- Services: `{Feature}Service.java` (interface)
- Service Impl: `{Feature}ServiceImpl.java` or `{Provider}{Feature}Service.java`
- Repositories: `{Feature}Repository.java`
- DTOs: `{Feature}{Type}Dto.java` (e.g., `UserRequestDto`, `ProductResponseDto`)

### Dependency Injection
- Use constructor injection for required dependencies
- Use Spring's `@Service`, `@Repository`, `@Component` annotations
- Prefer interfaces over implementations for service dependencies

### Exception Handling
- Use `@RestControllerAdvice` for global exception handling
- **Feature-specific exceptions** should be created in `features/{feature}/exception/`
  - Example: `features/payment/exception/PaymentNotFoundException.java`
  - Example: `features/user/exception/UserNotFoundException.java`
- **Shared exceptions** (used across multiple features) should be in `common/exception/`
  - `ProviderException` - Used by payment and subscription providers
  - `ValidationException` - Used across features for validation errors
  - `GlobalExceptionHandler` - Global exception handler for all features
- Return appropriate HTTP status codes

### Testing Structure
```
src/test/java/com/hafizbahtiar/spring/
├── features/{feature-name}/
│   ├── controller/
│   ├── service/
│   └── repository/
└── common/
```

## Code Quality Standards

### Code Style
- Follow Google Java Style Guide
- Use meaningful variable and method names
- Add JavaDoc for public APIs
- Keep methods small and focused

### Best Practices
- Single Responsibility Principle
- Dependency Inversion Principle
- Interface Segregation Principle
- DRY (Don't Repeat Yourself)

### Security Considerations
- Validate all inputs
- Use parameterized queries
- Implement proper authentication/authorization
- Sanitize data before processing

## Dependencies

### Database Dependencies
- **PostgreSQL**: Primary database for business data
  - `spring-boot-starter-data-jpa` - JPA/Hibernate support
  - `postgresql` - PostgreSQL JDBC driver
- **MongoDB**: Secondary database for logs and analytics
  - `spring-boot-starter-data-mongodb` - MongoDB support
- **H2**: In-memory database for testing/development
  - `h2` - H2 database driver

### Web and Validation
- `spring-boot-starter-webmvc` - Spring MVC and REST support
- `spring-boot-starter-validation` - Bean validation support

## Build and Deployment

### Building the Application
```bash
./mvnw clean compile
./mvnw package
./mvnw spring-boot:run
```

### Configuration Profiles
- `default`: Development environment (uses application.properties)
- `development`: Local development with debug logging
- `production`: Production environment with optimized settings
- `test`: Testing environment with H2 database

#### Profile Files
- `application.properties` - Default/base configuration
- `application-development.properties` - Development-specific settings
- `application-production.properties` - Production optimizations
- `application-test.properties` - Test environment settings

#### Using Profiles
```bash
# Activate specific profile
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=production"

# Multiple profiles (production takes precedence)
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=development,production"
```

## Contributing

1. Create feature branch from `main`
2. Follow the established directory structure
3. Add tests for new functionality
4. Update documentation as needed
5. Create pull request with clear description

## Maintenance

- Regularly update dependencies
- Monitor code coverage
- Review and refactor legacy code
- Update documentation for API changes
