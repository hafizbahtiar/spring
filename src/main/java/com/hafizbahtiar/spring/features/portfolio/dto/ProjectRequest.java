package com.hafizbahtiar.spring.features.portfolio.dto;

import com.hafizbahtiar.spring.features.portfolio.entity.PlatformType;
import com.hafizbahtiar.spring.features.portfolio.entity.ProjectStatus;
import com.hafizbahtiar.spring.features.portfolio.entity.ProjectType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for creating or updating a project.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequest {

    @NotBlank(message = "Project title is required")
    @Size(max = 200, message = "Project title must not exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    /**
     * Technologies used (list of strings)
     * Example: ["Java", "Spring Boot", "PostgreSQL", "React"]
     */
    private List<String> technologies;

    @Size(max = 500, message = "GitHub URL must not exceed 500 characters")
    private String githubUrl;

    @Size(max = 500, message = "Live URL must not exceed 500 characters")
    private String liveUrl;

    /**
     * Project image URL (optional) - kept for backward compatibility
     * @deprecated Use images list instead
     */
    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    @Deprecated
    private String imageUrl;

    /**
     * Project images (list of image URLs)
     * Example: ["https://example.com/image1.jpg", "https://example.com/image2.jpg"]
     * Used for displaying multiple images like Google Play Store screenshots
     */
    private List<String> images;

    /**
     * Project platform type (WEB, ANDROID, IOS, DESKTOP, MULTI_PLATFORM, OTHER)
     */
    private PlatformType platform;

    /**
     * Project roadmap/timeline (list of timeline events)
     * Each event should have: date, title, description (optional)
     * Example: [
     *   {"date": "2024-01-01", "title": "Project Started", "description": "Initial planning"},
     *   {"date": "2024-02-01", "title": "MVP Released", "description": "First version launched"}
     * ]
     */
    private List<RoadmapItem> roadmap;

    /**
     * Case study content (detailed project analysis, challenges, solutions, results)
     */
    @Size(max = 50000, message = "Case study must not exceed 50000 characters")
    private String caseStudy;

    /**
     * Inner class for roadmap timeline items
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoadmapItem {
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate date;
        
        @NotBlank(message = "Roadmap item title is required")
        @Size(max = 200, message = "Roadmap item title must not exceed 200 characters")
        private String title;
        
        @Size(max = 1000, message = "Roadmap item description must not exceed 1000 characters")
        private String description;
    }

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    private Double latitude;

    private Double longitude;

    @NotNull(message = "Project type is required")
    private ProjectType type;

    @NotNull(message = "Project status is required")
    private ProjectStatus status;

    private Boolean isFeatured = false;

    private Integer displayOrder;
}
