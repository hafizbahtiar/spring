package com.hafizbahtiar.spring.features.user.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating user profile.
 * All fields are optional to allow partial updates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    /**
     * User's first name
     */
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    /**
     * User's last name
     */
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    /**
     * User's bio/description
     */
    @Size(max = 1000, message = "Bio must not exceed 1000 characters")
    private String bio;

    /**
     * User's location
     */
    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    /**
     * User's website URL
     */
    @Size(max = 500, message = "Website URL must not exceed 500 characters")
    private String website;
}

