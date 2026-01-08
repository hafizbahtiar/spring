package com.hafizbahtiar.spring.features.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating or updating notification preferences.
 * All fields are optional to allow partial updates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferencesRequest {

    /**
     * Email notification for account activity
     */
    private Boolean emailAccountActivity;

    /**
     * Email notification for security alerts
     */
    private Boolean emailSecurityAlerts;

    /**
     * Email notification for marketing emails
     */
    private Boolean emailMarketing;

    /**
     * Email notification for weekly digest
     */
    private Boolean emailWeeklyDigest;

    /**
     * In-app notification for system updates
     */
    private Boolean inAppSystem;

    /**
     * In-app notification for project updates
     */
    private Boolean inAppProjects;

    /**
     * In-app notification for mentions
     */
    private Boolean inAppMentions;

    /**
     * Enable push notifications (master switch)
     */
    private Boolean pushEnabled;

    /**
     * Browser push notifications
     */
    private Boolean pushBrowser;

    /**
     * Mobile push notifications
     */
    private Boolean pushMobile;
}
