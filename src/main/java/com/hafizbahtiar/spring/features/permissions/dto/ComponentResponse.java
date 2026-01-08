package com.hafizbahtiar.spring.features.permissions.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for permission component response (registry).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComponentResponse {

    private Long id;
    private String pageKey;
    private String componentKey;
    private String componentName;
    private String componentType;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}

