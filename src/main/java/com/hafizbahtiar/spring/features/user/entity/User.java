package com.hafizbahtiar.spring.features.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_username", columnList = "username"),
        @Index(name = "idx_users_uuid", columnList = "uuid", unique = true),
        @Index(name = "idx_users_active", columnList = "active"),
        @Index(name = "idx_users_role", columnList = "role"),
        @Index(name = "idx_users_email_active", columnList = "email, active"),
        @Index(name = "idx_users_username_active", columnList = "username, active"),
        @Index(name = "idx_users_role_active", columnList = "role, active")
})
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Public UUID for user identification (exposed in APIs)
     * Generated automatically on creation via @PrePersist
     */
    @Column(name = "uuid", nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(nullable = false, length = 50, unique = true)
    private String username;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(length = 100)
    private String firstName;

    @Column(length = 100)
    private String lastName;

    @Column(length = 20)
    private String phone;

    /**
     * User bio/description
     */
    @Column(length = 1000)
    private String bio;

    /**
     * User location
     */
    @Column(length = 200)
    private String location;

    /**
     * User website URL
     */
    @Column(length = 500)
    private String website;

    /**
     * User avatar URL
     */
    @Column(length = 500)
    private String avatarUrl;

    @Column(nullable = false, length = 20)
    private String role = "USER";

    @Column(nullable = false)
    private Boolean emailVerified = false;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime lastLoginAt;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    /**
     * Generate UUID before persisting if not already set
     */
    @PrePersist
    protected void generateUuid() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }
    }

    // Constructor for registration
    public User(String email, String username, String passwordHash) {
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = "USER";
        this.emailVerified = false;
        this.active = true;
    }

    // Business methods
    public boolean isActive() {
        return Boolean.TRUE.equals(this.active);
    }

    public boolean isEmailVerified() {
        return Boolean.TRUE.equals(this.emailVerified);
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public void verifyEmail() {
        this.emailVerified = true;
    }

    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", role='" + role + '\'' +
                ", emailVerified=" + emailVerified +
                ", active=" + active +
                ", createdAt=" + createdAt +
                '}';
    }
}
