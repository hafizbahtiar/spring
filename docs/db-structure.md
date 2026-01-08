# Database Structure Documentation

This document outlines the database architecture and schema design for the Spring Boot application, which uses a dual-database approach with PostgreSQL as the primary database and MongoDB as the secondary database.

## Dependencies

### Maven Dependencies
```xml
<!-- JPA/Hibernate for PostgreSQL -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- PostgreSQL JDBC Driver -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>

<!-- MongoDB Support -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>

<!-- H2 for testing/development -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

## Architecture Overview

### Dual-Database Design

#### Primary Database: PostgreSQL
- **Purpose**: Core business data, transactions, complex relationships
- **Technology**: Spring Data JPA + Hibernate
- **Use Cases**: Users, products, orders, payments, inventory
- **Characteristics**: ACID compliance, relational integrity, complex queries

#### Secondary Database: MongoDB
- **Purpose**: Logs, audit trails, analytics, unstructured data
- **Technology**: Spring Data MongoDB
- **Use Cases**: Application logs, webhook payloads, user activity, analytics
- **Characteristics**: Flexible schemas, high write throughput, document storage

## PostgreSQL Schema Design

### Database Configuration
```sql
-- Database creation
CREATE DATABASE console;
CREATE USER console WITH ENCRYPTED PASSWORD 'console_2025!';
GRANT ALL PRIVILEGES ON DATABASE console TO console;

-- Connect to the database
\c console;
```

### Current Focus: User Management Tables

#### Users Table (Primary Focus)
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

-- Indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_active ON users(active);
CREATE INDEX idx_users_role ON users(role);
```

#### Products Table
```sql
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    stock_quantity INTEGER NOT NULL CHECK (stock_quantity >= 0),
    category VARCHAR(50) NOT NULL,
    sku VARCHAR(50) UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

-- Indexes
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_active ON products(active);
CREATE INDEX idx_products_price ON products(price);
CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_name ON products USING gin(to_tsvector('english', name));
```

#### Orders Table
```sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    total_amount DECIMAL(12,2) NOT NULL CHECK (total_amount >= 0),
    shipping_address JSONB,
    billing_address JSONB,
    payment_method VARCHAR(50),
    payment_status VARCHAR(20) DEFAULT 'PENDING',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

-- Indexes
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_payment_status ON orders(payment_status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_orders_order_number ON orders(order_number);
```

#### Order Items Table
```sql
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10,2) NOT NULL CHECK (unit_price >= 0),
    total_price DECIMAL(10,2) NOT NULL CHECK (total_price >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);
```

#### Payments Table
```sql
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id),
    payment_provider VARCHAR(50) NOT NULL, -- PAYPAL, STRIPE
    provider_payment_id VARCHAR(255) UNIQUE,
    amount DECIMAL(12,2) NOT NULL CHECK (amount >= 0),
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(20) NOT NULL,
    payment_method VARCHAR(50),
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

-- Indexes
CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_provider ON payments(payment_provider);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_provider_payment_id ON payments(provider_payment_id);
```

### Current Entity Relationships

```
Users (Primary Focus)
```

### Future Entity Relationships (To Be Implemented)

```
Users (1) ──── (N) Orders (1) ──── (N) Order Items (N) ──── (1) Products
                   │                      │
                   └──────── (1) Payments ─┘
```

### Constraints and Business Rules

#### Check Constraints
```sql
-- Price validation
ALTER TABLE products ADD CONSTRAINT chk_price_positive CHECK (price >= 0);
ALTER TABLE payments ADD CONSTRAINT chk_payment_amount_positive CHECK (amount >= 0);

-- Quantity validation
ALTER TABLE products ADD CONSTRAINT chk_stock_non_negative CHECK (stock_quantity >= 0);
ALTER TABLE order_items ADD CONSTRAINT chk_quantity_positive CHECK (quantity > 0);

-- Status validation
ALTER TABLE orders ADD CONSTRAINT chk_order_status CHECK (status IN ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED'));
ALTER TABLE payments ADD CONSTRAINT chk_payment_status CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REFUNDED', 'CANCELLED'));
```

#### Triggers for Audit
```sql
-- Updated timestamp trigger
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply to all tables
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_products_updated_at BEFORE UPDATE ON products FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_orders_updated_at BEFORE UPDATE ON orders FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_payments_updated_at BEFORE UPDATE ON payments FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
```

## MongoDB Collection Design

### Database: console

#### Application Logs Collection
```javascript
// Collection: application_logs
{
  _id: ObjectId,
  timestamp: ISODate,
  level: "INFO|WARN|ERROR",
  logger: "com.hafizbahtiar.spring...",
  message: "Log message",
  thread: "http-nio-8080-exec-1",
  exception: {
    class: "ExceptionClass",
    message: "Exception message",
    stackTrace: ["stack", "trace", "lines"]
  },
  context: {
    userId: 123,
    requestId: "req-12345",
    sessionId: "sess-67890",
    endpoint: "/api/v1/products",
    method: "GET",
    ipAddress: "192.168.1.100",
    userAgent: "Mozilla/5.0..."
  },
  additionalData: {} // Flexible field for extra context
}

// Indexes
db.application_logs.createIndex({timestamp: -1});
db.application_logs.createIndex({level: 1, timestamp: -1});
db.application_logs.createIndex({"context.userId": 1, timestamp: -1});
db.application_logs.createIndex({"context.requestId": 1});
db.application_logs.createIndex({logger: 1, timestamp: -1});
```

#### Webhook Logs Collection
```javascript
// Collection: webhook_logs
{
  _id: ObjectId,
  timestamp: ISODate,
  provider: "PAYPAL|STRIPE",
  eventType: "PAYMENT.CAPTURE.COMPLETED",
  webhookId: "wh-12345",
  payload: {}, // Full webhook payload
  headers: {
    "PayPal-Transmission-Id": "...",
    "Stripe-Signature": "..."
  },
  processingStatus: "RECEIVED|PROCESSING|SUCCESS|FAILED",
  processingTimeMs: 150,
  error: {
    code: "VALIDATION_ERROR",
    message: "Invalid signature",
    details: {}
  },
  retryCount: 0,
  orderId: 123, // If associated with order
  userId: 456   // If associated with user
}

// Indexes
db.webhook_logs.createIndex({timestamp: -1});
db.webhook_logs.createIndex({provider: 1, timestamp: -1});
db.webhook_logs.createIndex({eventType: 1, timestamp: -1});
db.webhook_logs.createIndex({processingStatus: 1, timestamp: -1});
db.webhook_logs.createIndex({orderId: 1, timestamp: -1});
db.webhook_logs.createIndex({webhookId: 1});
```

#### User Activity Collection
```javascript
// Collection: user_activity
{
  _id: ObjectId,
  userId: 123,
  sessionId: "sess-67890",
  activityType: "LOGIN|LOGOUT|PAGE_VIEW|API_CALL|PURCHASE",
  timestamp: ISODate,
  details: {
    endpoint: "/api/v1/products/456",
    method: "GET",
    responseStatus: 200,
    responseTimeMs: 45,
    userAgent: "Mozilla/5.0...",
    ipAddress: "192.168.1.100"
  },
  metadata: {} // Additional context-specific data
}

// Indexes
db.user_activity.createIndex({userId: 1, timestamp: -1});
db.user_activity.createIndex({sessionId: 1, timestamp: -1});
db.user_activity.createIndex({activityType: 1, timestamp: -1});
db.user_activity.createIndex({timestamp: -1});
```

#### Audit Trail Collection
```javascript
// Collection: audit_trail
{
  _id: ObjectId,
  timestamp: ISODate,
  userId: 123,
  action: "CREATE|UPDATE|DELETE",
  entityType: "Product|Order|User",
  entityId: 456,
  changes: {
    oldValues: {name: "Old Name", price: 10.99},
    newValues: {name: "New Name", price: 12.99},
    changedFields: ["name", "price"]
  },
  ipAddress: "192.168.1.100",
  userAgent: "Mozilla/5.0...",
  requestId: "req-12345",
  sessionId: "sess-67890"
}

// Indexes
db.audit_trail.createIndex({userId: 1, timestamp: -1});
db.audit_trail.createIndex({entityType: 1, entityId: 1, timestamp: -1});
db.audit_trail.createIndex({action: 1, timestamp: -1});
db.audit_trail.createIndex({timestamp: -1});
db.audit_trail.createIndex({requestId: 1});
```

## Schema Management Strategy

### Current Approach: Hibernate Auto DDL

The application currently uses **Hibernate's automatic schema management** for development simplicity. Hibernate automatically creates and updates database tables based on your `@Entity` class definitions.

#### Configuration
```properties
# In application.properties
spring.jpa.hibernate.ddl-auto=update
spring.flyway.enabled=false
```

#### How It Works
- **On Application Start**: Hibernate scans all `@Entity` classes
- **Schema Creation**: Automatically creates tables if they don't exist
- **Schema Updates**: Updates existing tables when entity definitions change
- **Data Preservation**: Existing data is preserved during updates

#### Benefits
- ✅ **Rapid Development** - No migration scripts needed during development
- ✅ **Automatic Sync** - Schema always matches entity definitions
- ✅ **Simplified Workflow** - Just update entities and restart application

#### Limitations
- ⚠️ **Development Only** - Not recommended for production
- ⚠️ **Limited Control** - Less control over exact schema structure
- ⚠️ **No Versioning** - No migration history or rollback capability

### Future Migration Strategy (Production)

For production environments, consider migrating to **Flyway** for better control and versioning:

#### Flyway Configuration (Future)
```properties
# For production use
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
spring.jpa.hibernate.ddl-auto=validate  # Validate only, don't create
```

#### Migration File Structure (Available but Disabled)
```
src/main/resources/db/migration/
├── V1.0.0__Initial_schema.sql      # Core tables, indexes, constraints, initial data
├── V1.0.1__Add_indexes.sql         # Performance indexes and query optimization
├── V1.0.2__Add_constraints.sql     # Business rules and data integrity
└── V2.0.0__Add_payment_metadata.sql # Future: Enhanced payment metadata
```

**Note**: Migration files exist in the codebase but are currently disabled. They can be re-enabled for production use when needed.

### Migration Example (Reference)
```sql
-- V1.0.0__Initial_schema.sql (Reference - Currently Not Used)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    -- ... other columns
);

CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    -- ... other columns
);

-- Add indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_products_name ON products(name);
```

## Performance Optimization

### PostgreSQL Optimizations

#### Partitioning Strategy
```sql
-- Partition orders by month for better performance
CREATE TABLE orders_y2024m01 PARTITION OF orders
    FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

-- Create partitions for future months
CREATE TABLE orders_y2024m02 PARTITION OF orders
    FOR VALUES FROM ('2024-02-01') TO ('2024-03-01');
```

#### Query Optimization
```sql
-- Use EXPLAIN ANALYZE to identify slow queries
EXPLAIN ANALYZE SELECT * FROM orders WHERE user_id = 123;

-- Add composite indexes for common query patterns
CREATE INDEX idx_orders_user_status ON orders(user_id, status);
CREATE INDEX idx_products_category_price ON products(category, price);
```

### MongoDB Optimizations

#### Sharding Strategy
```javascript
// Enable sharding on console database
sh.enableSharding("console");

// Shard large collections
sh.shardCollection("console.application_logs", {timestamp: 1});
sh.shardCollection("console.user_activity", {userId: 1, timestamp: 1});
```

#### Aggregation Pipeline Optimization
```javascript
// Optimized aggregation for user activity analytics
db.user_activity.aggregate([
  {$match: {userId: 123, timestamp: {$gte: ISODate("2024-01-01")}}},
  {$group: {_id: "$activityType", count: {$sum: 1}}},
  {$sort: {count: -1}}
], {allowDiskUse: true});
```

## Backup and Recovery

### PostgreSQL Backup Strategy
```bash
# Daily backup
pg_dump spring_db > spring_db_$(date +%Y%m%d).sql

# Compressed backup
pg_dump spring_db | gzip > spring_db_$(date +%Y%m%d).sql.gz

# Point-in-time recovery
pg_dump --format=custom --compress=9 spring_db > spring_db_$(date +%Y%m%d).backup
```

### MongoDB Backup Strategy
```bash
# Database backup
mongodump --db console --out /backup/mongo_$(date +%Y%m%d)

# Collection-specific backup
mongodump --db console --collection application_logs --out /backup/logs_$(date +%Y%m%d)

# Compressed backup
mongodump --db console --gzip --out /backup/compressed_$(date +%Y%m%d)
```

## Monitoring and Maintenance

### Health Checks
```yaml
# Application monitoring endpoints
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,info
  health:
    db:
      enabled: true
    mongo:
      enabled: true
```

### Key Metrics to Monitor

#### PostgreSQL Metrics
- Connection pool utilization
- Slow query performance
- Table/index bloat
- Replication lag (if applicable)

#### MongoDB Metrics
- Connection pool statistics
- Operation latency
- Index usage
- Storage utilization

### Maintenance Tasks

#### PostgreSQL Maintenance
```sql
-- Analyze tables for query optimization
ANALYZE VERBOSE;

-- Vacuum tables to reclaim space
VACUUM (VERBOSE, ANALYZE);

-- Reindex tables if needed
REINDEX TABLE CONCURRENTLY products;
```

#### MongoDB Maintenance
```javascript
// Compact collections
db.application_logs.compact();

// Build missing indexes
db.user_activity.createIndex({timestamp: -1});

// Validate collection integrity
db.webhook_logs.validate({full: true});
```

## Best Practices

### Data Integrity
1. Always use transactions for multi-table operations
2. Implement optimistic locking with version fields
3. Validate data at application and database levels
4. Use appropriate constraints and foreign keys

### Performance
1. Design indexes based on actual query patterns
2. Monitor slow queries and optimize accordingly
3. Use connection pooling appropriately
4. Archive old data to maintain performance

### Security
1. Use parameterized queries to prevent SQL injection
2. Implement proper authentication and authorization
3. Encrypt sensitive data at rest and in transit
4. Regular security audits and updates

### Scalability
1. Design for horizontal scaling when possible
2. Use read replicas for read-heavy workloads
3. Implement caching strategies
4. Plan for data archiving and partitioning

This database structure provides a solid foundation for the Spring Boot application, balancing the strengths of both PostgreSQL and MongoDB for optimal data management.
