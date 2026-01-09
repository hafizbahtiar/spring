# Database Migration Instructions: Adding UUID Columns

## Problem
The application is trying to query the `uuid` column from the `users` and `portfolio_profiles` tables, but these columns don't exist in the database yet.

## Solution
Run the migration SQL script to add the UUID columns.

## Option 1: Using Flyway (Recommended - Automatic)

Flyway has been temporarily enabled in `application.properties`. Simply start the application and Flyway will automatically run the migration:

```bash
mvn spring-boot:run
```

After the migration completes successfully, you can disable Flyway again by setting `spring.flyway.enabled=false` in `application.properties` if you prefer to use Hibernate DDL auto.

## Option 2: Manual SQL Execution

If you prefer to run the migration manually, connect to your PostgreSQL database and execute the SQL script:

```bash
# Connect to your database (replace with your actual connection details)
psql -h localhost -U your_username -d your_database_name -f src/main/resources/db/migration/V1__add_uuid_columns.sql
```

Or copy and paste the SQL from `src/main/resources/db/migration/V1__add_uuid_columns.sql` into your database client.

## What the Migration Does

1. Adds `uuid` column to `users` table
2. Creates unique index on `users.uuid`
3. Generates UUIDs for existing users (if any)
4. Makes `uuid` NOT NULL
5. Repeats the same steps for `portfolio_profiles` table

## Verification

After running the migration, verify the columns exist:

```sql
-- Check users table
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'users' AND column_name = 'uuid';

-- Check portfolio_profiles table
SELECT column_name, data_type, is_nullable 
FROM information_schema.columns 
WHERE table_name = 'portfolio_profiles' AND column_name = 'uuid';
```

Both should show `uuid` column with `data_type = 'uuid'` and `is_nullable = 'NO'`.
