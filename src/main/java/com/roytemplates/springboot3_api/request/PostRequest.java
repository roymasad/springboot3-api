package com.roytemplates.springboot3_api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for creating and updating posts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @Nullable
    private String description;

    @Nullable
    private String location;

    private MultipartFile file;
}