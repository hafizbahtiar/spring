package com.hafizbahtiar.spring.features.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for user preferences details.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPreferencesResponse {

    private Long id;
    private Long userId;
    private String theme;
    private String language;
    private String dateFormat;
    private String timeFormat;
    private String timezone;
    private String defaultDashboardView;
    private String itemsPerPage;
    private Boolean showWidgets;
    private String editorTheme;
    private Integer editorFontSize;
    private Double editorLineHeight;
    private Integer editorTabSize;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
