package com.roytemplates.springboot3_api.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Represents a request object for user login authentication.
 * This class encapsulates the necessary credentials (email and password) required for user login.
 * The fields are validated using Bean Validation annotations to ensure data integrity.
 *
 */
@Data
public class LoginRequest {
    // Email field for user authentication, must not be blank and follow valid email format
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    // Password field for user authentication, must not be blank
    @NotBlank(message = "Password is required")
    private String password;
}