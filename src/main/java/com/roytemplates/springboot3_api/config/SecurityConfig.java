package com.roytemplates.springboot3_api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.roytemplates.springboot3_api.filter.JwtAuthenticationFilter;
import com.roytemplates.springboot3_api.filter.RateLimitFilter;


/**
 * Configuration class for Spring Security settings.
 * This class handles security configurations including JWT authentication, request authorization,
 * and session management.
 *
 * Key features:
 * - Disables CSRF protection
 * - Permits all requests to /auth/** endpoints
 * - Requires authentication for all other requests
 * - Implements stateless session management
 * - Configures JWT authentication filter
 *
 * @see JwtAuthenticationFilter
 * @see AuthenticationProvider
 * @see SecurityFilterChain
 */
@Configuration // Indicates this is a configuration class
@EnableWebSecurity // Enables Spring Security web security support
@EnableMethodSecurity(prePostEnabled = true)  // for more fine tuned access in the controlelrs
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter; // Custom JWT filter for authentication

    @Autowired
    private RateLimitFilter rateLimitFilter; // Rate limiting filter

    @Autowired
    private AuthenticationProvider authenticationProvider; // Provider for authentication logic

    @Autowired
    private OAuth2SuccessHandler oAuth2SuccessHandler; // Custom OAuth2 success handler

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) // Disable CSRF protection
                // Turn on OAuth2 Login so default auto endpoints (/oauth2/authorization/{registrationId}) work
                // ex http://localhost:8080/oauth2/authorization/google
                //.oauth2Login(Customizer.withDefaults())
                .oauth2Login(oauth2 -> oauth2
                    .successHandler(oAuth2SuccessHandler) // Use custom handler component
                )                
                .authorizeHttpRequests(auth-> auth
                         .requestMatchers("/actuator/**").permitAll()
                         .requestMatchers("/v1/files/public/**").permitAll()
                         .requestMatchers("/oauth2/**").permitAll() // Allow unrestricted access to OAuth2 endpoints
                         .requestMatchers("/login/oauth2/code/**").permitAll() // Allow unrestricted access to OAuth2 login callback
                         .requestMatchers("/v1/auth/**").permitAll() // Allow unrestricted access to auth endpoints
                         .requestMatchers("/v1/users/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "DEFAULT") // Require ADMIN or SUPER_ADMIN role
                         .requestMatchers("/v1/files/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "DEFAULT") // Require ADMIN or SUPER_ADMIN role
                         .requestMatchers("/v1/posts/**").hasAnyRole("ADMIN", "DEFAULT")
                         .requestMatchers("/v1/business/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "DEFAULT")
                         .anyRequest().authenticated()) // Require authentication for all other requests, set to permitAll for better error debugging
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Use stateless sessions
                .authenticationProvider(authenticationProvider) // Set the authentication provider
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // Add JWT filter before authentication
                .addFilterAfter(rateLimitFilter, JwtAuthenticationFilter.class); // Add rate limiting after JWT authentication
        return http.build();
    }
}