package com.roytemplates.springboot3_api.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.roytemplates.springboot3_api.model.Like;

import java.util.Optional;

/**
 * Repository interface for Like operations.
 */
public interface LikeRepository extends MongoRepository<Like, String> {

    Optional<Like> findByUserIdAndPostId(String userId, String postId);
}