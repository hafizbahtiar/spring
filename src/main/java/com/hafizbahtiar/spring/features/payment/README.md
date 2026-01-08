# Payment Feature

This document describes the Payment Management feature module, which handles payment processing, payment method management, refunds, and integration with external payment providers.

## Overview

The Payment feature provides comprehensive payment processing capabilities including:

- Payment processing (one-time and recurring payments)
- Payment method management (add, list, set default, remove)
- Payment confirmation and status tracking
- Refund processing (full and partial refunds)
- Multi-provider support (Stripe, PayPal) through abstraction layer
- Payment event logging and audit trails

## Architecture

The Payment feature follows the features-first architecture pattern and is organized into clear layers:

```
features/payment/
├── entity/                    # Domain models
│   ├── Payment.java          # Payment transaction entity
│   ├── PaymentMethod.java     # Stored payment method entity
│   ├── PaymentStatus.java    # Payment status enum
│   ├── PaymentProvider.java  # Payment provider enum
│   └── PaymentMethodType.java # Payment method type enum
├── exception/                # Feature-specific exceptions
│   ├── PaymentNotFoundException.java
│   ├── PaymentProcessingException.java
│   ├── PaymentMethodNotFoundException.java
│   ├── PaymentMethodException.java
│   └── RefundException.java
├── repository/               # Data access layer (PostgreSQL)
│   ├── PaymentRepository.java
│   └── PaymentMethodRepository.java
├── repository/mongodb/       # MongoDB logging
│   └── PaymentLogRepository.java
├── dto/                      # Data Transfer Objects
│   ├── PaymentRequest.java
│   ├── PaymentResponse.java
│   ├── PaymentStatusResponse.java
│   ├── RefundRequest.java
│   ├── RefundResponse.java
│   ├── PaymentMethodRequest.java
│   └── PaymentMethodResponse.java
├── mapper/                   # DTO conversions
│   ├── PaymentMapper.java
│   └── PaymentMethodMapper.java
├── provider/                  # Payment provider abstraction
│   ├── PaymentProviderService.java  # Provider interface
│   └── stripe/
│       ├── StripeConfig.java
│       └── StripePaymentProvider.java
├── service/                  # Business logic
│   ├── PaymentService.java
│   ├── PaymentServiceImpl.java
│   ├── PaymentMethodService.java
│   ├── PaymentMethodServiceImpl.java
│   └── PaymentLoggingService.java
├── controller/v1/            # REST API endpoints
│   ├── PaymentController.java
│   └── PaymentMethodController.java
└── model/                    # MongoDB documents
    └── PaymentLog.java
```

## Components

### Entity Layer

#### Payment Entity (`entity/Payment.java`)

JPA entity representing a payment transaction stored in PostgreSQL.

**Key Features:**

- Links to User, Order, and Subscription entities
- Tracks provider-specific payment IDs (e.g., Stripe PaymentIntent ID)
- Stores payment amount, currency, and status
- Supports refund tracking
- Optimistic locking with @Version
- Business methods for status management

**Fields:**

- `id` - Primary key
- `user` - User who made the payment
- `orderId` - Associated order ID (nullable)
- `subscriptionId` - Associated subscription ID (nullable)
- `provider` - Payment provider (STRIPE, PAYPAL, etc.)
- `providerPaymentId` - Provider-specific payment ID
- `clientSecret` - Client secret for frontend payment confirmation (Stripe)
- `amount` - Payment amount
- `currency` - Currency code (ISO 4217)
- `status` - Payment status (PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED, CANCELLED)
- `paymentMethodId` - Reference to stored payment method
- `failureCode` - Failure reason code
- `refundAmount` - Total refunded amount
- `refundedAt` - Refund timestamp
- `metadata` - Additional metadata (JSONB)
- `createdAt`, `updatedAt` - Timestamps
- `version` - Optimistic locking version

**Business Methods:**

- `markAsCompleted()` - Mark payment as completed
- `markAsFailed(String reason, String code)` - Mark payment as failed
- `markAsRefunded(BigDecimal amount)` - Mark payment as refunded
- `canBeRefunded()` - Check if payment can be refunded
- `isTerminal()` - Check if payment is in terminal state

#### PaymentMethod Entity (`entity/PaymentMethod.java`)

JPA entity representing a stored payment method (e.g., saved credit card).

**Key Features:**

- Links to User entity
- Stores provider-specific payment method IDs
- Tracks card details (last4, brand, expiry)
- Supports default payment method per user
- Optimistic locking with @Version

**Fields:**

- `id` - Primary key
- `user` - User who owns the payment method
- `provider` - Payment provider (STRIPE, PAYPAL, etc.)
- `providerMethodId` - Provider-specific payment method ID
- `providerCustomerId` - Provider-specific customer ID
- `type` - Payment method type (CREDIT_CARD, DEBIT_CARD, etc.)
- `last4` - Last 4 digits of card
- `brand` - Card brand (visa, mastercard, etc.)
- `expiryMonth` - Card expiry month (1-12)
- `expiryYear` - Card expiry year (4 digits)
- `isDefault` - Whether this is the default payment method
- `metadata` - Additional metadata (JSONB)
- `createdAt`, `updatedAt` - Timestamps

**Business Methods:**

- `setAsDefault()` - Set as default payment method
- `unsetAsDefault()` - Unset as default
- `getDisplayName()` - Get formatted display name (e.g., "Visa •••• 4242")

### Provider Abstraction Layer

#### PaymentProviderService (`provider/PaymentProviderService.java`)

Interface for payment provider abstraction, allowing the payment service to work with any payment provider without being tightly coupled to a specific implementation.

**Key Methods:**

- `getProvider()` - Get the payment provider this service handles
- `createPayment()` - Create a payment intent/order
- `confirmPayment()` - Confirm a payment
- `getPaymentStatus()` - Get payment status from provider
- `refundPayment()` - Process a refund
- `createOrRetrieveCustomer()` - Create or retrieve customer in provider system
- `attachPaymentMethodToCustomer()` - Attach payment method to customer
- `detachPaymentMethodFromCustomer()` - Detach payment method from customer
- `getPaymentMethodDetails()` - Get payment method details (card info, etc.)
- `validateWebhookSignature()` - Validate webhook signature

**Result Classes:**

- `ProviderPaymentResult` - Result from payment creation/confirmation
- `ProviderRefundResult` - Result from refund operations
- `ProviderPaymentMethodDetails` - Payment method details from provider

#### StripePaymentProvider (`provider/stripe/StripePaymentProvider.java`)

Stripe implementation of PaymentProviderService.

**Features:**

- Creates Stripe PaymentIntents
- Confirms PaymentIntents
- Processes refunds via Stripe Refunds API
- Creates/retrieves Stripe Customers
- Attaches/detaches PaymentMethods to/from Customers
- Retrieves payment method details (card info)
- Validates Stripe webhook signatures
- Maps Stripe statuses to internal PaymentStatus enum

**Configuration:**

- Uses `StripeConfig` for API keys
- Initializes Stripe API key on startup (@PostConstruct)

### Service Layer

#### PaymentService (`service/PaymentService.java`)

Interface defining payment operations.

**Methods:**

- `processPayment()` - Create and process a payment
- `confirmPayment()` - Confirm a payment
- `getPayment()` - Get payment by ID
- `getPaymentByProviderId()` - Get payment by provider payment ID
- `listPayments()` - List payments for a user (paginated)
- `refundPayment()` - Process a refund
- `getPaymentStatus()` - Get payment status
- `updatePaymentStatus()` - Update payment status (for webhooks)

#### PaymentServiceImpl (`service/PaymentServiceImpl.java`)

Implementation of payment service with business logic.

**Features:**

- Provider-agnostic payment processing
- Payment method validation and resolution
- Payment status management
- Refund processing with validation
- Comprehensive logging via PaymentLoggingService
- Error handling with custom exceptions

#### PaymentMethodService (`service/PaymentMethodService.java`)

Interface defining payment method management operations.

**Methods:**

- `addPaymentMethod()` - Add a payment method for a user
- `listPaymentMethods()` - List all payment methods for a user
- `getPaymentMethod()` - Get payment method by ID
- `setDefaultPaymentMethod()` - Set a payment method as default
- `getDefaultPaymentMethod()` - Get default payment method
- `removePaymentMethod()` - Remove a payment method

#### PaymentMethodServiceImpl (`service/PaymentMethodServiceImpl.java`)

Implementation of payment method service.

**Features:**

- Fetches payment method details from provider (card info)
- Validates payment method ownership
- Manages default payment method per user
- Detaches payment methods from provider when removed
- Comprehensive logging via PaymentLoggingService

#### PaymentLoggingService (`service/PaymentLoggingService.java`)

Service for logging payment events to MongoDB asynchronously.

**Methods:**

- `logPaymentCreated()` - Log payment creation
- `logPaymentConfirmed()` - Log payment confirmation
- `logPaymentFailed()` - Log payment failure
- `logPaymentRefunded()` - Log refund
- `logPaymentStatusUpdate()` - Log status update
- `logPaymentMethodAdded()` - Log payment method addition
- `logPaymentMethodRemoved()` - Log payment method removal

### Controller Layer

#### PaymentController (`controller/v1/PaymentController.java`)

REST API controller for payment operations.

**Endpoints:**

- `POST /api/v1/payments` - Process a payment
- `POST /api/v1/payments/{id}/confirm` - Confirm a payment
- `GET /api/v1/payments/{id}` - Get payment by ID
- `GET /api/v1/payments` - List payments for current user (paginated)
- `POST /api/v1/payments/{id}/refund` - Process a refund
- `GET /api/v1/payments/{id}/status` - Get payment status

**Authorization:**

- User can only access their own payments
- Admin can access all payments
- Uses `@PreAuthorize` with `@securityService.ownsPayment(#id)`

#### PaymentMethodController (`controller/v1/PaymentMethodController.java`)

REST API controller for payment method management.

**Endpoints:**

- `POST /api/v1/payments/methods` - Add a payment method
- `GET /api/v1/payments/methods` - List payment methods for current user
- `GET /api/v1/payments/methods/{id}` - Get payment method by ID
- `PUT /api/v1/payments/methods/{id}/default` - Set as default payment method
- `GET /api/v1/payments/methods/default` - Get default payment method
- `DELETE /api/v1/payments/methods/{id}` - Remove a payment method

**Authorization:**

- User can only access their own payment methods
- Admin can access all payment methods
- Uses `@PreAuthorize` with `@securityService.ownsPaymentMethod(#id)`

### MongoDB Logging

#### PaymentLog (`model/PaymentLog.java`)

MongoDB document for payment event logging.

**Fields:**

- `paymentId` - Internal payment ID
- `userId` - User ID
- `provider` - Payment provider
- `providerPaymentId` - Provider payment ID
- `eventType` - Event type (CREATED, CONFIRMED, FAILED, REFUNDED, etc.)
- `timestamp` - Event timestamp
- `amount` - Payment amount
- `currency` - Currency code
- `status` - Payment status
- `ipAddress` - Client IP address
- `userAgent` - Client user agent
- `sessionId` - Session ID
- `requestId` - Request ID for tracing
- `metadata` - Additional metadata
- `responseTimeMs` - Response time in milliseconds
- `failureReason` - Failure reason (if failed)
- `refundAmount` - Refund amount (if refunded)
- `refundId` - Provider refund ID

## Payment Flow

### Payment Processing Flow

```
1. Client sends POST /api/v1/payments
   {
     "amount": 100.00,
     "currency": "USD",
     "paymentMethodId": 1,  // Optional: use saved payment method
     "orderId": 123,        // Optional: link to order
     "subscriptionId": 456  // Optional: link to subscription
   }

2. PaymentController receives request
   ↓
3. PaymentService.processPayment() is called
   ↓
4. Validates user and payment request
   ↓
5. Resolves payment method if provided
   ↓
6. Gets or creates provider customer (Stripe)
   ↓
7. PaymentProviderService.createPayment() creates PaymentIntent
   ↓
8. Payment entity is saved to PostgreSQL
   ↓
9. PaymentLoggingService logs payment creation (MongoDB)
   ↓
10. PaymentResponse is returned with clientSecret
    {
      "id": 1,
      "providerPaymentId": "pi_xxx",
      "clientSecret": "pi_xxx_secret_xxx",
      "status": "PENDING",
      "amount": 100.00,
      ...
    }
```

### Payment Confirmation Flow

```
1. Client sends POST /api/v1/payments/{id}/confirm
   {
     "paymentMethodId": 1  // Optional: use saved payment method
   }

2. PaymentController receives request
   ↓
3. PaymentService.confirmPayment() is called
   ↓
4. Validates payment state
   ↓
5. Resolves payment method if provided
   ↓
6. PaymentProviderService.confirmPayment() confirms PaymentIntent
   ↓
7. Payment status is updated in PostgreSQL
   ↓
8. PaymentLoggingService logs confirmation (MongoDB)
   ↓
9. Updated PaymentResponse is returned
```

### Refund Flow

```
1. Client sends POST /api/v1/payments/{id}/refund
   {
     "amount": 50.00,  // Optional: partial refund, null for full refund
     "reason": "Customer request"
   }

2. PaymentController receives request
   ↓
3. PaymentService.refundPayment() is called
   ↓
4. Validates payment can be refunded
   ↓
5. Validates refund amount (not exceeding available amount)
   ↓
6. PaymentProviderService.refundPayment() processes refund
   ↓
7. Payment status and refund amount are updated
   ↓
8. PaymentLoggingService logs refund (MongoDB)
   ↓
9. RefundResponse is returned
```

## Configuration

### Application Properties

Add to `application.properties` or environment variables:

```properties
# Stripe Configuration
stripe.secret-key=${STRIPE_SECRET_KEY:sk_test_xxx}
stripe.publishable-key=${STRIPE_PUBLISHABLE_KEY:pk_test_xxx}
stripe.webhook-secret=${STRIPE_WEBHOOK_SECRET:}
```

**Important:**

- Use test keys (`sk_test_xxx`, `pk_test_xxx`) for development
- Use live keys (`sk_live_xxx`, `pk_live_xxx`) for production
- Store keys in environment variables, not in code
- Webhook secret is required for webhook signature validation

### Database Configuration

**PostgreSQL (Business Data):**

- `payments` table - Payment transactions
- `payment_methods` table - Stored payment methods

**MongoDB (Logging):**

- `payment_logs` collection - Payment event logs

## Usage Examples

### Process a Payment

```bash
POST /api/v1/payments
Authorization: Bearer {token}
{
  "amount": 100.00,
  "currency": "USD",
  "paymentMethodId": 1,
  "description": "Order #123"
}
```

**Response:**

```json
{
  "id": 1,
  "userId": 1,
  "provider": "STRIPE",
  "providerPaymentId": "pi_xxx",
  "clientSecret": "pi_xxx_secret_xxx",
  "amount": 100.0,
  "currency": "USD",
  "status": "PENDING",
  "createdAt": "2024-12-23T12:00:00"
}
```

### Confirm a Payment

```bash
POST /api/v1/payments/1/confirm
Authorization: Bearer {token}
{
  "paymentMethodId": 1
}
```

### Add a Payment Method

```bash
POST /api/v1/payments/methods
Authorization: Bearer {token}
{
  "provider": "STRIPE",
  "providerMethodId": "pm_xxx",
  "providerCustomerId": "cus_xxx",
  "setAsDefault": true
}
```

**Response:**

```json
{
  "id": 1,
  "userId": 1,
  "provider": "STRIPE",
  "type": "CREDIT_CARD",
  "brand": "visa",
  "last4": "4242",
  "expiryMonth": 12,
  "expiryYear": 2025,
  "isDefault": true,
  "displayName": "Visa •••• 4242"
}
```

### Process a Refund

```bash
POST /api/v1/payments/1/refund
Authorization: Bearer {token}
{
  "amount": 50.00,
  "reason": "Customer request"
}
```

**Response:**

```json
{
  "paymentId": 1,
  "providerRefundId": "re_xxx",
  "refundAmount": 50.0,
  "paymentStatus": "REFUNDED",
  "reason": "Customer request",
  "refundedAt": "2024-12-23T12:00:00"
}
```

## Integration with Other Features

### Subscription Feature

- Subscriptions use PaymentService for recurring billing
- Subscription renewals create payments via PaymentService
- Payment status updates trigger subscription status updates

### User Feature

- Payment methods are associated with users
- Payments are linked to user accounts
- User ownership is validated for all payment operations

## Security Considerations

### Payment Data Security

- Payment methods are stored securely (only last4 digits, not full card numbers)
- Provider-specific IDs are used for actual payment processing
- Sensitive data (full card numbers, CVV) never touches our servers
- PCI compliance is handled by payment providers (Stripe, PayPal)

### Authorization

- Users can only access their own payments and payment methods
- Admin role required for accessing all payments
- Resource ownership is validated at service and controller layers

### Webhook Security

- Webhook signatures are validated using provider secrets
- Webhook endpoints should be secured and rate-limited
- Webhook payloads are logged for audit purposes

## Error Handling

### Custom Exceptions

All payment-related exceptions are located in `features/payment/exception/`:

- `PaymentNotFoundException` - Payment not found
- `PaymentProcessingException` - Payment processing failed
- `RefundException` - Refund processing failed
- `PaymentMethodNotFoundException` - Payment method not found
- `PaymentMethodException` - Payment method operation failed

**Common Exceptions** (shared across features, located in `common/exception/`):

- `ProviderException` - External provider error (used by payment and subscription providers)

All exceptions are handled by `GlobalExceptionHandler` and return appropriate HTTP status codes.

## Dependencies

### Required Dependencies (in pom.xml)

- `stripe-java` (24.16.0) - Stripe SDK for Java
- `spring-boot-starter-data-jpa` - JPA support
- `spring-boot-starter-data-mongodb` - MongoDB support
- `mapstruct` - DTO mapping
- `lombok` - Boilerplate reduction

### Internal Dependencies

- `features/user` - User entity and repository
- `features/user/exception` - User exceptions (UserNotFoundException)
- `common/exception` - Shared exceptions (ProviderException, ValidationException, GlobalExceptionHandler)
- `common/response` - API response utilities

## Future Enhancements

1. **PayPal Integration** - Implement PayPalPaymentProvider
2. **Webhook Endpoints** - Create webhook handlers for Stripe and PayPal
3. **Payment Retry Logic** - Automatic retry for failed payments
4. **Payment Analytics** - Dashboard and reporting features
5. **Multi-Currency Support** - Enhanced currency handling
6. **Payment Plans** - Support for installment payments
7. **3D Secure** - Enhanced security for card payments
8. **Payment Method Validation** - Real-time card validation

## Related Documentation

- [Stripe Webhook Setup Guide](../../../docs/stripe-webhook-setup.md)
- [Subscription Feature Documentation](../subscription/README.md)
- [API Structure Documentation](../../../docs/api-structure.md)
