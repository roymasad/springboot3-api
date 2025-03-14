package com.roytemplates.springboot3_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Configuration class responsible for setting up password encoder in the application.
 * Used to get around circular dependency issue.
 */
@Configuration
public class PassTools {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
