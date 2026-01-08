package com.hafizbahtiar.spring.features.portfolio.entity;

import com.hafizbahtiar.spring.features.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Skill entity for storing user skills.
 * Represents a skill that a user has, with category and proficiency level.
 */
@Entity
@Table(name = "portfolio_skills", indexes = {
        @Index(name = "idx_portfolio_skills_user_id", columnList = "user_id"),
        @Index(name = "idx_portfolio_skills_category", columnList = "category"),
        @Index(name = "idx_portfolio_skills_is_active", columnList = "is_active"),
        @Index(name = "idx_portfolio_skills_user_category", columnList = "user_id, category"),
        @Index(name = "idx_portfolio_skills_display_order", columnList = "display_order")
})
@Getter
@Setter
@NoArgsConstructor
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who owns this skill
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Skill name (e.g., "Java", "React", "Communication")
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Skill category (TECHNICAL, SOFT, LANGUAGE, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private SkillCategory category;

    /**
     * Proficiency level (BEGINNER, INTERMEDIATE, ADVANCED, EXPERT)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "proficiency", nullable = false, length = 50)
    private ProficiencyLevel proficiency;

    /**
     * Icon URL or icon identifier (optional)
     */
    @Column(name = "icon", length = 255)
    private String icon;

    /**
     * Skill description (optional)
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Display order for sorting skills
     */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    /**
     * Whether this skill is active (visible)
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /**
     * Optimistic locking version
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    /**
     * Timestamp when skill was created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when skill was last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructor for creating a new skill
     */
    public Skill(User user, String name, SkillCategory category, ProficiencyLevel proficiency) {
        this.user = user;
        this.name = name;
        this.category = category;
        this.proficiency = proficiency;
        this.isActive = true;
        this.displayOrder = 0;
    }

    // Business methods

    /**
     * Check if skill is active
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(this.isActive);
    }

    /**
     * Deactivate skill
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * Activate skill
     */
    public void activate() {
        this.isActive = true;
    }

    /**
     * Check if proficiency is at least intermediate
     */
    public boolean isAtLeastIntermediate() {
        return proficiency != null && proficiency.isAtLeastIntermediate();
    }

    /**
     * Check if proficiency is advanced or expert
     */
    public boolean isAdvancedOrExpert() {
        return proficiency != null && proficiency.isAdvancedOrExpert();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Skill skill = (Skill) o;
        return id != null && id.equals(skill.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Skill{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", proficiency=" + proficiency +
                ", isActive=" + isActive +
                ", displayOrder=" + displayOrder +
                ", createdAt=" + createdAt +
                '}';
    }
}
