package com.hafizbahtiar.spring.features.navigation.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for reordering menu items.
 * Contains a list of menu item IDs in the desired order.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReorderMenuItemsRequest {

    /**
     * List of menu item IDs in the desired order.
     * The order in the list determines the display order (1, 2, 3, ...).
     */
    @NotNull(message = "Ordered IDs list cannot be null")
    @NotEmpty(message = "Ordered IDs list cannot be empty")
    private List<Long> orderedIds;
}
