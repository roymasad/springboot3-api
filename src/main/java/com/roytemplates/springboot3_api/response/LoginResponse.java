package com.roytemplates.springboot3_api.response;

import com.roytemplates.springboot3_api.model.User;

import lombok.Data;

/**
 * Response object for login operations.
 * This class encapsulates the data returned after a login attempt,
 * including a message, user details, and authentication token.
 *
 */
@Data
public class LoginResponse {
    private String message;
    private User user;
    private String token;

    public LoginResponse(String message, User user, String token) {
        this.message = message;
        this.user = user;
        this.token = token;
    }

    public LoginResponse(String message, User user) {
        this.message = message;
        this.user = user;
    }
}