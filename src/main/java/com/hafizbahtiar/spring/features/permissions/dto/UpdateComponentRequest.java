package com.hafizbahtiar.spring.features.permissions.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating a permission component.
 * All fields are optional for partial updates.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateComponentRequest {

    /**
     * Human-readable component name
     */
    @Size(max = 200, message = "Component name must not exceed 200 characters")
    private String componentName;

    /**
     * Component type (e.g., "BUTTON", "LINK", "MENU_ITEM", "TAB", "FORM", "MODAL")
     */
    @Size(max = 50, message = "Component type must not exceed 50 characters")
    @Pattern(regexp = "^(BUTTON|LINK|MENU_ITEM|TAB|FORM|MODAL|INPUT|SELECT|CHECKBOX|RADIO|TEXTAREA|TABLE|CARD|PANEL|DROPDOWN|TOOLTIP|POPOVER|DIALOG|DRAWER|ACCORDION|TABS|LIST|GRID|CHART|MAP|CALENDAR|TIMELINE|WIZARD|STEPPER|OTHER)$", message = "Component type must be one of: BUTTON, LINK, MENU_ITEM, TAB, FORM, MODAL, INPUT, SELECT, CHECKBOX, RADIO, TEXTAREA, TABLE, CARD, PANEL, DROPDOWN, TOOLTIP, POPOVER, DIALOG, DRAWER, ACCORDION, TABS, LIST, GRID, CHART, MAP, CALENDAR, TIMELINE, WIZARD, STEPPER, OTHER")
    private String componentType;

    /**
     * Component description
     */
    private String description;
}
