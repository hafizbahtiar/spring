package com.hafizbahtiar.spring.features.auth.entity;

import com.hafizbahtiar.spring.features.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Session entity for tracking user authentication sessions.
 * Tracks active sessions across different devices and browsers.
 */
@Entity
@Table(name = "user_sessions", indexes = {
        @Index(name = "idx_user_sessions_user_id", columnList = "user_id"),
        @Index(name = "idx_user_sessions_session_id", columnList = "session_id", unique = true),
        @Index(name = "idx_user_sessions_is_active", columnList = "is_active"),
        @Index(name = "idx_user_sessions_user_active", columnList = "user_id, is_active"),
        @Index(name = "idx_user_sessions_refresh_token_expires_at", columnList = "refresh_token_expires_at")
})
@Getter
@Setter
@NoArgsConstructor
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who owns this session
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Unique session identifier (UUID)
     */
    @Column(name = "session_id", nullable = false, unique = true, length = 36)
    private String sessionId;

    /**
     * User agent string from HTTP request
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * IP address of the client
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * Device type (mobile, tablet, desktop)
     */
    @Column(name = "device_type", length = 20)
    private String deviceType;

    /**
     * Device name (e.g., "iPhone 13", "MacBook Pro")
     */
    @Column(name = "device_name", length = 100)
    private String deviceName;

    /**
     * Browser name (e.g., "Chrome", "Firefox", "Safari")
     */
    @Column(name = "browser", length = 50)
    private String browser;

    /**
     * Operating system (e.g., "Windows", "macOS", "iOS", "Android")
     */
    @Column(name = "os", length = 50)
    private String os;

    /**
     * Country code (e.g., "MY", "US", "GB")
     */
    @Column(name = "country", length = 10)
    private String country;

    /**
     * Region/state (e.g., "Selangor", "California")
     */
    @Column(name = "region", length = 100)
    private String region;

    /**
     * City name
     */
    @Column(name = "city", length = 100)
    private String city;

    /**
     * Latitude coordinate
     */
    @Column(name = "latitude")
    private Double latitude;

    /**
     * Longitude coordinate
     */
    @Column(name = "longitude")
    private Double longitude;

    /**
     * Timezone (e.g., "Asia/Kuala_Lumpur", "America/New_York")
     */
    @Column(name = "timezone", length = 50)
    private String timezone;

    /**
     * ISP (Internet Service Provider)
     */
    @Column(name = "isp", length = 200)
    private String isp;

    /**
     * Whether this session is currently active
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Last activity timestamp (updated on each request)
     */
    @Column(name = "last_activity_at", nullable = false)
    private LocalDateTime lastActivityAt;

    /**
     * Refresh token expiration timestamp (7 days from creation)
     * Used for two-token authentication strategy
     */
    @Column(name = "refresh_token_expires_at", nullable = true)
    private LocalDateTime refreshTokenExpiresAt;

    /**
     * Timestamp when session was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when session was last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructor for creating a new session
     */
    public Session(User user, String userAgent, String ipAddress) {
        this.user = user;
        this.sessionId = UUID.randomUUID().toString();
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
        this.isActive = true;
        this.lastActivityAt = LocalDateTime.now();
        // Set refresh token expiration to 7 days from creation
        this.refreshTokenExpiresAt = LocalDateTime.now().plusDays(7);
    }

    /**
     * Revoke this session (mark as inactive)
     */
    public void revoke() {
        this.isActive = false;
        this.lastActivityAt = LocalDateTime.now();
    }

    /**
     * Update last activity timestamp
     */
    public void updateActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }
}
