# Spring Boot Features-First Architecture

This project follows a **features-first** (or feature-driven) architecture pattern, where code is organized by business features rather than technical layers.

## ✅ Completed Features Summary

### Core Infrastructure
- ✅ **Infrastructure & Database Setup** - PostgreSQL (primary) + MongoDB (secondary) configured
- ✅ **Core Entities** - User, Payment, Subscription, Portfolio entities complete
- ✅ **Business Logic** - UserService, AuthService, PaymentService, SubscriptionService complete
- ✅ **REST API** - UserController, AuthController, PaymentController, SubscriptionController complete
- ✅ **CORS Configuration** - Frontend can now access backend
- ✅ **Exception Handling Infrastructure** - Custom exceptions and GlobalExceptionHandler
- ✅ **API Response Standardization** - ApiResponse wrapper and ResponseUtils

### Authentication & Security
- ✅ **Two-Token Authentication Strategy** - Access token (15 min) + Refresh token (7 days) implemented
- ✅ **Role Management** - OWNER and ADMIN roles with uniqueness validation
- ✅ **Password Reset Feature** - Complete with email integration
- ✅ **Security Logs API** - Authentication log retrieval endpoints

### Features
- ✅ **Payment Feature** - Stripe integration complete
- ✅ **Subscription Feature** - Dynamic plans, Stripe integration complete
- ✅ **Portfolio Feature** - All endpoints, services, logging complete
- ✅ **Admin Module** - Health, Metrics, Queue, Cron Jobs endpoints complete
- ✅ **Settings & Preferences Feature** - User Preferences, Notification Preferences, Currency Preferences, Session Management, Account Management, Profile Update complete
- ✅ **IP Geolocation Feature** - IPLocalize.com integration, session enrichment, IP lookup module
- ✅ **Socket.IO Real-Time Monitoring** - WebSocket server, JWT authentication, real-time health/metrics updates
- ✅ **Role-Based Access Control (RBAC) System** - Dynamic permission groups, fine-grained permissions, permission registry management, bulk operations, import/export, search and filtering

### Supporting Services
- ✅ **Email Service** - Password reset and email verification emails
- ✅ **User Activity API** - Activity logging and retrieval endpoints
- ✅ **MongoDB Logging** - Complete for Auth, User, Payment, Subscription, Portfolio, and IP Address features

## Directory Structure

```
src/main/java/com/hafizbahtiar/spring/
├── Application.java              # Main Spring Boot application class
├── config/                       # Global configuration classes
├── security/                     # Security-related classes (authentication, authorization)
├── common/                       # Shared utilities and common code
│   ├── dto/                      # Common Data Transfer Objects
│   ├── exception/                # Global exception handler and shared exceptions
│   │   ├── GlobalExceptionHandler.java  # Global exception handler
│   │   ├── ProviderException.java       # Shared provider exceptions
│   │   └── ValidationException.java     # Shared validation exceptions
│   └── util/                     # Utility classes and helper methods
└── features/                     # Business features (organized by domain)
    ├── user/                     # User management feature ✅
    │   ├── config/               # Feature-specific configuration
    │   ├── controller/v1/         # REST API controllers
    │   ├── service/              # Business logic services
    │   ├── repository/           # Data access layer
    │   ├── entity/               # JPA entity classes
    │   ├── exception/            # Feature-specific exceptions
    │   ├── dto/                  # Data Transfer Objects
    │   └── mapper/               # MapStruct mappers
    ├── auth/                     # Authentication feature ✅
    │   ├── controller/v1/         # Auth REST endpoints
    │   ├── service/              # Authentication services
    │   ├── exception/            # Feature-specific exceptions
    │   └── dto/                  # Auth DTOs
    ├── payment/                  # Payment feature ✅
    │   ├── controller/v1/         # Payment REST endpoints
    │   ├── service/              # Payment services
    │   ├── repository/           # Payment repositories
    │   ├── entity/               # Payment entities
    │   ├── exception/            # Feature-specific exceptions
    │   ├── provider/             # Payment provider abstraction
    │   ├── dto/                  # Payment DTOs
    │   └── mapper/               # Payment mappers
    └── subscription/             # Subscription feature ✅
        ├── controller/v1/         # Subscription REST endpoints
        ├── service/              # Subscription services
        ├── repository/           # Subscription repositories
        ├── entity/               # Subscription entities
        ├── exception/            # Feature-specific exceptions
        ├── provider/             # Subscription provider abstraction
        ├── dto/                  # Subscription DTOs
        └── mapper/               # Subscription mappers
```

## Documentation

Project documentation is located in the `docs/` directory:

- `docs/api-structure.md` - API endpoints and REST documentation
- `docs/codebase-structure.md` - Detailed codebase organization and guidelines
- `docs/db-structure.md` - Database architecture and schema design
- `docs/webhook-structure.md` - Webhook implementation and security

## Benefits of Features-First Architecture

1. **Domain-Driven Design (DDD)**: Each feature represents a business domain
2. **Easier Maintenance**: Related code is co-located
3. **Better Testability**: Features can be tested independently
4. **Scalability**: New features can be added without affecting existing ones
5. **Team Organization**: Teams can work on features independently

## Guidelines

### Feature Structure

Each feature should contain:

- **controller/**: REST endpoints and HTTP request handling
  - **v1/**: API version 1 controllers (for versioning support)
- **service/**: Business logic and orchestration
  - **impl/**: Service implementation classes (optional)
- **repository/**: Data access and persistence logic
- **model/**: Entity classes and domain objects
- **dto/**: Data transfer objects for API communication
- **config/**: Feature-specific configuration (optional)

### API Versioning

- Use semantic versioning for API endpoints
- Place versioned controllers in `controller/v1/`, `controller/v2/`, etc.
- Example: `UserController` → `features/user/controller/v1/UserController.java`

### Naming Conventions

- Use descriptive names for features (e.g., `user`, `product`, `order`, `payment`)
- Follow Java naming conventions for packages and classes
- Use consistent naming across features
- Service interfaces: `FeatureService.java`
- Service implementations: `FeatureServiceImpl.java` or `SpecificFeatureService.java`

### Common/Shared Code

- Place reusable utilities in `common/util/`
- Global DTOs in `common/dto/`
- **Shared exceptions** (used across multiple features) in `common/exception/`
  - `ProviderException` - Used by payment and subscription providers
  - `ValidationException` - Used across features for validation errors
  - `GlobalExceptionHandler` - Global exception handler for all features
- **Feature-specific exceptions** should be in `features/{feature}/exception/`
  - Example: `features/payment/exception/PaymentNotFoundException.java`
  - Example: `features/user/exception/UserNotFoundException.java`
- Security configurations in `security/`
- Global configurations in `config/`

## Current Features

### ✅ User Management Feature (COMPLETED)

Complete user management system with CRUD operations, profile management, and validation.

**Components:**

- ✅ User Entity with Lombok and business methods
- ✅ UserRepository with custom queries
- ✅ UserMapper (MapStruct) for DTO conversions
- ✅ UserService with BCrypt password encryption
- ✅ UserController with REST API endpoints
- ✅ DTOs: Registration, Update, Response, Profile

**API Endpoints:**

- `POST /api/v1/users` - Register user
- `GET /api/v1/users` - Get all users
- `GET /api/v1/users/{id}` - Get user by ID
- `PUT /api/v1/users/{id}` - Update user
- `DELETE /api/v1/users/{id}` - Delete user
- And more...

**Documentation:** See [User Feature README](features/user/README.md)

### ✅ Authentication Feature (COMPLETED)

JWT-based authentication system with separate auth feature module.

**Components:**

- ✅ AuthService with login/logout operations
- ✅ AuthController with authentication endpoints
- ✅ JWT token generation and validation
- ✅ JWT authentication filter for Spring Security
- ✅ Login/me/validate endpoints
- ✅ Feature-specific exceptions: InvalidCredentialsException

**API Endpoints:**

- `POST /api/v1/auth/login` - Authenticate and get JWT token
- `GET /api/v1/auth/me` - Get current authenticated user
- `POST /api/v1/auth/validate` - Validate JWT token

**Documentation:** See [Auth Feature README](features/auth/README.md)

### Security Components

**JWT Utilities:**

- ✅ JwtTokenProvider - Token generation and validation
- ✅ JwtAuthenticationFilter - Spring Security filter

**Documentation:** See [Security Package README](security/README.md)

### ✅ Payment Feature (COMPLETED)

Comprehensive payment processing system with multi-provider support (Stripe, extensible for PayPal).

**Components:**

- ✅ Payment and PaymentMethod entities
- ✅ PaymentProviderService abstraction layer
- ✅ StripePaymentProvider implementation
- ✅ PaymentService and PaymentMethodService
- ✅ PaymentController and PaymentMethodController
- ✅ MongoDB logging for payment events
- ✅ Feature-specific exceptions: PaymentNotFoundException, PaymentProcessingException, RefundException, PaymentMethodException

**API Endpoints:**

- `POST /api/v1/payments` - Process a payment
- `POST /api/v1/payments/{id}/confirm` - Confirm a payment
- `GET /api/v1/payments` - List payments
- `POST /api/v1/payments/{id}/refund` - Process a refund
- `POST /api/v1/payments/methods` - Add payment method
- `GET /api/v1/payments/methods` - List payment methods
- And more...

**Documentation:** See [Payment Feature README](features/payment/README.md)

### ✅ Subscription Feature (COMPLETED)

Dynamic subscription management system with admin-configurable plans and recurring billing.

**Components:**

- ✅ Subscription and SubscriptionPlan entities
- ✅ SubscriptionProviderService abstraction layer
- ✅ StripeSubscriptionProvider implementation
- ✅ SubscriptionService and SubscriptionPlanService
- ✅ SubscriptionController and SubscriptionPlanController
- ✅ MongoDB logging for subscription events
- ✅ Feature-specific exceptions: SubscriptionNotFoundException, SubscriptionException, SubscriptionCancellationException

**API Endpoints:**

- `POST /api/v1/subscriptions` - Create a subscription
- `GET /api/v1/subscriptions` - List subscriptions
- `POST /api/v1/subscriptions/{id}/cancel` - Cancel subscription
- `POST /api/v1/subscription-plans` - Create plan (admin)
- `GET /api/v1/subscription-plans` - List active plans
- And more...

**Documentation:** See [Subscription Feature README](features/subscription/README.md)

### ✅ IP Geolocation Feature (COMPLETED)

IP geolocation service with multi-provider support using adapter pattern.

**Components:**

- ✅ IPGeolocationService with adapter pattern for multiple providers
- ✅ IPLocalizeAdapter implementation (IPLocalize.com integration)
- ✅ IPAddressService for owner/admin IP lookup functionality
- ✅ IPAddressController with REST API endpoints
- ✅ MongoDB logging for IP lookup events
- ✅ Session enrichment with geolocation data (async, non-blocking)

**API Endpoints:**

- `POST /api/v1/ip-address/lookup` - Lookup IP address geolocation (owner/admin only)
- `GET /api/v1/ip-address/lookup/{ipAddress}` - Get geolocation for specific IP (owner/admin only)
- `GET /api/v1/ip-address/sessions/{sessionId}` - Get geolocation for session IP (owner/admin only)

**Features:**

- Automatic session enrichment with IP geolocation data
- Support for multiple IP geolocation providers (adapter pattern)
- Private IP address detection and skipping
- Async geolocation lookup to avoid blocking session creation

**Documentation:** See `docs/ip-address/iplocalize.md` and `docs/ip-address/structure.md`

### ✅ Security Logs API (COMPLETED)

Authentication log retrieval system for users and administrators.

**Components:**

- ✅ AuthLogService for querying authentication logs
- ✅ AuthLogResponse DTO for log data
- ✅ AuthController endpoints for log retrieval
- ✅ MongoDB integration with AuthLogRepository

**API Endpoints:**

- `GET /api/v1/auth/logs?limit=10` - Get current user's auth logs
- `GET /api/v1/auth/logs/security?limit=20` - Get security events (admin/owner only)
- `GET /api/v1/auth/logs/failed-attempts?identifier={email}&limit=10` - Get failed login attempts (admin/owner only)

**Features:**

- Users can only access their own authentication logs
- OWNER/ADMIN can access all security logs
- Supports filtering by event type, user, and date range
- Returns login attempts, token validations, and security events

### ✅ Socket.IO Real-Time Monitoring (COMPLETED)

Real-time WebSocket monitoring system for system health and metrics.

**Components:**

- ✅ SocketIOServer configuration (port 9092)
- ✅ MonitoringSocketHandler for event handling
- ✅ JWT authentication for WebSocket connections
- ✅ Integration with AdminHealthService and AdminMetricsService

**Features:**

- Real-time system health and metrics updates
- JWT-based authentication (OWNER/ADMIN only)
- Periodic broadcasts every 30 seconds
- Manual refresh support via `monitoring:refresh` event
- Error notifications via `monitoring:error` event
- Graceful client disconnection handling

**Configuration:**

- Server port: 9092 (configurable via `socketio.port`)
- Namespace: `/monitoring`
- Broadcast interval: 30 seconds
- CORS: Enabled for development

**Files:**

- `config/SocketIOConfig.java` - Socket.IO server configuration
- `features/admin/socket/MonitoringSocketHandler.java` - Event handlers

### Future Features

1. **Product Management** - Product catalog and inventory
2. **Order Management** - Order processing and tracking
3. **Webhook Integration** - Stripe and PayPal webhook handlers
4. **Scheduled Jobs** - Automated subscription renewal processing

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

## Database Configuration

### PostgreSQL Setup

```bash
# Create database
createdb console

# Create user
createuser console --createdb --login --pwprompt
# Enter password: console_2025!

# Grant permissions
psql -c "GRANT ALL PRIVILEGES ON DATABASE console TO console;"
```

### MongoDB Setup

```bash
# Start MongoDB (if using local installation)
mongosh

# Create database and user (optional)
use console
db.createUser({
  user: "console",
  pwd: "console_2025!",
  roles: ["readWrite"]
})
```

### Database Migrations

The application uses Flyway for database schema versioning and migrations.

#### Migration Files Location

```
src/main/resources/db/migration/
├── V1.0.0__Initial_schema.sql      # Core tables and initial data
├── V1.0.1__Add_indexes.sql         # Performance indexes
└── V1.0.2__Add_constraints.sql     # Business rules and constraints
```

#### Running Migrations

```bash
# Automatic migration on startup (configured)
./mvnw spring-boot:run

# Manual migration
./mvnw flyway:migrate

# Check migration status
./mvnw flyway:info
```

### Environment Configuration

The application supports environment-based configuration through `.env` files or system environment variables.

#### Local Development Setup

1. Copy `env-example.txt` to `.env` in the project root
2. Update the values for your local environment
3. The application will automatically load `.env` file properties

#### Environment Variables (Production)

```bash
# PostgreSQL
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/console
export SPRING_DATASOURCE_USERNAME=console
export SPRING_DATASOURCE_PASSWORD=your_secure_password

# MongoDB
export SPRING_DATA_MONGODB_URI=mongodb://localhost:27017/console
export SPRING_DATA_MONGODB_USERNAME=console
export SPRING_DATA_MONGODB_PASSWORD=your_secure_password
```

#### Configuration Profiles

- `development`: Local development with debug logging
- `production`: Production environment with optimized settings
- `test`: Testing environment with H2 database

#### Using Profiles

```bash
# Development
./mvnw spring-boot:run --spring.profiles.active=development

# Production
./mvnw spring-boot:run --spring.profiles.active=production

# Test
./mvnw test --spring.profiles.active=test
```

#### Profile-Specific Files

- `application.properties` - Default configuration
- `application-development.properties` - Development overrides
- `application-production.properties` - Production overrides
- `application-test.properties` - Test overrides

## Getting Started

1. Create a new feature directory under `features/`
2. Implement the required layers (controller/v1, service, repository, model)
3. Add feature-specific DTOs and configuration as needed
4. Update this README when adding new features
5. Update API documentation in `docs/api-structure.md`
