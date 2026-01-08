package com.hafizbahtiar.spring.features.portfolio.dto;

import com.hafizbahtiar.spring.features.portfolio.entity.DegreeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Request DTO for creating or updating an education.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EducationRequest {

    @NotBlank(message = "Institution name is required")
    @Size(max = 200, message = "Institution name must not exceed 200 characters")
    private String institution;

    @NotNull(message = "Degree type is required")
    private DegreeType degree;

    @NotBlank(message = "Field of study is required")
    @Size(max = 200, message = "Field of study must not exceed 200 characters")
    private String fieldOfStudy;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Start date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    @Size(max = 50, message = "Grade must not exceed 50 characters")
    private String grade;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    private Double latitude;

    private Double longitude;

    private Boolean isCurrent = false;

    private Integer displayOrder;
}
