package com.hafizbahtiar.spring.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Stable pagination response DTO that matches Spring Data Page structure.
 * This eliminates the warning about PageImpl serialization instability.
 * 
 * Structure matches Spring Data Page<T>:
 * {
 * "content": [...],
 * "totalElements": number,
 * "totalPages": number,
 * "number": number, // 0-indexed page number
 * "size": number,
 * "first": boolean,
 * "last": boolean,
 * "numberOfElements": number
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaginatedResponse<T> {

    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int number; // 0-indexed page number
    private int size;
    private boolean first;
    private boolean last;
    private int numberOfElements;

    /**
     * Convert Spring Data Page to PaginatedResponse DTO
     */
    public static <T> PaginatedResponse<T> fromPage(Page<T> page) {
        return PaginatedResponse.<T>builder()
                .content(page.getContent())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .number(page.getNumber())
                .size(page.getSize())
                .first(page.isFirst())
                .last(page.isLast())
                .numberOfElements(page.getNumberOfElements())
                .build();
    }
}
