package com.roytemplates.springboot3_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

/**
 * Configuration properties for rate limiting.
 * Defines the rate limits for different types of users.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {
    // Requests per minute for unauthenticated users
    private int unauthenticatedLimit = 30;
    
    // Requests per minute for authenticated users
    private int authenticatedLimit = 60;
    
    // Requests per minute for admin users
    private int adminLimit = 100;
    
    // Time window in minutes
    private int timeWindow = 1;
}