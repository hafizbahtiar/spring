# Spring Boot Application Development Roadmap

This document outlines the development phases and tasks required to complete the Spring Boot application with dual-database architecture (PostgreSQL + MongoDB).

## 🎯 Project Overview

- **Architecture**: Features-First Architecture
- **Databases**: PostgreSQL (primary) + MongoDB (secondary)
- **Features**: User, Payment, Subscription, Portfolio management
- **Webhooks**: Payment provider integrations (PayPal, Stripe)
- **Purpose**: Showcase project + Console for portfolio + SaaS platform in future

---

## 🎯 Priority-Based Task List

### 🔴 CRITICAL PRIORITY

#### Phase 0: Layer 2 - Dynamic Groups & Permissions System

**Status**: ✅ **IMPLEMENTED** - Foundation for flexible role-based access control.

> **Goal**: Implement a flexible permission system where OWNER/ADMIN can create Groups and assign fine-grained permissions at Module, Page, and Component levels. This extends the current static role system (Layer 1) with dynamic, configurable permissions (Layer 2).

**Current State:**

- ✅ Layer 1 (Static Roles) is implemented: OWNER, ADMIN, USER roles exist
- ✅ Layer 2 (Dynamic Groups & Permissions) is implemented: Dynamic permission groups, fine-grained permissions, registry management, bulk operations, import/export, search/filtering
- ✅ Role-based access control using `@PreAuthorize` with `hasRole()`, `hasAnyRole()`, and custom permission checks
- ✅ Dynamic permission groups with fine-grained permissions at Module, Page, and Component levels

**Deferred Enhancements (Optional):**

- [ ] Validation for component type consistency - **DEFERRED** (can be enhanced later)
- [ ] Cascade delete options (soft delete with recovery) - **DEFERRED** (can be added later)
- [ ] Update `UserPrincipal` to include group permissions (optional, for performance) - **DEFERRED** (can be added later for optimization)
- [ ] Add permission checks to support endpoints (when implemented) - **DEFERRED** (support module not yet implemented)

- [ ] **Phase 7: Testing & Documentation (Week 7-8)**

  - [ ] Unit tests:
    - [ ] Permission evaluation logic tests
    - [ ] Permission hierarchy tests (MODULE → PAGE → COMPONENT)
    - [ ] Multiple groups OR logic tests
    - [ ] Explicit deny override tests
    - [ ] Highest permission wins tests
  - [ ] Integration tests:
    - [ ] Group CRUD operations
    - [ ] Permission CRUD operations
    - [ ] User assignment operations
    - [ ] Permission check endpoints
    - [ ] Security integration tests
  - [ ] API documentation:
    - [ ] Swagger/OpenAPI documentation for all endpoints
    - [ ] Permission system usage examples
  - [ ] User guide:
    - [ ] How to create groups
    - [ ] How to assign permissions
    - [ ] How to assign users to groups
    - [ ] Permission evaluation examples

**Key Design Decisions (from documentation):**

1. **Automatic Inheritance**: Module permission grants access to all pages/components within that module
2. **Explicit Deny Support**: `granted = false` explicitly denies access, overriding allow permissions
3. **OR Logic with Deny Override**: If any group allows, user has access (unless explicit deny exists)
4. **Highest Permission Wins**: When multiple groups allow, take highest permission (READ < WRITE < DELETE)
5. **Creator Access Validation**: Groups can only include modules/pages/components that creator has access to
6. **Frontend + Backend Checks**: Both frontend (UX) and backend (security) checks required

**Status**: ✅ **IMPLEMENTED** - RBAC system with dynamic permission groups, fine-grained permissions, registry management, bulk operations, import/export, and search/filtering is fully operational.

**Priority**: 🔴 CRITICAL (Foundation for flexible access control) - ✅ **COMPLETED**

**Dependencies**:

- Layer 1 (Static Roles) - ✅ Already implemented
- Redis (for caching) - ⚠️ May need setup
- MongoDB (for audit logging) - ✅ Already configured

---

#### Phase 1: Email Verification Feature

**Status**: ❌ **NOT IMPLEMENTED** - Frontend has verify-email page that requires backend implementation.

**Required Implementation:**

- [ ] Verify email verification endpoints are working:
  - [ ] `POST /api/v1/auth/verify-email` - Verify email with token
  - [ ] `POST /api/v1/auth/resend-verification` - Resend verification email
- [ ] Test email verification flow end-to-end
- [ ] Ensure frontend integration works correctly
- [ ] Add MongoDB logging for verification events

**Frontend Integration:**

- Frontend page: `app/(auth)/verify-email/page.tsx`
- Frontend expects: `POST /api/v1/auth/verify-email` with `token` field

**Estimated Time**: 2-3 days

**Dependencies**: Email service infrastructure (✅ completed)

---

### 🟠 HIGH PRIORITY

#### Phase 2: Two-Token Authentication Testing

**Status**: ⚠️ **IMPLEMENTATION COMPLETE, TESTING PENDING**

**Required Testing:**

- [ ] **Unit Tests**

  - [ ] Test `refreshToken()` method with valid refresh token
  - [ ] Test `refreshToken()` with expired refresh token
  - [ ] Test `refreshToken()` with inactive session
  - [ ] Test `refreshToken()` with inactive user
  - [ ] Test login returns both tokens correctly

- [ ] **Integration Tests**

  - [ ] Test `/auth/refresh` endpoint with valid refresh token
  - [ ] Test `/auth/refresh` endpoint with expired refresh token
  - [ ] Test `/auth/refresh` endpoint with revoked session
  - [ ] Test logout invalidates refresh token
  - [ ] Test concurrent refresh requests

- [ ] **Manual Testing**
  - [ ] Test login flow returns refresh token
  - [ ] Test access token expires after 15 minutes
  - [ ] Test refresh token works to get new access token
  - [ ] Test refresh token expires after 7 days
  - [ ] Test logout revokes refresh token
  - [ ] Test session revocation invalidates refresh token

**Estimated Time**: 2-3 days

**Priority**: HIGH (security feature needs validation)

---

### 🟡 MEDIUM PRIORITY

#### Phase 3: Webhook Integration (Payment & Subscription Webhooks)

**Status**: ❌ **NOT IMPLEMENTED**

**Required Implementation:**

- [ ] **Webhook Security**

  - [ ] PayPal webhook signature validation
  - [ ] Stripe webhook signature validation
  - [ ] HMAC-SHA256 implementation

- [ ] **Webhook Controllers**

  - [ ] `POST /api/v1/webhooks/paypal` - PayPal webhook endpoint
  - [ ] `POST /api/v1/webhooks/stripe` - Stripe webhook endpoint

- [ ] **Webhook Processing**

  - [ ] Event type parsing and routing
  - [ ] Payment status updates
  - [ ] Subscription status synchronization
  - [ ] MongoDB audit logging

- [ ] **Webhook Reliability**
  - [ ] Idempotency key handling
  - [ ] Failed webhook retry logic
  - [ ] Dead letter queue for persistent failures

**Estimated Time**: 3-4 days

---

#### Phase 5: All Logs API (Optional - Admin Only)

**Status**: ✅ **IMPLEMENTED**

**Completed Implementation:**

- [x] Create `LogController` with aggregated endpoints:
  - [x] `GET /api/v1/logs?type=all&limit=20` - Get aggregated logs from all collections (admin/owner only)
  - [x] `GET /api/v1/logs/user-activity?limit=10` - Get user activity logs
  - [x] `GET /api/v1/logs/security?limit=10` - Get security logs
  - [x] `GET /api/v1/logs/portfolio?limit=10` - Get portfolio logs
- [x] Create unified `LogResponse` DTO that can represent different log types
- [x] Create `LogService` to aggregate logs from multiple MongoDB collections
- [x] Authorization: OWNER/ADMIN only

**Implementation Details:**

- **Location**: `spring/src/main/java/com/hafizbahtiar/spring/features/logs`
- **DTO**: `LogResponse` - Unified response for all log types (AUTH, USER_ACTIVITY, PORTFOLIO, PERMISSION)
- **Service**: `LogService` - Aggregates logs from auth_logs, user_activity, portfolio_logs, permission_logs collections
- **Controller**: `LogController` - REST endpoints with `@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")`
- **Features**:
  - Aggregates logs from multiple MongoDB collections
  - Sorts by timestamp descending
  - Supports filtering by log type (all, user-activity, security, portfolio)
  - Limit validation (1-100, defaults to 20 for all, 10 for specific types)
  - Error handling for individual collection failures (continues with other collections)

**Priority**: MEDIUM (optional feature)

---

### 🟢 LOW PRIORITY

#### Phase 9: Testing & Quality Assurance

**Status**: ❌ **NOT IMPLEMENTED**

**Required Implementation:**

- [ ] **Unit Testing**

  - [ ] Entity tests
  - [ ] Service tests
  - [ ] Repository tests

- [ ] **Integration Testing**

  - [ ] API integration tests
  - [ ] Database integration tests
  - [ ] External service mocking

- [ ] **Test Infrastructure**

  - [ ] H2 database for unit tests
  - [ ] TestContainers for integration tests
  - [ ] Test data seeding

- [ ] **Code Quality**
  - [ ] Configure JaCoCo (aim for >80% coverage)
  - [ ] SonarQube integration
  - [ ] Security vulnerability scanning

**Estimated Time**: 3-4 days

---

#### Phase 10: Deployment & Production

**Status**: ❌ **NOT IMPLEMENTED**

**Required Implementation:**

- [ ] **Containerization**

  - [ ] Multi-stage Dockerfile
  - [ ] Docker Compose for local development

- [ ] **CI/CD Pipeline**

  - [ ] GitHub Actions setup
  - [ ] Automated testing
  - [ ] Code quality checks

- [ ] **Production Configuration**

  - [ ] Production application.properties
  - [ ] Database connection optimization
  - [ ] Logging configuration

- [ ] **Monitoring & Observability**
  - [ ] Spring Boot Actuator endpoints
  - [ ] Health checks configuration
  - [ ] Centralized logging setup

**Estimated Time**: 2-3 days

---

#### Phase 11: Performance & Optimization

**Status**: ❌ **NOT IMPLEMENTED**

**Required Implementation:**

- [ ] **Database Optimization**

  - [ ] Query optimization and indexing
  - [ ] N+1 query problem resolution
  - [ ] Database connection pooling tuning

- [ ] **Caching Strategy**

  - [ ] Redis integration
  - [ ] Cache configuration
  - [ ] Cache invalidation strategy

- [ ] **API Optimization**
  - [ ] JSON serialization tuning
  - [ ] Compression configuration
  - [ ] Rate limiting implementation

**Estimated Time**: 2-3 days

---

#### Phase 12: Dynamic Navigation Menu System (MEDIUM PRIORITY)

**Status**: ⚠️ **PARTIALLY IMPLEMENTED** - Backend and frontend integration complete, data initialization pending.

> **Goal**: Replace hardcoded sidebar navigation with dynamic menu items stored in database, controlled by permissions.

**Current State:**

- ✅ Backend API complete (database schema, service layer, DTOs, controller)
- ✅ Frontend integration complete (API client, navigation context, sidebar component)
- ❌ Data initialization pending (default menu items not yet populated)

**Required Implementation:**

- [ ] **Data Initialization**

  - [ ] Create database migration or initialization script to populate default menu items:
    - [ ] Main Navigation: Home (`/dashboard`)
    - [ ] Portfolio (OWNER only): Profile, Projects, Companies, Skills, Experiences, Education, Certifications, Blog, Testimonials, Contacts
    - [ ] Admin (OWNER/ADMIN): Dashboard, Queues, Health, Cron Jobs, Metrics
    - [ ] Settings (all authenticated users)
  - [ ] Map icon names to Lucide React icon identifiers (e.g., "home", "briefcase", "settings")

- [ ] **Integration with Permission System**
  - [ ] Filter menu items based on user's group permissions (Layer 2 is now implemented)
  - [ ] Check `required_permission_module` and `required_permission_page` against user's effective permissions
  - [ ] Only show menu items user has READ access to

**Estimated Time**: 3-4 days

**Dependencies**: None (can be implemented independently, but will integrate with Layer 2 when available)

---

#### Phase 13: Optional Improvements

**Status**: ❌ **NOT IMPLEMENTED**

**Optional Enhancements:**

- [ ] **API Documentation** - Add Swagger/OpenAPI documentation
- [ ] **Database Migrations** - Create Flyway migrations for production (currently using Hibernate ddl-auto=update)
- [ ] **PayPal Integration** - Implement PayPal payment provider (Stripe is complete)
- [ ] **Blog Content Image Upload** - Add `POST /api/v1/portfolio/blogs/images` endpoint for uploading images to be embedded in content

**Estimated Time**: Varies by feature

---

## 📝 Implementation Notes

### Schema Management

- **Current**: Using Hibernate `ddl-auto=update` for development
- **Production**: Re-enable Flyway by setting `spring.flyway.enabled=true` and `spring.jpa.hibernate.ddl-auto=validate`

### Testing Status

- **Unit Tests**: Not yet implemented
- **Integration Tests**: Not yet implemented
- **Test Coverage**: 0% (target: >80%)

---

_Last Updated: January 3, 2026_
_Next Review: January 2026_
_Current Focus: Phase 0 - RBAC System (✅ COMPLETED) → Email Verification (Critical) → Two-Token Auth Testing (High) → Webhook Integration (Medium)_
