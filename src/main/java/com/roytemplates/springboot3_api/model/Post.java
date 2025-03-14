package com.roytemplates.springboot3_api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Size;
import java.time.Instant;

/**
 * Represents a post entity in the system.
 * This class is mapped to the "posts" collection in MongoDB.
 *
 * The Post class contains information such as:
 * - Title
 * - Description
 * - Location
 * - Creation Date (UTC)
 * - User ID
 * - Number of Likes
 * - Business ID
 * - Image URL
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "posts")
public class Post {

    @Id
    private String id;

    @Indexed
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @Size(max = 255, message = "Location cannot exceed 255 characters")
    private String location;

    private Instant creationDateUtc;

    @Indexed
    private String userId;

    private Integer likes = 0;

    @Indexed
    private String businessId;

    private String imageUrl;
}