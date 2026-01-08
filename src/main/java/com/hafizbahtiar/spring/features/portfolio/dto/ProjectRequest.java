package com.hafizbahtiar.spring.features.portfolio.dto;

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

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;

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
