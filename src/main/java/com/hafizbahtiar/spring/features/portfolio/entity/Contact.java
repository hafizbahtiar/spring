package com.hafizbahtiar.spring.features.portfolio.entity;

import com.hafizbahtiar.spring.features.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * Contact entity for storing contact messages and inquiries.
 * Represents contact form submissions, emails, and other contact sources.
 */
@Entity
@Table(name = "portfolio_contacts", indexes = {
        @Index(name = "idx_portfolio_contacts_user_id", columnList = "user_id"),
        @Index(name = "idx_portfolio_contacts_email", columnList = "email"),
        @Index(name = "idx_portfolio_contacts_status", columnList = "status"),
        @Index(name = "idx_portfolio_contacts_source", columnList = "source"),
        @Index(name = "idx_portfolio_contacts_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who owns this contact (the person receiving the contact)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Contact name
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * Contact email
     */
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    /**
     * Contact phone number (optional)
     */
    @Column(name = "phone", length = 50)
    private String phone;

    /**
     * Message subject
     */
    @Column(name = "subject", nullable = false, length = 500)
    private String subject;

    /**
     * Message content
     */
    @Column(name = "message", nullable = false, length = 5000)
    private String message;

    /**
     * Contact status (NEW, READ, REPLIED, ARCHIVED)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ContactStatus status = ContactStatus.NEW;

    /**
     * Contact source (FORM, EMAIL, SOCIAL, OTHER)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 50)
    private ContactSource source = ContactSource.FORM;

    /**
     * Additional metadata (stored as JSON)
     * Can include IP address, user agent, referrer, etc.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Object metadata;

    /**
     * Timestamp when contact was read (optional)
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    /**
     * Timestamp when contact was replied to (optional)
     */
    @Column(name = "replied_at")
    private LocalDateTime repliedAt;

    /**
     * Optimistic locking version
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    /**
     * Timestamp when contact was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when contact was last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructor for creating a new contact
     */
    public Contact(User user, String name, String email, String subject, String message) {
        this.user = user;
        this.name = name;
        this.email = email;
        this.subject = subject;
        this.message = message;
        this.status = ContactStatus.NEW;
        this.source = ContactSource.FORM;
    }

    // Business methods

    /**
     * Check if contact is new (not read)
     */
    public boolean isNew() {
        return status == ContactStatus.NEW;
    }

    /**
     * Check if contact has been read
     */
    public boolean isRead() {
        return status == ContactStatus.READ || status == ContactStatus.REPLIED;
    }

    /**
     * Check if contact has been replied to
     */
    public boolean isReplied() {
        return status == ContactStatus.REPLIED;
    }

    /**
     * Check if contact is archived
     */
    public boolean isArchived() {
        return status == ContactStatus.ARCHIVED;
    }

    /**
     * Mark contact as read
     */
    public void markAsRead() {
        if (this.status == ContactStatus.NEW) {
            this.status = ContactStatus.READ;
            this.readAt = LocalDateTime.now();
        }
    }

    /**
     * Mark contact as replied
     */
    public void markAsReplied() {
        this.status = ContactStatus.REPLIED;
        this.repliedAt = LocalDateTime.now();
        // Set readAt if not already set
        if (this.readAt == null) {
            this.readAt = LocalDateTime.now();
        }
    }

    /**
     * Archive contact
     */
    public void archive() {
        this.status = ContactStatus.ARCHIVED;
    }

    /**
     * Unarchive contact (set back to READ if previously replied, otherwise NEW)
     */
    public void unarchive() {
        if (this.repliedAt != null) {
            this.status = ContactStatus.REPLIED;
        } else if (this.readAt != null) {
            this.status = ContactStatus.READ;
        } else {
            this.status = ContactStatus.NEW;
        }
    }

    /**
     * Get display name (contact name)
     */
    public String getDisplayName() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Contact contact = (Contact) o;
        return id != null && id.equals(contact.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Contact{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", subject='" + subject + '\'' +
                ", status=" + status +
                ", source=" + source +
                ", createdAt=" + createdAt +
                '}';
    }
}
