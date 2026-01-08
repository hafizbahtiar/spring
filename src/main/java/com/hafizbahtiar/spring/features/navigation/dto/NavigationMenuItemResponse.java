package com.hafizbahtiar.spring.features.navigation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for navigation menu item.
 * Includes hierarchical structure with children.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NavigationMenuItemResponse {

    private Long id;
    private String title;
    private String url;
    private String iconName;
    private String groupLabel;
    private Integer displayOrder;
    private Long parentId;
    private Integer level;
    private String requiredRole;
    private String requiredPermissionModule;
    private String requiredPermissionPage;
    private Integer badge;
    private Boolean active;

    /**
     * Child menu items (for hierarchical structure).
     * Only populated when fetching complete hierarchy.
     */
    private List<NavigationMenuItemResponse> children;
}
