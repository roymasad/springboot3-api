package com.roytemplates.springboot3_api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.roytemplates.springboot3_api.service.CustomUserDetailsService;

/**
 * Configuration class responsible for setting up authentication provider in the application.
 * This class configures the DaoAuthenticationProvider with custom user details service 
 * and password encoder for handling authentication processes.
 *
 * @Configuration indicates that this class contains bean definitions for the application context
 */
@Configuration
public class AuthenticationProviderConfig {
    // Inject the custom user details service
    @Autowired
    private CustomUserDetailsService userDetailsService;

    // Inject the password encoder
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // Configure and return the authentication provider bean
    @Bean
    public AuthenticationProvider authenticationProvider() {
        // Create a new DAO authentication provider
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        // Set the custom user details service
        authProvider.setUserDetailsService(userDetailsService);
        // Set the password encoder for authentication
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

}