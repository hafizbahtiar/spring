# Webhook Structure Documentation

This document outlines the webhook implementation structure for handling asynchronous notifications from external services, primarily payment providers.

## Overview

Webhooks are HTTP callbacks that allow external services to notify our application about events asynchronously. This enables real-time updates for various business processes including payment processing, order management, and external system integrations.

All features in the application have webhook support structures, allowing for comprehensive event-driven architecture. Each feature includes dedicated webhook packages for controllers, services, DTOs, and security utilities.

## Webhook Architecture

### Directory Structure

```
src/main/java/com/hafizbahtiar/spring/features/payment/
├── webhook/                    # Webhook security and utilities
├── controller/v1/webhook/      # Webhook endpoint controllers
├── service/webhook/           # Webhook processing services
└── dto/webhook/               # Webhook data transfer objects
```

### Security Considerations

Webhooks must be secured to prevent unauthorized access and ensure data integrity:

1. **Signature Verification**: Verify webhook signatures using provider-specific algorithms
2. **IP Whitelisting**: Restrict webhook sources to known provider IPs
3. **Idempotency**: Handle duplicate webhook deliveries gracefully
4. **Rate Limiting**: Protect against webhook spam attacks

## Feature-Specific Webhook Support

### Payment Webhooks (Implemented)
Primary webhook support for payment processing with external provider integrations.

### Order Webhooks (Planned)
- Order status updates from fulfillment systems
- Shipping notifications
- Order cancellation confirmations
- Inventory level alerts

### Product Webhooks (Planned)
- Inventory updates from suppliers
- Price change notifications
- Product availability alerts
- Catalog synchronization events

### User Webhooks (Planned)
- User registration events
- Profile update notifications
- Account status changes
- Authentication events

## Supported Webhook Providers

### PayPal Webhooks

**Supported Events:**
- `PAYMENT.CAPTURE.COMPLETED` - Payment successfully captured
- `PAYMENT.CAPTURE.DENIED` - Payment capture denied
- `PAYMENT.CAPTURE.PENDING` - Payment capture pending
- `PAYMENT.CAPTURE.REFUNDED` - Payment refunded

**Endpoint:** `POST /api/v1/webhooks/paypal`

**Headers:**
```
PayPal-Transmission-Id: {transmission-id}
PayPal-Transmission-Time: {timestamp}
PayPal-Cert-Url: {certificate-url}
PayPal-Auth-Algo: {algorithm}
PayPal-Transmission-Sig: {signature}
Content-Type: application/json
```

### Stripe Webhooks

**Supported Events:**
- `payment_intent.succeeded` - Payment succeeded
- `payment_intent.payment_failed` - Payment failed
- `payment_intent.canceled` - Payment canceled
- `charge.dispute.created` - Charge disputed

**Endpoint:** `POST /api/v1/webhooks/stripe`

**Headers:**
```
Stripe-Signature: t={timestamp},v1={signature}
Content-Type: application/json
```

## Webhook Processing Flow

### 1. Request Reception
- Webhook endpoints receive HTTP POST requests from providers
- Basic validation (method, content-type, required headers)

### 2. Security Verification
- Verify webhook signatures using provider-specific algorithms
- Validate timestamp to prevent replay attacks
- Check source IP against whitelist (optional)

### 3. Payload Processing
- Parse JSON payload into typed DTOs
- Extract event type and relevant data
- Validate payload structure and required fields

### 4. Business Logic Execution
- Update payment/order status in database
- Trigger downstream business processes
- Send notifications if needed

### 5. Response
- Return appropriate HTTP status codes
- Log processing results for monitoring

## Webhook DTOs

### PayPal Webhook Payload
```java
public class PayPalWebhookDto {
    private String id;
    private String eventType;
    private PayPalResource resource;
    private String createTime;
    // getters and setters
}
```

### Stripe Webhook Payload
```java
public class StripeWebhookDto {
    private String id;
    private String object;
    private String apiVersion;
    private long created;
    private StripeEventData data;
    // getters and setters
}
```

## Controller Implementation

### Base Webhook Controller
```java
@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {

    @PostMapping("/paypal")
    public ResponseEntity<Void> handlePayPalWebhook(@RequestBody String payload, @RequestHeader Map<String, String> headers) {
        // Implementation
    }

    @PostMapping("/stripe")
    public ResponseEntity<Void> handleStripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String signature) {
        // Implementation
    }
}
```

## Service Layer

### Webhook Processing Service
```java
@Service
public class WebhookProcessingService {

    public void processPayPalWebhook(PayPalWebhookDto webhook) {
        // Update payment status
        // Trigger business logic
    }

    public void processStripeWebhook(StripeWebhookDto webhook) {
        // Update payment status
        // Trigger business logic
    }
}
```

### Signature Verification Service
```java
@Service
public class WebhookSecurityService {

    public boolean verifyPayPalSignature(String payload, Map<String, String> headers) {
        // PayPal signature verification logic
    }

    public boolean verifyStripeSignature(String payload, String signature) {
        // Stripe signature verification logic
    }
}
```

## Error Handling

### Common Webhook Errors
- `400 Bad Request` - Invalid payload format
- `401 Unauthorized` - Signature verification failed
- `403 Forbidden` - IP not whitelisted
- `422 Unprocessable Entity` - Invalid event type
- `429 Too Many Requests` - Rate limit exceeded
- `500 Internal Server Error` - Processing failed

### Logging and Monitoring
- Log all webhook attempts (successful and failed)
- Monitor webhook latency and success rates
- Alert on signature verification failures
- Track event processing metrics

## Configuration

### Application Properties
```yaml
# PayPal Webhook Configuration
paypal:
  webhook:
    verification: true
    tolerance: 300000  # 5 minutes in milliseconds

# Stripe Webhook Configuration
stripe:
  webhook:
    endpoint-secret: ${STRIPE_WEBHOOK_SECRET}
    tolerance: 300

# General Webhook Configuration
webhook:
  rate-limit: 1000  # requests per minute
  ip-whitelist:
    enabled: false
    addresses: []
```

## Testing

### Webhook Testing Tools
- **Stripe CLI**: `stripe listen --forward-to localhost:8080/api/v1/webhooks/stripe`
- **PayPal Webhook Simulator**: Use PayPal's developer dashboard
- **ngrok/localtunnel**: Expose local server for testing

### Unit Testing
```java
@SpringBootTest
public class WebhookControllerTest {

    @Test
    public void shouldProcessValidPayPalWebhook() {
        // Test implementation
    }

    @Test
    public void shouldRejectInvalidSignature() {
        // Test implementation
    }
}
```

## Best Practices

### Reliability
- Implement idempotency to handle duplicate webhooks
- Use database transactions for state changes
- Implement retry mechanisms for failed processing

### Security
- Always verify webhook signatures
- Use HTTPS for webhook endpoints
- Implement proper input validation
- Log sensitive data appropriately (avoid logging full payloads)

### Performance
- Process webhooks asynchronously when possible
- Implement queuing for high-volume scenarios
- Monitor and alert on webhook processing delays

### Monitoring
- Track webhook success/failure rates
- Monitor processing latency
- Alert on unusual patterns (e.g., high failure rates)

## Future Enhancements

- Support for additional payment providers (Square, Braintree, etc.)
- Webhook event queuing with retry mechanisms
- Advanced analytics and reporting
- Webhook payload archiving for audit purposes
