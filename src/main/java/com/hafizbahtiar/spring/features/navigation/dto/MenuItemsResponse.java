package com.hafizbahtiar.spring.features.navigation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for menu items grouped by group label.
 * Can be used for both flat and hierarchical menu structures.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemsResponse {

    /**
     * Menu items grouped by group label.
     * Key: Group label (e.g., "Navigation", "Portfolio", "Admin")
     * Value: List of menu items in that group (hierarchical structure with
     * children)
     */
    private Map<String, List<NavigationMenuItemResponse>> items;

    /**
     * Alternative: Flat list of all menu items (if hierarchical structure is not
     * needed)
     */
    private List<NavigationMenuItemResponse> flatItems;
}
