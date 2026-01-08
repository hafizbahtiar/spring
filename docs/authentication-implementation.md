# Authentication Implementation Guide

This document outlines how to implement user authentication as a separate auth service with JWT tokens.

## Architecture Overview

The authentication will be implemented as a **separate `auth` feature** following the features-first architecture:
- **`features/auth/`** - Complete authentication feature module
- Handles login/logout operations
- Generates and validates JWT tokens
- Integrates with Spring Security
- Uses the existing `features/user/` feature for user data access

## Components Needed

### 1. Dependencies
Add JWT library to `pom.xml`:
```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
```

### 2. Auth Feature Structure (`features/auth/`)
- **DTOs** (`dto/`)
  - `LoginRequest.java` - Email/username and password
  - `LoginResponse.java` - JWT token and user info
  
- **Service Layer** (`service/`)
  - `AuthService.java` - Interface for authentication operations
  - `AuthServiceImpl.java` - Implementation with JWT logic
  
- **Controller** (`controller/v1/`)
  - `AuthController.java` - Login, logout, and current user endpoints

### 3. JWT Utilities (in `security/`)
- `JwtTokenProvider.java` - JWT token generation and validation
- `JwtAuthenticationFilter.java` - Filter to intercept requests and validate tokens

### 6. Security Configuration Updates
- Update `SecurityConfig.java` to integrate JWT filter
- Configure protected/public endpoints

## Implementation Steps

### Step 1: Add JWT Dependencies
Add the JWT dependencies to `pom.xml` (see above).

### Step 2: Create JWT Token Provider
Create `security/JwtTokenProvider.java`:
- Generate JWT tokens with user claims (id, email, username, role)
- Validate JWT tokens
- Extract user information from tokens
- Handle token expiration

### Step 3: Create Auth DTOs
- `LoginRequest`: identifier (email/username) + password
- `LoginResponse`: token + user info + expiration time

### Step 4: Create AuthService
- `authenticate()`: Validate credentials, return JWT token
- `validateToken()`: Validate JWT token
- `getCurrentUser()`: Extract user from token
- `logout()`: Invalidate token (optional, can use token blacklist)

### Step 5: Create JWT Authentication Filter
- Extends `OncePerRequestFilter`
- Intercepts requests, extracts JWT from Authorization header
- Validates token and sets authentication in SecurityContext

### Step 6: Create AuthController
- `POST /api/v1/auth/login` - Authenticate and return JWT
- `POST /api/v1/auth/logout` - Logout (optional)
- `GET /api/v1/auth/me` - Get current authenticated user

### Step 7: Update SecurityConfig
- Add JWT filter to filter chain
- Configure public endpoints (login, register)
- Protect other endpoints with JWT authentication

## Code Structure

```
src/main/java/com/hafizbahtiar/spring/
├── security/
│   ├── JwtTokenProvider.java          # JWT utilities (shared)
│   ├── JwtAuthenticationFilter.java   # JWT filter (shared)
│   └── SecurityConfig.java             # Updated config
└── features/
    ├── user/                           # User management feature
    │   ├── entity/User.java
    │   ├── repository/UserRepository.java
    │   └── service/UserService.java
    └── auth/                           # Authentication feature (NEW)
        ├── dto/
        │   ├── LoginRequest.java
        │   └── LoginResponse.java
        ├── service/
        │   ├── AuthService.java
        │   └── AuthServiceImpl.java
        └── controller/v1/
            └── AuthController.java
```

## Key Implementation Details

### JWT Token Structure
```json
{
  "sub": "user@example.com",
  "userId": 1,
  "username": "johndoe",
  "role": "USER",
  "iat": 1234567890,
  "exp": 1234654290
}
```

### Authentication Flow
1. User sends login request with credentials
2. AuthService validates credentials against UserRepository
3. If valid, generate JWT token with user claims
4. Return token to client
5. Client includes token in Authorization header for subsequent requests
6. JwtAuthenticationFilter validates token on each request
7. Spring Security sets authentication context

### Security Considerations
- Use strong JWT secret (from environment variable)
- Set appropriate token expiration (e.g., 24 hours)
- Store password hash, never plain text
- Use HTTPS in production
- Consider refresh tokens for longer sessions
- Implement token blacklist for logout (optional)

## Integration with Existing Code

- **User Feature** (`features/user/`): AuthService will use UserRepository from user feature
- **PasswordEncoder**: Already configured in `features/user/config/PasswordEncoderConfig.java`, reuse for password validation
- **User Entity**: Already has all necessary fields in `features/user/entity/User.java`
- **SecurityConfig**: Update to add JWT filter

## Feature Separation

### `features/user/` - User Management
- User CRUD operations
- Profile management
- User data access

### `features/auth/` - Authentication
- Login/logout operations
- JWT token management
- Authentication logic
- Uses `features/user/` for user data access

## Testing Strategy

1. Unit tests for JwtTokenProvider
2. Unit tests for AuthService
3. Integration tests for AuthController
4. Test token expiration
5. Test invalid credentials
6. Test protected endpoints with/without token

