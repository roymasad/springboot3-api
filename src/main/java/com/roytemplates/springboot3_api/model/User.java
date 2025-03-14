package com.roytemplates.springboot3_api.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.sql.Date;
import java.time.Instant;
import java.util.List;

/**
 * Represents a user entity in the system.
 * This class is mapped to the "users" collection in MongoDB.
 *
 * The User class contains personal information such as:
 * - First and last name
 * - Email address
 * - Password (encrypted)
 * - User role
 * - List of associated children
 *
 * Validation constraints:
 * - First name: cannot be blank
 * - Last name: cannot be blank
 * - Email: cannot be blank and must be in valid email format
 * - Password: cannot be blank and must be at least 6 characters long
 */
 // @Data for automatic getters, setters, equals, hashCode, and toString methods
 // @Document specifies the MongoDB collection name
@Data
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @JsonIgnore
    private String password;
    private UserRole role;
    private List<String> children;
    private String businessID;
    private String phoneNumber;
    private ProfileStatus profileStatus;
    private Boolean emailVerified;
    private String profilePicture;
    private AuthProvider provider;
    private String nofitications;
    private Instant creationDateUtc;

    public User(String firstName, String lastName, String email, String password, UserRole role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = role;
        this.businessID = "";
        this.profileStatus = ProfileStatus.ACTIVE;
        this.emailVerified = false;
        this.provider = AuthProvider.EMAIL;
        this.profilePicture = "";
        this.nofitications = "true";
        this.creationDateUtc = Instant.now();

    }
    public User(){}

}