# User Feature

This document describes the User Management feature module, which handles user data, profiles, and user-related operations.

## Overview

The User feature provides comprehensive user management capabilities including:

- User registration and profile management
- User data persistence
- User information retrieval and updates
- Email and username validation
- User status management (active/inactive, email verification)

## Architecture

The User feature follows the features-first architecture pattern and is organized into clear layers:

```
features/user/
├── entity/              # Domain model
│   └── User.java       # JPA entity with business logic
├── exception/          # Feature-specific exceptions
│   ├── UserNotFoundException.java
│   └── UserAlreadyExistsException.java
├── repository/         # Data access layer
│   └── UserRepository.java
├── mapper/             # DTO conversions
│   └── UserMapper.java
├── dto/                # Data Transfer Objects
│   ├── UserRegistrationRequest.java
│   ├── UserUpdateRequest.java
│   ├── UserResponse.java
│   └── UserProfileResponse.java
├── service/            # Business logic
│   ├── UserService.java
│   └── UserServiceImpl.java
├── controller/v1/      # REST API endpoints
│   └── UserController.java
└── config/             # Feature-specific configuration
    └── PasswordEncoderConfig.java
```

## Components

### Entity Layer

#### User Entity (`entity/User.java`)

JPA entity representing a user in the system.

**Key Features:**

- Lombok annotations (@Getter, @Setter, @NoArgsConstructor)
- JPA annotations with proper indexing
- Business methods (getFullName(), isActive(), verifyEmail(), etc.)
- Custom equals(), hashCode(), and toString() methods
- Optimistic locking with @Version

**Fields:**

- `id` - Primary key
- `email` - Unique email address
- `username` - Unique username
- `passwordHash` - BCrypt hashed password
- `firstName`, `lastName` - User name fields
- `phone` - Phone number
- `role` - User role (USER, ADMIN, MODERATOR)
- `emailVerified` - Email verification status
- `active` - Account active status
- `createdAt`, `updatedAt`, `lastLoginAt` - Timestamps
- `version` - Optimistic locking version

**Business Methods:**

- `getFullName()` - Returns formatted full name
- `isActive()` - Checks if user is active
- `isEmailVerified()` - Checks email verification status
- `activate()`, `deactivate()` - Status management
- `verifyEmail()` - Mark email as verified
- `updateLastLogin()` - Update last login timestamp

### Repository Layer

#### UserRepository (`repository/UserRepository.java`)

Spring Data JPA repository with custom query methods.

**Key Methods:**

- `findByActiveTrue()` - Find all active users
- `findByRoleAndActiveTrue(String role)` - Find users by role
- `findByEmailIgnoreCase(String email)` - Case-insensitive email lookup
- `findByUsernameIgnoreCase(String username)` - Case-insensitive username lookup
- `findByEmailOrUsernameAndActive(String identifier)` - Login lookup
- `existsByEmailIgnoreCase(String email)` - Email existence check
- `existsByUsernameIgnoreCase(String username)` - Username existence check
- `existsByEmailAndIdNot(String email, Long id)` - Email uniqueness for updates
- `existsByUsernameAndIdNot(String username, Long id)` - Username uniqueness for updates
- `findByEmailVerifiedFalseAndActiveTrue()` - Find unverified users
- `countByRole(String role)` - Count users by role
- `findRecentlyRegistered(LocalDateTime since)` - Find recent registrations

### Mapper Layer

#### UserMapper (`mapper/UserMapper.java`)

MapStruct mapper for converting between entities and DTOs.

**Mapping Methods:**

- `toEntity(UserRegistrationRequest)` - Convert registration request to entity
- `updateEntityFromRequest(UserUpdateRequest, User)` - Update entity from request
- `toResponse(User)` - Convert entity to response DTO
- `toProfileResponse(User)` - Convert entity to profile response
- `toResponseList(List<User>)` - Convert list of entities
- `toSummary(User)` - Convert to summary response
- `toSummaryList(List<User>)` - Convert list to summaries

### DTO Layer

#### UserRegistrationRequest (`dto/UserRegistrationRequest.java`)

Request DTO for user registration.

**Validation:**

- Email: Required, valid format, max 255 characters
- Username: Required, 3-50 characters, alphanumeric with underscores/hyphens
- Password: Required, 8-100 characters, must contain uppercase, lowercase, number, and special character
- FirstName, LastName: Optional, max 100 characters
- Phone: Optional, valid phone format

#### UserUpdateRequest (`dto/UserUpdateRequest.java`)

Request DTO for updating user information.

**Fields:** Same as registration but password is not included (update separately if needed)

#### UserResponse (`dto/UserResponse.java`)

Response DTO containing user information.

**Fields:** All user fields except password hash

**Inner Class:** `Summary` - Lightweight user summary

#### UserProfileResponse (`dto/UserProfileResponse.java`)

Extended response DTO for user profile with additional fields.

**Additional Fields:**

- `profilePictureUrl` - Profile picture URL
- `bio` - User biography
- `timezone` - User timezone
- `language` - Preferred language

### Service Layer

#### UserService (`service/UserService.java`)

Interface defining user management operations.

**Methods:**

- `register(UserRegistrationRequest)` - Register new user
- `getById(Long)` - Get user by ID
- `getProfileById(Long)` - Get user profile
- `getAllUsers()` - Get all active users
- `updateUser(Long, UserUpdateRequest)` - Update user information
- `deleteUser(Long)` - Deactivate user
- `verifyEmail(Long)` - Verify user email
- `emailExists(String)` - Check email availability
- `usernameExists(String)` - Check username availability

#### UserServiceImpl (`service/UserServiceImpl.java`)

Implementation of user service with business logic.

**Features:**

- Transaction management (@Transactional)
- Password hashing with BCrypt
- Email/username uniqueness validation
- Comprehensive logging
- Error handling

### Controller Layer

#### UserController (`controller/v1/UserController.java`)

REST API controller for user operations.

**Endpoints:**

- `POST /api/v1/users` - Register new user
- `GET /api/v1/users` - Get all active users
- `GET /api/v1/users/{id}` - Get user by ID
- `GET /api/v1/users/{id}/profile` - Get user profile
- `PUT /api/v1/users/{id}` - Update user
- `DELETE /api/v1/users/{id}` - Delete (deactivate) user
- `POST /api/v1/users/{id}/verify` - Verify email
- `GET /api/v1/users/check/email` - Check email availability
- `GET /api/v1/users/check/username` - Check username availability

### Configuration

#### PasswordEncoderConfig (`config/PasswordEncoderConfig.java`)

Configuration for password encoding.

- BCryptPasswordEncoder bean
- Strength: 12
- Used by both UserService and AuthService

## Usage Examples

### Register User

```bash
POST /api/v1/users
{
  "email": "user@example.com",
  "username": "johndoe",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890"
}
```

### Get User

```bash
GET /api/v1/users/1
```

### Update User

```bash
PUT /api/v1/users/1
{
  "email": "newemail@example.com",
  "firstName": "Jane",
  "lastName": "Smith"
}
```

### Check Email Availability

```bash
GET /api/v1/users/check/email?email=test@example.com
```

## Integration with Other Features

### Authentication Feature

- UserRepository is used by AuthService for user lookup
- PasswordEncoder is shared between UserService and AuthService
- User entity is referenced by authentication logic

## Error Handling

### Custom Exceptions

All user-related exceptions are located in `features/user/exception/`:

- `UserNotFoundException` - User not found
- `UserAlreadyExistsException` - User already exists (email or username conflict)

**Common Exceptions** (shared across features, located in `common/exception/`):

- `ValidationException` - Used for validation errors

All exceptions are handled by `GlobalExceptionHandler` and return appropriate HTTP status codes.

## Security Considerations

- Passwords are never stored in plain text (BCrypt hashing)
- Email and username are case-insensitive for lookups
- User deactivation instead of deletion (soft delete)
- Optimistic locking prevents concurrent update conflicts
- Input validation on all endpoints

## Database Schema

The User entity maps to the `users` table with:

- Proper indexes on email, username, active, role
- Composite indexes for common query patterns
- Constraints for data integrity
- Timestamps for audit trail

## Future Enhancements

- User roles and permissions management
- User preferences and settings
- User activity tracking
- Profile picture upload
- Email verification workflow
- Password reset functionality
- Two-factor authentication support
