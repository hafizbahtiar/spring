package com.hafizbahtiar.spring.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * MongoDB document for email event logging.
 * Stores email sending events for audit, analytics, and debugging purposes.
 * Logs both successful and failed email sending attempts.
 */
@Document(collection = "email_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailLog {

    @Id
    private String id;

    /**
     * Recipient email address
     */
    @Indexed
    private String to;

    /**
     * Email subject
     */
    private String subject;

    /**
     * Template name used (if template-based email)
     * Values: password-reset, email-verification, welcome, etc.
     */
    @Indexed
    private String templateName;

    /**
     * Email type
     * Values: PLAIN_TEXT, HTML, TEMPLATED
     */
    private String emailType;

    /**
     * Timestamp when the email was sent (or attempted)
     */
    @Indexed
    private LocalDateTime sentAt;

    /**
     * Status of the email sending attempt
     * Values: SENT, FAILED
     */
    @Indexed
    private String status;

    /**
     * Error message (if status is FAILED)
     */
    private String errorMessage;

    /**
     * User ID associated with the email (if applicable)
     * e.g., password reset emails, email verification emails
     */
    @Indexed
    private Long userId;

    /**
     * IP address of the client that triggered the email (if applicable)
     */
    private String ipAddress;

    /**
     * User agent string from the request (if applicable)
     */
    private String userAgent;

    /**
     * Request ID for tracing (if applicable)
     */
    @Indexed
    private String requestId;

    /**
     * Additional metadata as flexible JSON structure
     * Can include template variables, email provider info, etc.
     */
    private Object metadata;

    /**
     * Response time in milliseconds (for performance tracking)
     */
    private Long responseTimeMs;
}

