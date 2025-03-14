package com.roytemplates.springboot3_api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Data Transfer Object for Post with additional isLiked field.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDTO {

    private String id;
    private String title;
    private String description;
    private String location;
    private Instant creationDateUtc;
    private String userId;
    private Integer likes;
    private String businessId;
    private String imageUrl;
    private boolean isLiked;
}