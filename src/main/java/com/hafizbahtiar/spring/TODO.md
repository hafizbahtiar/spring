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

**Priority**: 🔴 CRITICAL (Foundation for flexible access control) - ✅ **COMPLETED**

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

**Status**: ✅ **IMPLEMENTED** - Backend, frontend, data initialization, and UI improvements complete.

> **Goal**: Replace hardcoded sidebar navigation with dynamic menu items stored in database, controlled by permissions.

**Priority**: MEDIUM - ✅ **COMPLETED**

---

#### Phase 13: Dynamic Cron Job Management System (Hybrid Approach)

**Status**: ⚠️ **PHASES 1-7 IMPLEMENTED, TESTING PENDING**

> **Goal**: Implement a hybrid cron job management system where OWNER can create and manage cron jobs. Supports both application-level (Spring `@Scheduled` with dynamic scheduling) and database-level (PostgreSQL `pg_cron`) job types.

**Architecture Decision:**

- **Application-Level Jobs**: Use Spring's `TaskScheduler` or Quartz Scheduler for dynamic scheduling. Jobs execute within Spring Boot context and can access Spring services.
- **Database-Level Jobs**: Use PostgreSQL `pg_cron` extension for SQL-based jobs that run independently of the application.
- **Storage**: All job definitions stored in PostgreSQL `cron_jobs` table.
- **UI**: Owners can create/edit/delete jobs and choose job type (application or database).

**Current State:**

- ✅ Cron job monitoring exists (`/api/v1/admin/cron-jobs`) - read-only status and history
- ✅ MongoDB logging for cron job executions exists
- ✅ CRUD functionality for creating/managing jobs (Phases 1-7 completed)
- ✅ Dynamic scheduler implementation (application-level and database-level)
- ✅ `pg_cron` integration

**Phase 8: Testing & Validation (CRITICAL)**

- [ ] **Unit Tests**

  - [ ] Test cron expression validation
  - [ ] Test job scheduling/unscheduling
  - [ ] Test job execution wrapper
  - [ ] Test service layer CRUD operations

- [ ] **Integration Tests**

  - [ ] Test create/update/delete job via API
  - [ ] Test job execution (manual and scheduled)
  - [ ] Test enable/disable functionality
  - [ ] Test `pg_cron` integration (if extension available)

- [ ] **Manual Testing**
  - [ ] Create application job and verify execution
  - [ ] Create database job and verify execution (if `pg_cron` available)
  - [ ] Test job enable/disable
  - [ ] Test job update (rescheduling)
  - [ ] Test job deletion
  - [ ] Test error handling (invalid cron expression, invalid job class)

**Estimated Time**: 5-7 days
**Dependencies**:

- PostgreSQL `pg_cron` extension (optional, for database jobs)
- Quartz Scheduler or Spring `TaskScheduler` (for dynamic scheduling)

**Frontend Dependencies:**
- Frontend will create CRUD UI for cron job management once backend APIs are complete
- See frontend TODO.md for UI implementation details

**Priority**: MEDIUM (enhancement feature)

---

#### Phase 14: User Management & Role Management APIs

**Status**: ✅ **IMPLEMENTED** - All APIs, permission checks, and business logic validations complete.

> **⚠️ HIGH PRIORITY**: Implement backend APIs for User Management and Role Management pages as defined in the permission registry.

**Implementation Summary:**

- [x] **User Management APIs** ✅ **COMPLETED**
  - [x] `GET /api/v1/admin/users` - List users with filters/pagination/search ✅
  - [x] `GET /api/v1/admin/users/{id}` - Get user details ✅
  - [x] `POST /api/v1/admin/users` - Create new user ✅
  - [x] `PUT /api/v1/admin/users/{id}` - Update user ✅
  - [x] `DELETE /api/v1/admin/users/{id}` - Delete/deactivate user ✅
  - [x] `PATCH /api/v1/admin/users/{id}/role` - Change user role ✅
  - [x] `GET /api/v1/admin/users/{id}/groups` - Get user's permission groups ✅
  - [x] `POST /api/v1/admin/users/{id}/groups` - Assign groups to user ✅
  - [x] `DELETE /api/v1/admin/users/{id}/groups/{groupId}` - Remove group from user ✅

- [x] **Role Management APIs** ✅ **COMPLETED**
  - [x] `GET /api/v1/admin/roles` - Get all roles with statistics (user count per role) ✅
  - [x] `GET /api/v1/admin/roles/{role}/users` - Get users by role ✅
  - [x] `GET /api/v1/admin/roles/{role}/permissions` - Get permissions for role (if applicable) ✅

- [x] **Permission Checks** ✅ **COMPLETED**
  - [x] Add `@PreAuthorize` checks for all endpoints:
    - [x] `users.list` - View user list ✅
    - [x] `create_user` - Create new user ✅
    - [x] `edit_user` - Edit user ✅
    - [x] `delete_user` - Delete user ✅
    - [x] `change_role` - Change user role ✅
    - [x] `users.roles` - View role management page ✅

- [x] **Business Logic** ✅ **COMPLETED**
  - [x] Prevent changing OWNER role (only one owner allowed) ✅
  - [x] Prevent deleting OWNER user ✅
  - [x] Validate role assignments (OWNER, ADMIN, USER only) ✅
  - [x] Handle user deactivation (soft delete) ✅
  - [x] Handle permission group assignments ✅

**Files Created:**
- `AdminUserController.java` - User management endpoints
- `AdminRoleController.java` - Role management endpoints
- `AdminUserCreateRequest.java` - DTO for creating users
- `AdminUserUpdateRequest.java` - DTO for updating users
- `ChangeRoleRequest.java` - DTO for role changes

**Service Methods Added:**
- `UserService.getUsersWithFilters()` - Paginated user list with search/filter
- `UserService.createUser()` - Admin user creation
- `UserService.updateUserActiveStatus()` - Activate/deactivate users

**Repository Methods Added:**
- `UserRepository.findUsersWithFilters()` - Paginated query with filters

**Frontend Dependencies:**
- Frontend expects these APIs to implement User Management (`/admin/users`) and Role Management (`/admin/roles`) pages
- See frontend TODO.md for UI implementation details

**Estimated Time**: 3-4 days
**Priority**: HIGH (completes admin interface as per permission registry)

---

#### Phase 15: Portfolio Public View APIs

**Status**: ❌ **NOT IMPLEMENTED**

**Required Implementation:**

- [ ] **Public Portfolio APIs**
  - [ ] `GET /api/v1/portfolio/public/{username}` - Get public portfolio by username (read-only)
  - [ ] `GET /api/v1/portfolio/public/{username}/projects` - Get public projects
  - [ ] `GET /api/v1/portfolio/public/{username}/skills` - Get public skills
  - [ ] `GET /api/v1/portfolio/public/{username}/experiences` - Get public experiences
  - [ ] `GET /api/v1/portfolio/public/{username}/blogs` - Get public blog posts

- [ ] **Portfolio Visibility Settings**
  - [ ] Add `isPublic` field to portfolio settings
  - [ ] Add visibility controls per portfolio section
  - [ ] Add portfolio theme/settings entity

- [ ] **Security**
  - [ ] Ensure public endpoints don't expose sensitive data
  - [ ] Add rate limiting for public endpoints
  - [ ] Validate username format and existence

**Frontend Dependencies:**
- Frontend will create public portfolio page at `/portfolio/[username]`
- See frontend TODO.md for UI implementation details

**Estimated Time**: 2-3 days
**Priority**: MEDIUM

---

#### Phase 16: Optional Improvements

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

_Last Updated: January 8, 2026_
_Next Review: January 2026_
_Current Focus: Phase 0 - RBAC System (✅ COMPLETED) → Phase 12 - Dynamic Navigation Menu (✅ COMPLETED) → Email Verification (Critical) → Two-Token Auth Testing (High) → Webhook Integration (Medium) → Dynamic Cron Job Management (Medium)_
