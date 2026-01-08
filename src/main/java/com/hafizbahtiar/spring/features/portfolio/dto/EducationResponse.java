package com.hafizbahtiar.spring.features.portfolio.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hafizbahtiar.spring.features.portfolio.entity.DegreeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for education details.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EducationResponse {

    private Long id;
    private Long userId;
    private String institution;
    private DegreeType degree;
    private String degreeDisplayName;
    private String fieldOfStudy;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String grade;
    private String address;
    private Double latitude;
    private Double longitude;
    private Boolean isCurrent;
    private Integer displayOrder;
    private Long durationInMonths; // Calculated duration
    private Boolean isHigherEducation; // Calculated flag

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
