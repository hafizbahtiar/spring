package com.hafizbahtiar.spring.features.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Notification Preferences entity for storing user's notification preferences.
 * One-to-one relationship with User entity (one preferences per user).
 */
@Entity
@Table(name = "notification_preferences", indexes = {
        @Index(name = "idx_notification_preferences_user_id", columnList = "user_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class NotificationPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who owns these preferences (one-to-one relationship)
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * Email notification for account activity (login attempts, password changes, etc.)
     */
    @Column(name = "email_account_activity", nullable = false)
    private Boolean emailAccountActivity = true;

    /**
     * Email notification for security alerts
     */
    @Column(name = "email_security_alerts", nullable = false)
    private Boolean emailSecurityAlerts = true;

    /**
     * Email notification for marketing emails
     */
    @Column(name = "email_marketing", nullable = false)
    private Boolean emailMarketing = false;

    /**
     * Email notification for weekly digest
     */
    @Column(name = "email_weekly_digest", nullable = false)
    private Boolean emailWeeklyDigest = false;

    /**
     * In-app notification for system updates
     */
    @Column(name = "in_app_system", nullable = false)
    private Boolean inAppSystem = true;

    /**
     * In-app notification for project updates
     */
    @Column(name = "in_app_projects", nullable = false)
    private Boolean inAppProjects = true;

    /**
     * In-app notification for mentions
     */
    @Column(name = "in_app_mentions", nullable = false)
    private Boolean inAppMentions = true;

    /**
     * Enable push notifications (master switch)
     */
    @Column(name = "push_enabled", nullable = false)
    private Boolean pushEnabled = false;

    /**
     * Browser push notifications
     */
    @Column(name = "push_browser", nullable = false)
    private Boolean pushBrowser = true;

    /**
     * Mobile push notifications
     */
    @Column(name = "push_mobile", nullable = false)
    private Boolean pushMobile = false;

    /**
     * Optimistic locking version
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    /**
     * Timestamp when preferences were created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when preferences were last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructor for creating default preferences for a user
     */
    public NotificationPreferences(User user) {
        this.user = user;
        this.emailAccountActivity = true;
        this.emailSecurityAlerts = true;
        this.emailMarketing = false;
        this.emailWeeklyDigest = false;
        this.inAppSystem = true;
        this.inAppProjects = true;
        this.inAppMentions = true;
        this.pushEnabled = false;
        this.pushBrowser = true;
        this.pushMobile = false;
    }
}

