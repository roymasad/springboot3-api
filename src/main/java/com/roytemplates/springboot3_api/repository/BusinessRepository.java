package com.roytemplates.springboot3_api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.roytemplates.springboot3_api.model.Business;

// Interface that extends MongoRepository to handle Business entity operations
public interface BusinessRepository extends MongoRepository<Business, String> {

    Optional<Business> findByName(String name);

    Optional<Business> findByAdminID(String id);

    List<Business> findByDeletedFalse();
    
}
