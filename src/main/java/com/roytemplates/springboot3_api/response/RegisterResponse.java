package com.roytemplates.springboot3_api.response;

import com.roytemplates.springboot3_api.model.User;

import lombok.Data;

@Data
public class RegisterResponse {
    private String message;
    private User user;
    private String token;

    public RegisterResponse(String message, User user, String token) {
        this.message = message;
        this.user = user;
        this.token = token;
    }
}