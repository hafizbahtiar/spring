package com.hafizbahtiar.spring.features.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Currency Preferences entity for storing user's currency preferences.
 * One-to-one relationship with User entity (one preferences per user).
 */
@Entity
@Table(name = "currency_preferences", indexes = {
        @Index(name = "idx_currency_preferences_user_id", columnList = "user_id", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
public class CurrencyPreferences {

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
     * Base currency (e.g., "MYR", "USD", "EUR")
     */
    @Column(name = "base_currency", length = 10, nullable = false)
    private String baseCurrency = "MYR";

    /**
     * Supported currencies list (stored as JSONB in PostgreSQL)
     * List of currency codes the user wants to work with
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "supported_currencies", columnDefinition = "jsonb")
    private List<String> supportedCurrencies;

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
    public CurrencyPreferences(User user) {
        this.user = user;
        this.baseCurrency = "MYR";
        this.supportedCurrencies = List.of("MYR");
    }
}
