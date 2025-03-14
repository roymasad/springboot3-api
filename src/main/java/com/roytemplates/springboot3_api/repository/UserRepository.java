package com.roytemplates.springboot3_api.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.roytemplates.springboot3_api.model.User;

import java.util.List;
import java.util.Optional;

// Interface that extends MongoRepository to handle User entity operations
// MongoRepository<User, String> specifies the entity type (User) and ID type (String)
public interface UserRepository extends MongoRepository<User, String> {
    // Method to find a user by their email address
    // Returns Optional to safely handle cases where user might not exist
    Optional<User> findByEmail(String email);

    // Method to check if a user with given email exists
    // Returns true if user exists, false otherwise
    Boolean existsByEmail(String email);

    // Method to find all users by their businessID
    List<User> findByBusinessID(String businessId);

}