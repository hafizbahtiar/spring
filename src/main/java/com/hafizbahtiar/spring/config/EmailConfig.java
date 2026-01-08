package com.hafizbahtiar.spring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Email configuration properties.
 * Maps properties from application.properties with prefix "app.email"
 */
@Configuration
@ConfigurationProperties(prefix = "app.email")
@Data
public class EmailConfig {

    /**
     * From email address (e.g., noreply@yourapp.com)
     */
    private String from = "noreply@yourapp.com";

    /**
     * Frontend URL for email links (e.g., http://localhost:3000)
     */
    private String frontendUrl = "http://localhost:3000";

    /**
     * Application name for email templates
     */
    private String appName = "Console";
}
