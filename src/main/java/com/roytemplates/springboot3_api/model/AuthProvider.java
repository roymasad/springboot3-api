package com.roytemplates.springboot3_api.model;

/**
 * Enum representing different authentication providers.
 * Used to distinguish between email-based and OAuth-based authentication.
 */
public enum AuthProvider {
    EMAIL,      // Regular email/password authentication
    GOOGLE,     // Google OAuth authentication
    APPLE       // Apple OAuth authentication
}
