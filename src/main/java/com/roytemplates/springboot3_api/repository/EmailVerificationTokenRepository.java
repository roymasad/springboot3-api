package com.roytemplates.springboot3_api.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.roytemplates.springboot3_api.model.EmailVerificationToken;

/**
 * Repository interface for EmailVerificationToken entities.
 * Provides CRUD operations and custom queries for email verification tokens.
 */
public interface EmailVerificationTokenRepository extends MongoRepository<EmailVerificationToken, String> {
    
    /**
     * Find a verification token by its token string.
     * @param token The token string to search for
     * @return The EmailVerificationToken if found, null otherwise
     */
    EmailVerificationToken findByToken(String token);
    
    /**
     * Find all verification tokens for a specific user.
     * @param userId The ID of the user
     * @return The EmailVerificationToken if found, null otherwise
     */
    EmailVerificationToken findByUserId(String userId);
    
    /**
     * Delete all verification tokens for a specific user.
     * @param userId The ID of the user
     */
    void deleteByUserId(String userId);
}