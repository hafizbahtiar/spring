# Subscription Feature

This document describes the Subscription Management feature module, which handles subscription plans, user subscriptions, billing cycles, and subscription lifecycle operations.

## Overview

The Subscription feature provides comprehensive subscription management capabilities including:

- Dynamic subscription plan management (admin-configurable plans stored in database)
- Subscription creation and cancellation
- Subscription plan updates and changes
- Automatic subscription renewal processing
- Trial period management
- Billing cycle management (monthly, quarterly, yearly)
- Integration with payment providers for recurring billing
- Subscription status tracking and lifecycle management

## Architecture

The Subscription feature follows the features-first architecture pattern and is organized into clear layers:

```
features/subscription/
├── entity/                    # Domain models
│   ├── Subscription.java     # User subscription entity
│   ├── SubscriptionPlan.java # Dynamic subscription plan entity
│   ├── SubscriptionStatus.java # Subscription status enum
│   ├── SubscriptionPlanType.java # Plan type enum (BASIC, PRO, etc.)
│   └── BillingCycle.java     # Billing cycle enum (MONTHLY, YEARLY, etc.)
├── exception/                # Feature-specific exceptions
│   ├── SubscriptionNotFoundException.java
│   ├── SubscriptionPlanNotFoundException.java
│   ├── SubscriptionException.java
│   └── SubscriptionCancellationException.java
├── repository/               # Data access layer (PostgreSQL)
│   ├── SubscriptionRepository.java
│   └── SubscriptionPlanRepository.java
├── repository/mongodb/       # MongoDB logging
│   └── SubscriptionLogRepository.java
├── dto/                      # Data Transfer Objects
│   ├── SubscriptionRequest.java
│   ├── SubscriptionResponse.java
│   ├── SubscriptionUpdateRequest.java
│   ├── SubscriptionStatusResponse.java
│   ├── SubscriptionPlanRequest.java
│   ├── SubscriptionPlanResponse.java
│   └── SubscriptionPlanSummary.java
├── mapper/                   # DTO conversions
│   ├── SubscriptionMapper.java
│   └── SubscriptionPlanMapper.java
├── provider/                 # Subscription provider abstraction
│   ├── SubscriptionProviderService.java  # Provider interface
│   └── stripe/
│       └── StripeSubscriptionProvider.java
├── service/                  # Business logic
│   ├── SubscriptionService.java
│   ├── SubscriptionServiceImpl.java
│   ├── SubscriptionPlanService.java
│   ├── SubscriptionPlanServiceImpl.java
│   └── SubscriptionLoggingService.java
├── controller/v1/            # REST API endpoints
│   ├── SubscriptionController.java
│   └── SubscriptionPlanController.java
└── model/                    # MongoDB documents
    └── SubscriptionLog.java
```

## Components

### Entity Layer

#### Subscription Entity (`entity/Subscription.java`)

JPA entity representing a user's subscription stored in PostgreSQL.

**Key Features:**

- Links to User and SubscriptionPlan entities
- Tracks provider-specific subscription IDs (e.g., Stripe Subscription ID)
- Manages billing periods and renewal dates
- Supports trial periods
- Tracks cancellation status (immediate or at period end)
- Optimistic locking with @Version
- Business methods for status management

**Fields:**

- `id` - Primary key
- `user` - User who owns the subscription
- `subscriptionPlan` - Subscription plan this subscription is for
- `status` - Subscription status (ACTIVE, TRIALING, PAST_DUE, CANCELLED, EXPIRED, etc.)
- `provider` - Payment provider (STRIPE, PAYPAL, etc.)
- `providerSubscriptionId` - Provider-specific subscription ID
- `providerCustomerId` - Provider-specific customer ID
- `currentPeriodStart` - Start of current billing period
- `currentPeriodEnd` - End of current billing period
- `cancelAtPeriodEnd` - Whether subscription will cancel at period end
- `cancelledAt` - Timestamp when subscription was cancelled
- `trialStart` - Start of trial period (if applicable)
- `trialEnd` - End of trial period (if applicable)
- `billingCycle` - Billing cycle (MONTHLY, QUARTERLY, YEARLY)
- `nextBillingDate` - Next billing date (when subscription will renew)
- `metadata` - Additional metadata (JSONB)
- `createdAt`, `updatedAt` - Timestamps
- `version` - Optimistic locking version

**Business Methods:**

- `isActive()` - Check if subscription is active (ACTIVE or TRIALING)
- `isTrial()` - Check if subscription is in trial period
- `canBeCancelled()` - Check if subscription can be cancelled
- `cancelAtPeriodEnd()` - Cancel subscription at period end
- `cancelImmediately()` - Cancel subscription immediately
- `reactivate()` - Reactivate cancelled subscription
- `daysUntilRenewal()` - Calculate days until next renewal
- `markAsActive()` - Mark subscription as active
- `markAsPastDue()` - Mark subscription as past due
- `markAsExpired()` - Mark subscription as expired

#### SubscriptionPlan Entity (`entity/SubscriptionPlan.java`)

JPA entity representing a dynamic subscription plan stored in PostgreSQL.

**Key Features:**

- Plans are stored in database (not hardcoded)
- Admin-manageable (can be created/modified via API)
- Supports multiple plans per type (e.g., multiple BASIC plans)
- Flexible features stored as JSONB
- Links to provider-specific plan IDs (e.g., Stripe Price ID)
- Soft delete (isActive flag)

**Fields:**

- `id` - Primary key
- `name` - Plan name (e.g., "Basic Monthly", "Pro Yearly")
- `description` - Plan description
- `planType` - Plan type (BASIC, PRO, ENTERPRISE, CUSTOM)
- `price` - Plan price
- `currency` - Currency code (ISO 4217)
- `billingCycle` - Billing cycle (MONTHLY, QUARTERLY, YEARLY)
- `features` - Flexible features (JSONB)
- `isActive` - Whether plan is active and available
- `maxUsers` - Maximum users allowed (example feature)
- `maxStorageGB` - Maximum storage in GB (example feature)
- `providerPlanId` - Provider-specific plan ID (e.g., Stripe Price ID)
- `metadata` - Additional metadata (JSONB)
- `createdAt`, `updatedAt` - Timestamps
- `version` - Optimistic locking version

**Business Methods:**

- `getDisplayName()` - Get formatted display name
- `getFormattedPrice()` - Get formatted price string
- `deactivate()` - Deactivate plan (soft delete)
- `activate()` - Activate plan

### Provider Abstraction Layer

#### SubscriptionProviderService (`provider/SubscriptionProviderService.java`)

Interface for subscription provider abstraction, allowing the subscription service to work with any subscription provider without being tightly coupled to a specific implementation.

**Key Methods:**

- `getProvider()` - Get the payment provider this service supports
- `createSubscription()` - Create a subscription with the provider
- `cancelSubscription()` - Cancel a subscription
- `updateSubscription()` - Update a subscription (plan change, etc.)
- `getSubscription()` - Get subscription details from provider
- `createOrUpdatePrice()` - Create or update Stripe Price for a plan
- `mapProviderStatusToSubscriptionStatus()` - Map provider status to internal status

**Result Class:**

- `ProviderSubscriptionResult` - Result from subscription operations

#### StripeSubscriptionProvider (`provider/stripe/StripeSubscriptionProvider.java`)

Stripe implementation of SubscriptionProviderService.

**Features:**

- Creates Stripe Subscriptions
- Cancels Stripe Subscriptions (immediate or at period end)
- Updates Stripe Subscriptions (plan changes, etc.)
- Retrieves Stripe Subscription details
- Creates/updates Stripe Prices for dynamic plans
- Maps Stripe subscription statuses to internal SubscriptionStatus enum

### Service Layer

#### SubscriptionService (`service/SubscriptionService.java`)

Interface defining subscription operations.

**Methods:**

- `createSubscription()` - Create a new subscription
- `cancelSubscription()` - Cancel a subscription
- `updateSubscription()` - Update subscription (plan change, etc.)
- `renewSubscription()` - Process subscription renewal payment
- `getSubscription()` - Get subscription by ID
- `getSubscriptionByProviderId()` - Get subscription by provider ID
- `getUserSubscriptions()` - List subscriptions for a user (paginated)
- `getActiveSubscription()` - Get active subscription for a user
- `updateSubscriptionStatus()` - Update subscription status (for webhooks)
- `handlePaymentSuccess()` - Handle successful subscription payment
- `handlePaymentFailure()` - Handle failed subscription payment
- `getSubscriptionStatus()` - Get subscription status
- `reactivateSubscription()` - Reactivate cancelled subscription

#### SubscriptionServiceImpl (`service/SubscriptionServiceImpl.java`)

Implementation of subscription service with business logic.

**Features:**

- Validates user and plan before creating subscription
- Checks for existing active subscriptions
- Creates/retrieves provider customer
- Ensures plan has provider plan ID (creates Stripe Price if missing)
- Creates provider subscription
- Processes renewal payments via PaymentService
- Calculates billing periods based on billing cycle
- Updates subscription status based on payment results
- Comprehensive logging via SubscriptionLoggingService

#### SubscriptionPlanService (`service/SubscriptionPlanService.java`)

Interface defining subscription plan management operations.

**Methods:**

- `createPlan()` - Create a new subscription plan (admin only)
- `updatePlan()` - Update an existing subscription plan (admin only)
- `getPlan()` - Get subscription plan by ID
- `listActivePlans()` - List all active plans (public)
- `listPlansByType()` - List plans by plan type
- `listPlansByBillingCycle()` - List plans by billing cycle
- `deactivatePlan()` - Deactivate a plan (admin only)
- `activatePlan()` - Activate a plan (admin only)

#### SubscriptionPlanServiceImpl (`service/SubscriptionPlanServiceImpl.java`)

Implementation of subscription plan service.

**Features:**

- Creates plans with automatic Stripe Price creation
- Validates plan has no active subscriptions before deactivation
- Updates plan details
- Manages plan activation/deactivation

#### SubscriptionLoggingService (`service/SubscriptionLoggingService.java`)

Service for logging subscription events to MongoDB asynchronously.

**Methods:**

- `logSubscriptionCreated()` - Log subscription creation
- `logSubscriptionCancelled()` - Log subscription cancellation
- `logSubscriptionUpdated()` - Log subscription update
- `logSubscriptionReactivated()` - Log subscription reactivation
- `logSubscriptionRenewed()` - Log subscription renewal
- `logPaymentSuccess()` - Log successful subscription payment
- `logPaymentFailure()` - Log failed subscription payment
- `logStatusUpdate()` - Log status update
- `logWebhookReceived()` - Log webhook event

### Controller Layer

#### SubscriptionController (`controller/v1/SubscriptionController.java`)

REST API controller for subscription operations.

**Endpoints:**

- `POST /api/v1/subscriptions` - Create a new subscription
- `GET /api/v1/subscriptions/{id}` - Get subscription by ID
- `GET /api/v1/subscriptions` - List subscriptions for current user (paginated)
- `GET /api/v1/subscriptions/active` - Get active subscription for current user
- `POST /api/v1/subscriptions/{id}/cancel` - Cancel a subscription
- `PUT /api/v1/subscriptions/{id}` - Update subscription
- `POST /api/v1/subscriptions/{id}/reactivate` - Reactivate cancelled subscription
- `GET /api/v1/subscriptions/{id}/status` - Get subscription status

**Authorization:**

- User can only access their own subscriptions
- Admin can access all subscriptions
- Uses `@PreAuthorize` with `@securityService.ownsResource(#id)`

#### SubscriptionPlanController (`controller/v1/SubscriptionPlanController.java`)

REST API controller for subscription plan management.

**Endpoints:**

- `POST /api/v1/subscription-plans` - Create a subscription plan (admin only)
- `PUT /api/v1/subscription-plans/{id}` - Update a subscription plan (admin only)
- `GET /api/v1/subscription-plans/{id}` - Get subscription plan by ID
- `GET /api/v1/subscription-plans` - List all active plans (public)
- `GET /api/v1/subscription-plans/type/{planType}` - List plans by type
- `GET /api/v1/subscription-plans/billing-cycle/{billingCycle}` - List plans by billing cycle
- `POST /api/v1/subscription-plans/{id}/deactivate` - Deactivate a plan (admin only)
- `POST /api/v1/subscription-plans/{id}/activate` - Activate a plan (admin only)

**Authorization:**

- Public endpoints for listing plans
- Admin-only endpoints for plan management
- Uses `@PreAuthorize("hasRole('ADMIN')")` for admin endpoints

### MongoDB Logging

#### SubscriptionLog (`model/SubscriptionLog.java`)

MongoDB document for subscription event logging.

**Fields:**

- `subscriptionId` - Internal subscription ID
- `userId` - User ID
- `subscriptionPlanId` - Subscription plan ID
- `eventType` - Event type (CREATED, CANCELLED, UPDATED, RENEWED, etc.)
- `provider` - Payment provider
- `providerSubscriptionId` - Provider subscription ID
- `status` - Subscription status
- `planName` - Plan name
- `amount` - Subscription amount
- `currency` - Currency code
- `timestamp` - Event timestamp
- `ipAddress` - Client IP address
- `userAgent` - Client user agent
- `sessionId` - Session ID
- `requestId` - Request ID for tracing
- `success` - Whether operation succeeded
- `failureReason` - Failure reason (if failed)
- `failureCode` - Failure code (if failed)
- `responseTimeMs` - Response time in milliseconds
- `details` - Event details (nested object)
- `metadata` - Additional metadata

## Subscription Flow

### Subscription Creation Flow

```
1. Client sends POST /api/v1/subscriptions
   {
     "planId": 1,
     "paymentMethodId": 1,
     "trialDays": 14,  // Optional
     "cancelAtPeriodEnd": false
   }

2. SubscriptionController receives request
   ↓
3. SubscriptionService.createSubscription() is called
   ↓
4. Validates user and plan exist
   ↓
5. Checks for existing active subscriptions
   ↓
6. Gets or creates provider customer (Stripe)
   ↓
7. Ensures plan has provider plan ID (creates Stripe Price if missing)
   ↓
8. SubscriptionProviderService.createSubscription() creates Stripe Subscription
   ↓
9. Subscription entity is saved to PostgreSQL
   ↓
10. SubscriptionLoggingService logs subscription creation (MongoDB)
    ↓
11. SubscriptionResponse is returned
    {
      "id": 1,
      "userId": 1,
      "plan": { ... },
      "status": "ACTIVE",
      "providerSubscriptionId": "sub_xxx",
      "currentPeriodStart": "2024-12-23T00:00:00",
      "currentPeriodEnd": "2025-01-23T00:00:00",
      "nextBillingDate": "2025-01-23T00:00:00",
      ...
    }
```

### Subscription Renewal Flow

```
1. Scheduled job or webhook triggers renewal
   ↓
2. SubscriptionService.renewSubscription() is called
   ↓
3. Validates subscription is due for renewal
   ↓
4. Gets user's default payment method
   ↓
5. PaymentService.processPayment() creates renewal payment
   ↓
6. PaymentService.confirmPayment() confirms payment
   ↓
7. Calculates new billing period based on billing cycle
   ↓
8. Updates subscription dates (currentPeriodStart, currentPeriodEnd, nextBillingDate)
   ↓
9. Updates subscription status (e.g., PAST_DUE → ACTIVE)
   ↓
10. SubscriptionLoggingService logs renewal (MongoDB)
    ↓
11. Updated SubscriptionResponse is returned
```

### Subscription Cancellation Flow

```
1. Client sends POST /api/v1/subscriptions/{id}/cancel?immediate=false
   ↓
2. SubscriptionController receives request
   ↓
3. SubscriptionService.cancelSubscription() is called
   ↓
4. Validates subscription can be cancelled
   ↓
5. SubscriptionProviderService.cancelSubscription() cancels Stripe Subscription
   ↓
6. Updates subscription status and cancellation date
   ↓
7. SubscriptionLoggingService logs cancellation (MongoDB)
   ↓
8. Updated SubscriptionResponse is returned
```

## Configuration

### Application Properties

Subscription feature uses the same Stripe configuration as Payment feature:

```properties
# Stripe Configuration (shared with Payment feature)
stripe.secret-key=${STRIPE_SECRET_KEY:sk_test_xxx}
stripe.publishable-key=${STRIPE_PUBLISHABLE_KEY:pk_test_xxx}
stripe.webhook-secret=${STRIPE_WEBHOOK_SECRET:}
```

### Database Configuration

**PostgreSQL (Business Data):**

- `subscriptions` table - User subscriptions
- `subscription_plans` table - Dynamic subscription plans

**MongoDB (Logging):**

- `subscription_logs` collection - Subscription event logs

## Usage Examples

### Create a Subscription

```bash
POST /api/v1/subscriptions
Authorization: Bearer {token}
{
  "planId": 1,
  "paymentMethodId": 1,
  "trialDays": 14
}
```

**Response:**

```json
{
  "id": 1,
  "userId": 1,
  "plan": {
    "id": 1,
    "name": "Pro Monthly",
    "planType": "PRO",
    "price": 29.99,
    "currency": "USD",
    "billingCycle": "MONTHLY",
    "displayName": "Pro Monthly (Monthly)",
    "formattedPrice": "USD 29.99/monthly"
  },
  "status": "TRIALING",
  "provider": "STRIPE",
  "providerSubscriptionId": "sub_xxx",
  "currentPeriodStart": "2024-12-23T00:00:00",
  "currentPeriodEnd": "2025-01-06T00:00:00",
  "trialStart": "2024-12-23T00:00:00",
  "trialEnd": "2025-01-06T00:00:00",
  "nextBillingDate": "2025-01-06T00:00:00",
  "billingCycle": "MONTHLY"
}
```

### Create a Subscription Plan (Admin)

```bash
POST /api/v1/subscription-plans
Authorization: Bearer {admin_token}
{
  "name": "Pro Monthly",
  "description": "Professional plan with advanced features",
  "planType": "PRO",
  "price": 29.99,
  "currency": "USD",
  "billingCycle": "MONTHLY",
  "maxUsers": 10,
  "maxStorageGB": 100,
  "isActive": true
}
```

**Response:**

```json
{
  "id": 1,
  "name": "Pro Monthly",
  "description": "Professional plan with advanced features",
  "planType": "PRO",
  "price": 29.99,
  "currency": "USD",
  "billingCycle": "MONTHLY",
  "maxUsers": 10,
  "maxStorageGB": 100,
  "isActive": true,
  "providerPlanId": "price_xxx",
  "displayName": "Pro Monthly (Monthly)",
  "formattedPrice": "USD 29.99/monthly"
}
```

### Cancel a Subscription

```bash
POST /api/v1/subscriptions/1/cancel?immediate=false
Authorization: Bearer {token}
```

**Response:**

```json
{
  "id": 1,
  "status": "ACTIVE",
  "cancelAtPeriodEnd": true,
  "currentPeriodEnd": "2025-01-23T00:00:00",
  ...
}
```

### List Active Plans

```bash
GET /api/v1/subscription-plans
```

**Response:**

```json
[
  {
    "id": 1,
    "name": "Basic Monthly",
    "planType": "BASIC",
    "price": 9.99,
    "billingCycle": "MONTHLY",
    "displayName": "Basic Monthly (Monthly)",
    "formattedPrice": "USD 9.99/monthly"
  },
  {
    "id": 2,
    "name": "Pro Monthly",
    "planType": "PRO",
    "price": 29.99,
    "billingCycle": "MONTHLY",
    "displayName": "Pro Monthly (Monthly)",
    "formattedPrice": "USD 29.99/monthly"
  }
]
```

## Integration with Other Features

### Payment Feature

- Subscriptions use PaymentService for recurring billing
- Subscription renewals create payments via PaymentService
- Payment status updates trigger subscription status updates
- Payment methods are used for subscription billing

### User Feature

- Subscriptions are associated with users
- User ownership is validated for all subscription operations
- User's default payment method is used for renewals

## Security Considerations

### Subscription Access Control

- Users can only access their own subscriptions
- Admin role required for accessing all subscriptions
- Resource ownership is validated at service and controller layers
- Plan management endpoints are admin-only

### Plan Management

- Plans can be deactivated but existing subscriptions continue
- Validation prevents deactivation of plans with active subscriptions (optional)
- Plan changes don't affect existing subscriptions (only new subscriptions)

### Payment Security

- Subscription payments use the same security measures as Payment feature
- Payment methods are validated before use
- Payment failures trigger subscription status updates

## Error Handling

### Custom Exceptions

All subscription-related exceptions are located in `features/subscription/exception/`:

- `SubscriptionNotFoundException` - Subscription not found
- `SubscriptionPlanNotFoundException` - Subscription plan not found
- `SubscriptionException` - General subscription operation failed
- `SubscriptionCancellationException` - Subscription cancellation failed

**Common Exceptions** (shared across features, located in `common/exception/`):

- `ProviderException` - External provider error (used by payment and subscription providers)

All exceptions are handled by `GlobalExceptionHandler` and return appropriate HTTP status codes.

## Dependencies

### Required Dependencies (in pom.xml)

- `stripe-java` (24.16.0) - Stripe SDK for Java (shared with Payment feature)
- `spring-boot-starter-data-jpa` - JPA support
- `spring-boot-starter-data-mongodb` - MongoDB support
- `mapstruct` - DTO mapping
- `lombok` - Boilerplate reduction

### Internal Dependencies

- `features/payment` - Payment service for billing
- `features/payment/exception` - Payment exceptions (PaymentNotFoundException, etc.)
- `features/user` - User entity and repository
- `features/user/exception` - User exceptions (UserNotFoundException)
- `common/exception` - Shared exceptions (ProviderException, ValidationException, GlobalExceptionHandler)
- `common/response` - API response utilities

## Future Enhancements

1. **Webhook Integration** - Implement webhook handlers for subscription events
2. **Scheduled Renewal Job** - Automated renewal processing via scheduled tasks
3. **Proration** - Handle prorated charges for plan changes mid-cycle
4. **Usage-Based Billing** - Support for usage-based subscription features
5. **Subscription Analytics** - Dashboard and reporting features
6. **Plan Comparison** - Compare plans side-by-side
7. **Subscription Upgrades/Downgrades** - Seamless plan changes
8. **Grace Period** - Handle grace periods for failed payments
9. **Dunning Management** - Automated retry logic for failed payments
10. **Subscription Coupons** - Discount and coupon support

## Related Documentation

- [Payment Feature Documentation](../payment/README.md)
- [Stripe Webhook Setup Guide](../../../docs/stripe-webhook-setup.md)
- [API Structure Documentation](../../../docs/api-structure.md)
