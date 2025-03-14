package com.roytemplates.springboot3_api.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.util.Date;

@Data
@Document(collection = "passwordResetTokens")
public class PasswordResetToken {

    @Id
    private String id;
    
    private String token;
    
    private String userId;
    
    private Date expiryDate;
    
    public PasswordResetToken(String token, String userId, Date expiryDate) {
        this.token = token;
        this.userId = userId;
        this.expiryDate = expiryDate;
    }
    
    public PasswordResetToken() {
    }
}