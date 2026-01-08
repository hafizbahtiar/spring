package com.hafizbahtiar.spring.features.permissions.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for registry export response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistryExportResponse {

    /**
     * Export format (JSON, CSV)
     */
    private String format;

    /**
     * Export data
     */
    private RegistryData data;

    /**
     * Export metadata
     */
    private ExportMetadata metadata;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegistryData {
        private List<ModuleResponse> modules;
        private List<PageResponse> pages;
        private List<ComponentResponse> components;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExportMetadata {
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime exportedAt;
        private Long exportedBy;
        private Integer moduleCount;
        private Integer pageCount;
        private Integer componentCount;
    }
}
