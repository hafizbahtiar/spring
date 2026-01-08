package com.hafizbahtiar.spring.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration for asynchronous processing and scheduling.
 * Enables @Async annotation support for background task execution.
 * Enables @Scheduled annotation support for periodic tasks.
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
    // Async configuration - uses default executor
    // Can be customized here if needed (thread pool size, etc.)
}
