package com.roytemplates.springboot3_api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

import com.roytemplates.springboot3_api.dto.PostDTO;
import com.roytemplates.springboot3_api.model.FileMetadata;
import com.roytemplates.springboot3_api.model.Like;
import com.roytemplates.springboot3_api.model.Post;
import com.roytemplates.springboot3_api.model.User;
import com.roytemplates.springboot3_api.repository.LikeRepository;
import com.roytemplates.springboot3_api.repository.PostRepository;
import com.roytemplates.springboot3_api.repository.UserRepository;
import com.roytemplates.springboot3_api.request.PostRequest;
import com.roytemplates.springboot3_api.security.CustomUserPrincipal;
import com.roytemplates.springboot3_api.service.FileService;
import com.roytemplates.springboot3_api.service.FileStorageService;
import com.roytemplates.springboot3_api.service.JwtService;

import jakarta.validation.Valid;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Controller for managing posts.
 */
@Slf4j
@RestController
@RequestMapping("/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostRepository postRepository;
    private final FileService fileService;
    private final FileStorageService fileStorageService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;

     /**
     * Creates a new post.
     *
     * @param request     The post request.
     * @param authHeader  The authorization header.
     * @return The created post.
     */
    @PostMapping("/")
    @PreAuthorize("hasRole('DEFAULT')")
    public ResponseEntity<Post> createPost(
            @Valid @ModelAttribute PostRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        try {
            User user = principal.getUser();

            // Validate file type and size
            if (!fileStorageService.validateImageFile(request.getFile())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            // Compress and store the image
            FileMetadata metadata = fileService.uploadImage(request.getFile(), user.getBusinessID(), user.getId());
            String imageUrl = metadata.getStoredFilename();

            // Create the post
            Post post = Post.builder()
                    .title(request.getTitle())
                    .description(request.getDescription())
                    .location(request.getLocation())
                    .creationDateUtc(Instant.now())
                    .userId(user.getId())
                    .businessId(user.getBusinessID())
                    .imageUrl(imageUrl)
                    .build();

            // Save the post to the database
            Post savedPost = postRepository.save(post);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedPost);
        } catch (IOException e) {
            log.error("Error creating post", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (AccessDeniedException e) {
            log.error("Access denied", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (NoSuchElementException e) {
            log.error("User not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (RuntimeException e) {
            log.error("Error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Lists all posts for the current user or business.
     *
     * @param authHeader The authorization header.
     * @return A list of posts.
     */
    @GetMapping("/")
    public ResponseEntity<List<PostDTO>> listPosts(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        try {
            // shift zero based page index to start with 1
            if (page > 0) page--; 
            if (size > 100) size = 100; // limit page size to 100
            
            User user = principal.getUser();
            Pageable pageable = PageRequest.of(page, size);
    
            // Get paginated posts by business ID
            List<Post> postPage = postRepository.findByBusinessId(user.getBusinessID(), pageable);
    
            // Convert to PostDTO and check if liked
            List<PostDTO> postDTOs = postPage.stream().map(post -> {
                boolean isLiked = likeRepository.findByUserIdAndPostId(user.getId(), post.getId()).isPresent();
                return PostDTO.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .description(post.getDescription())
                        .location(post.getLocation())
                        .creationDateUtc(post.getCreationDateUtc())
                        .userId(post.getUserId())
                        .likes(post.getLikes())
                        .businessId(post.getBusinessId())
                        .imageUrl(post.getImageUrl())
                        .isLiked(isLiked)
                        .build();
            }).collect(Collectors.toList());
    
            return ResponseEntity.ok(postDTOs);
        } catch (NoSuchElementException e) {
            log.error("User not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (AccessDeniedException e) {
            log.error("Access denied", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (RuntimeException e) {
            log.error("Error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Updates an existing post.
     *
     * @param id        The ID of the post to update.
     * @param request   The post request.
     * @param authHeader The authorization header.
     * @return The updated post.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Post> updatePost(
            @PathVariable("id") String id,
            @Valid @RequestBody PostRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        try {
            User user = principal.getUser();

            // Get the post from the database
            Optional<Post> optionalPost = postRepository.findById(id);

            if (optionalPost.isPresent()) {
                Post post = optionalPost.get();

                // Check if the user is authorized to update the post
                if (!post.getUserId().equals(user.getId()) || !post.getBusinessId().equals(user.getBusinessID())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                }

                // Update the post
                post.setTitle(request.getTitle());
                post.setDescription(request.getDescription());
                post.setLocation(request.getLocation());

                // Save the post to the database
                Post updatedPost = postRepository.save(post);

                return ResponseEntity.ok(updatedPost);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (NoSuchElementException e) {
            log.error("User not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (AccessDeniedException e) {
            log.error("Access denied", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (RuntimeException e) {
            log.error("Error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Deletes an existing post.
     *
     * @param id        The ID of the post to delete.
     * @param authHeader The authorization header.
     * @return A response indicating success or failure.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DEFAULT')")
    public ResponseEntity<Void> deletePost(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        try {
            User user = principal.getUser();

            // Get the post from the database
            Optional<Post> optionalPost = postRepository.findById(id);

            if (optionalPost.isPresent()) {
                Post post = optionalPost.get();

                // Check if the user is authorized to delete the post
                if (!post.getUserId().equals(user.getId()) || !post.getBusinessId().equals(user.getBusinessID())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                }

                // Delete the post from the database
                postRepository.delete(post);

                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (NoSuchElementException e) {
            log.error("User not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (AccessDeniedException e) {
            log.error("Access denied", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (RuntimeException e) {
            log.error("Error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

     /**
     * Toggles like/unlike for a post.
     *
     * @param postId     The ID of the post to like/unlike.
     * @param authHeader The authorization header.
     * @return A response indicating the like status.
     */
    @PostMapping("/{postId}/like")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEFAULT')")
    public ResponseEntity<Boolean> toggleLike(
        @PathVariable("postId") String postId,
        @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        try {
            User user = principal.getUser();

            // Retrieve the post from the repository
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new NoSuchElementException("Post not found"));

            // Check if the user is authorized to like the post
            if (!post.getBusinessId().equals(user.getBusinessID())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }

            // Find existing like record
            Optional<Like> existingLike = likeRepository.findByUserIdAndPostId(user.getId(), postId);

            boolean isLiked;
            if (existingLike.isPresent()) {
                Like like = existingLike.get();
                isLiked = !like.isLiked();
                like.setLiked(isLiked);
                likeRepository.save(like);
            } else {
                Like newLike = Like.builder()
                        .userId(user.getId())
                        .postId(postId)
                        .liked(true)
                        .build();
                likeRepository.save(newLike);
                isLiked = true;
            }

            // Update post likes count
            if (isLiked) {
                post.setLikes(post.getLikes() + 1);
            } else {
                post.setLikes(post.getLikes() - 1);
            }
            postRepository.save(post);

            return ResponseEntity.ok(isLiked);
        } catch (NoSuchElementException e) {
            log.error("Entity not found", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (AccessDeniedException e) {
            log.error("Access denied", e);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (RuntimeException e) {
            log.error("Error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}