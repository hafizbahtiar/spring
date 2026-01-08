package com.hafizbahtiar.spring.features.permissions.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a permission component.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateComponentRequest {

    /**
     * Page key this component belongs to (e.g., "support.chat",
     * "finance.dashboard")
     * Format: "module.page"
     */
    @NotBlank(message = "Page key is required")
    @Size(max = 100, message = "Page key must not exceed 100 characters")
    private String pageKey;

    /**
     * Unique component key within the page (e.g., "edit_button", "delete_button")
     * Must be lowercase alphanumeric with underscores, max 100 characters
     */
    @NotBlank(message = "Component key is required")
    @Size(max = 100, message = "Component key must not exceed 100 characters")
    @Pattern(regexp = "^[a-z0-9_]+$", message = "Component key must be lowercase alphanumeric with underscores only")
    private String componentKey;

    /**
     * Human-readable component name
     */
    @NotBlank(message = "Component name is required")
    @Size(max = 200, message = "Component name must not exceed 200 characters")
    private String componentName;

    /**
     * Component type (e.g., "BUTTON", "LINK", "MENU_ITEM", "TAB", "FORM", "MODAL")
     */
    @NotBlank(message = "Component type is required")
    @Size(max = 50, message = "Component type must not exceed 50 characters")
    @Pattern(regexp = "^(BUTTON|LINK|MENU_ITEM|TAB|FORM|MODAL|INPUT|SELECT|CHECKBOX|RADIO|TEXTAREA|TABLE|CARD|PANEL|DROPDOWN|TOOLTIP|POPOVER|DIALOG|DRAWER|ACCORDION|TABS|LIST|GRID|CHART|MAP|CALENDAR|TIMELINE|WIZARD|STEPPER|OTHER)$", message = "Component type must be one of: BUTTON, LINK, MENU_ITEM, TAB, FORM, MODAL, INPUT, SELECT, CHECKBOX, RADIO, TEXTAREA, TABLE, CARD, PANEL, DROPDOWN, TOOLTIP, POPOVER, DIALOG, DRAWER, ACCORDION, TABS, LIST, GRID, CHART, MAP, CALENDAR, TIMELINE, WIZARD, STEPPER, OTHER")
    private String componentType;

    /**
     * Component description
     */
    private String description;
}
