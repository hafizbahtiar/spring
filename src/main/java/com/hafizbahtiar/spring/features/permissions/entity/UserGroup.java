package com.hafizbahtiar.spring.features.permissions.entity;

import com.hafizbahtiar.spring.features.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * UserGroup entity representing the many-to-many relationship between users and
 * permission groups.
 * Tracks which users are assigned to which groups and who assigned them.
 */
@Entity
@Table(name = "user_groups", indexes = {
        @Index(name = "idx_user_groups_user_id", columnList = "user_id"),
        @Index(name = "idx_user_groups_group_id", columnList = "group_id"),
        @Index(name = "idx_user_groups_assigned_by", columnList = "assigned_by")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_group_unique", columnNames = { "user_id", "group_id" })
})
@Getter
@Setter
@NoArgsConstructor
public class UserGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User assigned to the group
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Permission group the user is assigned to
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private PermissionGroup group;

    /**
     * User who assigned this user to the group
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by", nullable = false)
    private User assignedBy;

    @CreationTimestamp
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;
}
