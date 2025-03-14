package com.roytemplates.springboot3_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.roytemplates.springboot3_api.repository.UserRepository;
import com.roytemplates.springboot3_api.security.CustomUserPrincipal;

import java.util.Optional;

/**
 * Custom implementation of Spring Security's UserDetailsService.
 * This service is responsible for loading user-specific data during authentication.
 * It retrieves user information from the database and converts it into a Spring Security UserDetails object.
 *
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    // Inject UserRepository for database operations
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Try to find user by email (username)
        Optional<com.roytemplates.springboot3_api.model.User> userOptional = userRepository.findByEmail(username);
        
        // Throw exception if user not found
        if (userOptional.isEmpty()){
            throw new UsernameNotFoundException("User not found");
        }

        // Get user from Optional
        com.roytemplates.springboot3_api.model.User user = userOptional.get();

        return new CustomUserPrincipal(user);

        // Build and return Spring Security User with email, password and role (converting custom User to UserDetails is buggy)
        // return org.springframework.security.core.userdetails.User
        //     .withUsername(user.getEmail())
        //     .password(user.getPassword())
        //     .roles(user.getRole().toString())
        //     .build();
    }
}