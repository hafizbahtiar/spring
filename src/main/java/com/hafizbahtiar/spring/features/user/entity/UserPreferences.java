package com.hafizbahtiar.spring.features.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * User Preferences entity for storing user's application preferences.
 * One-to-one relationship with User entity (one preferences per user).
 */
@Entity
@Table(name = "user_preferences", indexes = {
        @Index(name = "idx_user_preferences_user_id", columnList = "user_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class UserPreferences {

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
     * Theme preference (light, dark, system)
     */
    @Column(name = "theme", length = 20, nullable = false)
    private String theme = "system";

    /**
     * Language preference (e.g., "en", "ms", "zh")
     */
    @Column(name = "language", length = 10, nullable = false)
    private String language = "en";

    /**
     * Date format preference (e.g., "MM/DD/YYYY", "DD/MM/YYYY", "YYYY-MM-DD")
     */
    @Column(name = "date_format", length = 20, nullable = false)
    private String dateFormat = "MM/DD/YYYY";

    /**
     * Time format preference (12h or 24h)
     */
    @Column(name = "time_format", length = 5, nullable = false)
    private String timeFormat = "12h";

    /**
     * Timezone preference (e.g., "UTC", "Asia/Kuala_Lumpur", "America/New_York")
     */
    @Column(name = "timezone", length = 50, nullable = false)
    private String timezone = "UTC";

    /**
     * Default dashboard view (grid, list, table)
     */
    @Column(name = "default_dashboard_view", length = 20, nullable = false)
    private String defaultDashboardView = "grid";

    /**
     * Items per page preference (as string to allow flexibility)
     */
    @Column(name = "items_per_page", length = 10, nullable = false)
    private String itemsPerPage = "20";

    /**
     * Show widgets on dashboard
     */
    @Column(name = "show_widgets", nullable = false)
    private Boolean showWidgets = true;

    /**
     * Editor theme preference (light, dark, monokai, github)
     */
    @Column(name = "editor_theme", length = 20, nullable = false)
    private String editorTheme = "dark";

    /**
     * Editor font size (10-24)
     */
    @Column(name = "editor_font_size", nullable = false)
    private Integer editorFontSize = 14;

    /**
     * Editor line height (1.0-3.0)
     */
    @Column(name = "editor_line_height", nullable = false)
    private Double editorLineHeight = 1.5;

    /**
     * Editor tab size (2-8)
     */
    @Column(name = "editor_tab_size", nullable = false)
    private Integer editorTabSize = 4;

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
    public UserPreferences(User user) {
        this.user = user;
        this.theme = "system";
        this.language = "en";
        this.dateFormat = "MM/DD/YYYY";
        this.timeFormat = "12h";
        this.timezone = "UTC";
        this.defaultDashboardView = "grid";
        this.itemsPerPage = "20";
        this.showWidgets = true;
        this.editorTheme = "dark";
        this.editorFontSize = 14;
        this.editorLineHeight = 1.5;
        this.editorTabSize = 4;
    }
}

