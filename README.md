# Spring Boot SaaS Application

> **A showcase of modern Spring Boot development practices** - Building a production-ready SaaS application with dual-database architecture, payment gateway integrations, and comprehensive security.

## 🎯 Project Overview

This project demonstrates advanced Spring Boot development skills and serves as a foundation for a future **Software-as-a-Service (SaaS)** platform. The application implements a **Features-First Architecture** with dual-database support, enabling scalable and maintainable code organization.

### Key Highlights

- ✅ **Features-First Architecture** - Organized by business domains, not technical layers
- ✅ **Dual-Database Architecture** - PostgreSQL (primary) + MongoDB (secondary)
- ✅ **JWT Authentication & Authorization** - Secure API with role-based access control
- ✅ **Payment Gateway Integration** - PayPal and Stripe support for SaaS subscriptions
- ✅ **RESTful API Design** - Consistent response formats and error handling
- ✅ **Production-Ready** - Exception handling, validation, logging, and security

---

## 🏗️ Architecture

### Features-First Architecture

Code is organized by **business features** rather than technical layers, promoting:
- **Better maintainability** - Related code stays together
- **Easier scaling** - Features can be developed independently
- **Clear boundaries** - Each feature is self-contained

```
features/
├── user/          # User management
├── auth/          # Authentication & authorization
├── payment/       # Payment processing (PayPal, Stripe)
├── subscription/  # SaaS subscription management
└── ...
```

### Dual-Database Design

#### PostgreSQL (Primary Database)
- **Purpose**: Core business data, transactions, relationships
- **Stores**: Users, products, orders, payments, subscriptions
- **Technology**: Spring Data JPA + Hibernate
- **Characteristics**: ACID compliance, relational integrity

#### MongoDB (Secondary Database)
- **Purpose**: Logs, audit trails, analytics, unstructured data
- **Stores**: Application logs, webhook payloads, user activity, analytics
- **Technology**: Spring Data MongoDB
- **Characteristics**: Flexible schemas, high write throughput

---

## 🚀 Features Implemented

### ✅ User Management (`features/user/`)
- User registration with email/username validation
- Profile management (CRUD operations)
- Email verification
- User activation/deactivation
- Role-based access (USER, ADMIN)

**Endpoints:**
- `POST /api/v1/users` - Register new user
- `GET /api/v1/users/{id}` - Get user by ID
- `PUT /api/v1/users/{id}` - Update user profile
- `DELETE /api/v1/users/{id}` - Deactivate user
- `GET /api/v1/users` - List all users (Admin only)

### ✅ Authentication (`features/auth/`)
- JWT-based authentication
- Login with email/username
- Token validation
- Current user retrieval
- Secure password hashing (BCrypt)

**Endpoints:**
- `POST /api/v1/auth/login` - Authenticate and get JWT token
- `GET /api/v1/auth/me` - Get current authenticated user
- `POST /api/v1/auth/validate` - Validate JWT token

### ✅ Authorization & Security
- **Role-Based Access Control (RBAC)** - USER and ADMIN roles
- **Method-Level Security** - `@PreAuthorize` annotations
- **Resource Ownership** - Users can only modify their own resources
- **JWT Token Security** - Signed tokens with expiration

**Security Features:**
- Password encryption (BCrypt, strength 12)
- JWT token generation and validation
- Spring Security integration
- Custom `UserPrincipal` for security context

### ✅ API Infrastructure
- **Consistent Response Format** - `ApiResponse<T>` wrapper
- **Error Handling** - Global exception handler with custom exceptions
- **Input Validation** - Bean validation with `@Valid`
- **Standardized Errors** - `ApiErrorResponse` with proper HTTP status codes

---

## 🔄 Features in Progress

### 🚧 Payment Processing (`features/payment/`)
**Status**: Planning & Design Phase

**Planned Features:**
- Multi-gateway support (PayPal, Stripe)
- Payment processing abstraction
- Payment status tracking
- Refund processing
- Payment history

**Integration Points:**
- PayPal REST API integration
- Stripe API integration
- Webhook handling for payment events
- MongoDB logging for payment audit trails

### 🚧 Subscription Management (`features/subscription/`)
**Status**: Planning Phase

**Planned Features:**
- Subscription plans (Basic, Pro, Enterprise)
- Subscription lifecycle management
- Billing cycle management
- Subscription upgrades/downgrades
- Payment method management

---

## 🛠️ Technology Stack

### Core Framework
- **Spring Boot 4.0.0** - Application framework
- **Java 17** - Programming language
- **Maven** - Build tool

### Databases
- **PostgreSQL** - Primary relational database
- **MongoDB** - Secondary document database
- **Hibernate DDL Auto** - Schema management (update mode)

### Security
- **Spring Security** - Authentication & authorization
- **JWT (jjwt 0.12.3)** - Token-based authentication
- **BCrypt** - Password hashing

### Data Access
- **Spring Data JPA** - PostgreSQL access
- **Spring Data MongoDB** - MongoDB access
- **Hibernate** - JPA implementation with auto DDL (development)

### Utilities
- **Lombok** - Reduce boilerplate code
- **MapStruct** - Type-safe bean mapping
- **Bean Validation** - Input validation

### Payment Gateways (Planned)
- **PayPal SDK** - PayPal integration
- **Stripe SDK** - Stripe integration

---

## 📁 Project Structure

```
src/main/java/com/hafizbahtiar/spring/
├── config/                    # Global configuration
│   ├── SecurityConfig.java    # Spring Security configuration
│   └── CorsConfig.java        # CORS configuration (planned)
│
├── security/                  # Security components
│   ├── JwtTokenProvider.java      # JWT token utilities
│   └── JwtAuthenticationFilter.java  # JWT filter
│
├── common/                    # Shared components
│   ├── dto/                   # Common DTOs
│   │   ├── ApiResponse.java   # Success response wrapper
│   │   └── ApiErrorResponse.java  # Error response format
│   ├── exception/             # Custom exceptions
│   │   ├── UserNotFoundException.java
│   │   ├── UserAlreadyExistsException.java
│   │   ├── InvalidCredentialsException.java
│   │   └── GlobalExceptionHandler.java
│   ├── security/              # Security utilities
│   │   ├── Role.java          # Role enum
│   │   ├── UserPrincipal.java # User principal
│   │   ├── SecurityUtils.java # Security utilities
│   │   └── SecurityService.java # Security service bean
│   └── util/                  # Utility classes
│       └── ResponseUtils.java # Response helpers
│
└── features/                  # Business features
    ├── user/                  # User management
    │   ├── entity/            # User entity
    │   ├── repository/        # User repository
    │   ├── service/           # User service
    │   ├── controller/v1/     # User API endpoints
    │   ├── dto/               # User DTOs
    │   └── mapper/            # MapStruct mapper
    │
    ├── auth/                  # Authentication
    │   ├── service/           # Auth service
    │   ├── controller/v1/     # Auth API endpoints
    │   └── dto/               # Auth DTOs
    │
    ├── payment/               # Payment processing (planned)
    │   ├── entity/            # Payment entity
    │   ├── service/           # Payment service
    │   ├── controller/v1/     # Payment API endpoints
    │   ├── webhook/           # Webhook handlers
    │   └── provider/           # Gateway providers
    │       ├── paypal/        # PayPal integration
    │       └── stripe/        # Stripe integration
    │
    └── subscription/          # Subscription management (planned)
        ├── entity/            # Subscription entity
        ├── service/           # Subscription service
        └── controller/v1/     # Subscription API endpoints
```

---

## 🔐 Security Features

### Authentication
- **JWT Tokens** - Stateless authentication
- **Password Hashing** - BCrypt with strength 12
- **Token Expiration** - Configurable (default: 24 hours)

### Authorization
- **Role-Based Access Control (RBAC)**
  - `USER` - Standard user permissions
  - `ADMIN` - Full system access
- **Method-Level Security** - `@PreAuthorize` annotations
- **Resource Ownership** - Users can only modify their own resources

### Security Best Practices
- ✅ CSRF protection disabled (stateless API)
- ✅ Password never stored in plain text
- ✅ JWT tokens signed with secret key
- ✅ Input validation on all endpoints
- ✅ SQL injection prevention (JPA parameterized queries)
- ✅ XSS protection (input sanitization)

---

## 📚 Documentation

Comprehensive documentation is available in the `/docs` directory:

- **[Database Structure](docs/db-structure.md)** - Database schema and design
- **[Codebase Structure](docs/codebase-structure.md)** - Architecture and organization
- **[Authentication Implementation](docs/authentication-implementation.md)** - Auth flow and JWT
- **[Authorization & RBAC](docs/authorization-rbac-implementation.md)** - Security and access control
- **[API Structure](docs/api-structure.md)** - API design and endpoints
- **[Webhook Structure](docs/webhook-structure.md)** - Webhook implementation

---

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- MongoDB 4.4+ (optional for development)

### Schema Management

**Current Approach (Development)**: The application uses **Hibernate's automatic schema management** (`ddl-auto=update`). Tables are automatically created and updated based on your `@Entity` class definitions. This simplifies development but is not recommended for production.

**For Production**: Consider migrating to Flyway for better control, versioning, and rollback capabilities. Migration files are available in `src/main/resources/db/migration/` but are currently disabled.

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd spring
   ```

2. **Set up PostgreSQL database**
   ```sql
   CREATE DATABASE console;
   CREATE USER console WITH ENCRYPTED PASSWORD 'your_password';
   GRANT ALL PRIVILEGES ON DATABASE console TO console;
   ```

3. **Configure application properties**
   ```properties
   # Database
   spring.datasource.url=jdbc:postgresql://localhost:5432/console
   spring.datasource.username=console
   spring.datasource.password=your_password
   
   # Hibernate will automatically create/update schema
   spring.jpa.hibernate.ddl-auto=update
   
   # JWT
   jwt.secret=your-256-bit-secret-key-change-in-production
   jwt.expiration=86400000
   ```

4. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```
   
   **Note**: Hibernate will automatically create database tables based on your `@Entity` classes on first run. No manual migration scripts needed.

5. **Access the API**
   - API Base URL: `http://localhost:8080/api/v1`
   - Health Check: `http://localhost:8080/actuator/health`

---

## 📝 API Examples

### Register a User
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "username": "johndoe",
    "password": "SecurePassword123!"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "user@example.com",
    "password": "SecurePassword123!"
  }'
```

### Get Current User (Authenticated)
```bash
curl -X GET http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer <your-jwt-token>"
```

### Update User Profile (Own Resource)
```bash
curl -X PUT http://localhost:8080/api/v1/users/123 \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe"
  }'
```

---

## 🧪 Testing

### Run Tests
```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=UserServiceTest

# Run with coverage
./mvnw test jacoco:report
```

### Test Coverage
- Target: >80% code coverage
- Tools: JaCoCo

---

## 🎓 Learning Outcomes

This project demonstrates proficiency in:

### Spring Boot Ecosystem
- ✅ Spring Boot application development
- ✅ Spring Data JPA and MongoDB
- ✅ Spring Security integration
- ✅ RESTful API design

### Architecture & Design
- ✅ Features-First Architecture
- ✅ Dual-database design patterns
- ✅ Service layer abstraction
- ✅ DTO pattern implementation

### Security
- ✅ JWT authentication
- ✅ Role-based access control
- ✅ Password encryption
- ✅ Method-level security

### Best Practices
- ✅ Exception handling
- ✅ Input validation
- ✅ API response standardization
- ✅ Code organization

### Payment Integration (In Progress)
- 🔄 Multi-gateway payment processing
- 🔄 Webhook handling
- 🔄 Payment status management

---

## 🗺️ Roadmap

### Phase 1: Core Features ✅
- [x] User management
- [x] Authentication & authorization
- [x] API infrastructure

### Phase 2: Payment Integration 🚧
- [ ] PayPal integration
- [ ] Stripe integration
- [ ] Payment processing service
- [ ] Webhook handling

### Phase 3: Subscription Management 🚧
- [ ] Subscription plans
- [ ] Billing cycles
- [ ] Subscription lifecycle

### Phase 4: Production Ready
- [ ] Comprehensive testing
- [ ] Performance optimization
- [ ] Monitoring & logging
- [ ] Deployment configuration

---

## 📄 License

This project is for educational and showcase purposes.

---

## 👤 Author

**Hafiz Bahtiar**
- Showcasing Spring Boot development skills
- Building foundation for SaaS platform
- Learning modern backend development practices

---

## 🙏 Acknowledgments

- Spring Boot team for excellent framework
- Community contributors for best practices
- Payment gateway providers (PayPal, Stripe) for APIs

---

**Last Updated**: December 2025  
**Status**: Active Development  
**Version**: 0.0.1-SNAPSHOT

