package com.roytemplates.springboot3_api.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.roytemplates.springboot3_api.model.PasswordResetToken;

import java.util.Date;

public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, String> {
    
    PasswordResetToken findByToken(String token);
    
    // /void deleteByExpiryDateBefore(Date now);
}