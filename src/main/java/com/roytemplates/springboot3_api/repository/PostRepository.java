package com.roytemplates.springboot3_api.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.roytemplates.springboot3_api.model.Post;

import java.util.List;

/**
 * Repository interface for managing {@link Post} entities.
 */
public interface PostRepository extends MongoRepository<Post, String> {

    /**
     * Finds posts by user ID.
     *
     * @param userId The ID of the user.
     * @return A list of posts created by the user.
     */
    List<Post> findByUserId(String userId);

    /**
     * Finds posts by business ID.
     *
     * @param businessId The ID of the business.
     * @return A list of posts associated with the business.
     */
    @Query(value = "{ 'businessId': ?0 }", sort = "{ 'creationDateUtc': -1 }")
    List<Post> findByBusinessId(String businessId, Pageable pageable);

    /**
     * Finds posts by user ID and business ID.
     *
     * @param userId The ID of the user.
     * @param businessId The ID of the business.
     * @return A list of posts created by the user and associated with the business.
     */
    @Query(value = "{ 'userId': ?0, 'businessId': ?1 }", sort = "{ 'creationDateUtc': -1 }")
    List<Post> findByUserIdAndBusinessId(String userId, String businessId, Pageable pageable);
}