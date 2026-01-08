# API Structure Documentation

This document outlines the REST API structure and endpoints for the Spring Boot application.

## API Versioning

The API uses URI versioning with the following structure:
```
/api/v1/{resource}
```

## Current API Endpoints (User Management Focus)

### User Management (In Development)
```
POST   /api/v1/auth/register      # User registration
POST   /api/v1/auth/login         # User authentication
POST   /api/v1/auth/logout        # User logout
GET    /api/v1/auth/me           # Get current user profile

GET    /api/v1/users             # Get all users (admin only)
GET    /api/v1/users/{id}        # Get user by ID
PUT    /api/v1/users/{id}        # Update user profile
DELETE /api/v1/users/{id}        # Delete user (admin only)
POST   /api/v1/users/{id}/verify # Verify user email
```

### Future API Endpoints (To Be Implemented)

#### Product Management
```
GET    /api/v1/products          # Get all products
GET    /api/v1/products/{id}     # Get product by ID
POST   /api/v1/products          # Create new product
PUT    /api/v1/products/{id}     # Update product
DELETE /api/v1/products/{id}     # Delete product
```

#### Order Management
```
GET    /api/v1/orders            # Get user orders
GET    /api/v1/orders/{id}       # Get order by ID
POST   /api/v1/orders            # Create new order
PUT    /api/v1/orders/{id}       # Update order status
```

#### Payment Processing
```
POST   /api/v1/payments          # Process payment
GET    /api/v1/payments/{id}     # Get payment status
```

#### Webhooks
```
POST   /api/v1/webhooks/users     # User event webhooks
POST   /api/v1/webhooks/orders    # Order status webhooks
POST   /api/v1/webhooks/payments  # Payment webhooks
```

## Response Format

All API responses follow a consistent JSON structure:

```json
{
  "success": true,
  "data": { ... },
  "message": "Optional message",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

## Error Responses

Error responses include appropriate HTTP status codes and error details:

```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Validation failed",
    "details": { ... }
  },
  "timestamp": "2024-01-01T00:00:00Z"
}
```

## Authentication

Future endpoints will require authentication via JWT tokens:

```
Authorization: Bearer {jwt-token}
```

## Content Types

- Request: `application/json`
- Response: `application/json`

## Rate Limiting

API endpoints may implement rate limiting in production environments.
