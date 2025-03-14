package com.roytemplates.springboot3_api.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Represents an email verification token entity in the system.
 * This class is mapped to the "email_verification_tokens" collection in MongoDB.
 */
@Data
@Document(collection = "email_verification_tokens")
public class EmailVerificationToken {
    
    @Id
    private String id;
    
    private String token;
    private String userId;
    private Date expiryDate;
    
    public EmailVerificationToken() {}
    
    public EmailVerificationToken(String token, String userId, Date expiryDate) {
        this.token = token;
        this.userId = userId;
        this.expiryDate = expiryDate;
    }
    
    public boolean isExpired() {
        return new Date().after(this.expiryDate);
    }
}