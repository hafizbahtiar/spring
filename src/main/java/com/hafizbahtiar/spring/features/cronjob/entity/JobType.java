package com.hafizbahtiar.spring.features.cronjob.entity;

/**
 * Enum representing the type of cron job.
 * 
 * - APPLICATION: Spring application-level jobs that execute within the Spring Boot context
 *   and can access Spring services and beans.
 * - DATABASE: PostgreSQL pg_cron jobs that execute SQL scripts directly in the database,
 *   independent of the application lifecycle.
 */
public enum JobType {
    /**
     * Application-level job executed by Spring's TaskScheduler or Quartz Scheduler.
     * Can access Spring services and beans.
     */
    APPLICATION,

    /**
     * Database-level job executed by PostgreSQL pg_cron extension.
     * Executes SQL scripts directly in the database.
     */
    DATABASE
}
