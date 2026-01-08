/**
 * Payment feature module.
 * Handles payment processing, payment method management, refunds, and payment provider integrations.
 * 
 * This feature integrates with external payment providers (Stripe, PayPal) through a provider abstraction layer,
 * allowing for easy extension to additional payment gateways. Payment transactions are stored in PostgreSQL,
 * while payment events and audit logs are stored in MongoDB.
 * 
 * Related features:
 * - {@link com.hafizbahtiar.spring.features.subscription} - Uses payment feature for subscription billing
 * - {@link com.hafizbahtiar.spring.features.user} - Payment methods are associated with users
 */
package com.hafizbahtiar.spring.features.payment;

