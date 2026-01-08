/**
 * Subscription feature module.
 * Handles subscription management, subscription plans, billing cycles, and subscription lifecycle operations.
 * 
 * This feature provides dynamic subscription plan management (admin-configurable plans stored in database),
 * subscription creation and cancellation, automatic renewal processing, and integration with payment providers
 * for recurring billing. Subscription data is stored in PostgreSQL, while subscription events and audit logs
 * are stored in MongoDB.
 * 
 * Related features:
 * - {@link com.hafizbahtiar.spring.features.payment} - Uses payment feature for subscription billing and renewals
 * - {@link com.hafizbahtiar.spring.features.user} - Subscriptions are associated with users
 */
package com.hafizbahtiar.spring.features.subscription;

