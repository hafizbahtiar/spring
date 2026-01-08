package com.hafizbahtiar.spring.features.permissions.entity;

import com.hafizbahtiar.spring.features.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * PermissionGroup entity representing a group of users with shared permissions.
 * Groups can be created by OWNER or ADMIN users and assigned fine-grained
 * permissions
 * at module, page, and component levels.
 */
@Entity
@Table(name = "permission_groups", indexes = {
        @Index(name = "idx_permission_groups_created_by", columnList = "created_by"),
        @Index(name = "idx_permission_groups_active", columnList = "active"),
        @Index(name = "idx_permission_groups_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
public class PermissionGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Group name (must be unique)
     */
    @Column(name = "name", nullable = false, length = 100, unique = true)
    private String name;

    /**
     * Group description
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * User who created this group
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    /**
     * Whether this group is active (inactive groups don't grant permissions)
     */
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    /**
     * Permissions assigned to this group
     */
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupPermission> permissions = new ArrayList<>();

    /**
     * Users assigned to this group
     */
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserGroup> userGroups = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Business method to check if group is active
     */
    public boolean isActive() {
        return active != null && active;
    }

    /**
     * Business method to activate the group
     */
    public void activate() {
        this.active = true;
    }

    /**
     * Business method to deactivate the group
     */
    public void deactivate() {
        this.active = false;
    }
}
