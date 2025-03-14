package com.roytemplates.springboot3_api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.roytemplates.springboot3_api.model.FileMetadata;
import com.roytemplates.springboot3_api.model.User;
import com.roytemplates.springboot3_api.repository.UserRepository;
import com.roytemplates.springboot3_api.security.CustomUserPrincipal;
import com.roytemplates.springboot3_api.service.FileService;
import com.roytemplates.springboot3_api.service.JwtService;

import java.io.IOException;
import java.util.List;

/**
 * Controller for handling file operations
 */
@Slf4j
@RestController
@RequestMapping("/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    /**
     * Upload an image file (PNG, JPEG, WebP only)
     */
    @PostMapping("/upload/image")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN') or hasRole('DEFAULT')")
    public ResponseEntity<FileMetadata> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("Authorization") String authHeader) {
        try {
            // Remove "Bearer " and extract user info from the token
            String token = authHeader.replace("Bearer ", "").trim();
            // Use your jwtService (inject it if needed) to extract user email
            String email = jwtService.extractUsername(token);
            // Retrieve the user from your repository (inject UserRepository)
            User user = userRepository.findByEmail(email)
                            .orElseThrow(() -> new RuntimeException("User not found"));
            
            FileMetadata metadata = fileService.uploadImage(file, user.getBusinessID(), user.getId());
            return ResponseEntity.ok(metadata);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            log.error("Error uploading image", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get file by Name 
     * and verify that it belongs to the user's business
     */
    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> getFile(
            @PathVariable String fileName,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "").trim();
            String email = jwtService.extractUsername(token);
            User user = userRepository.findByEmail(email)
                            .orElseThrow(() -> new RuntimeException("User not found"));

            Resource resource = fileService.getFileByNameAndBusiness(fileName, user.getBusinessID());
            // Get file metadata for content type and original filename
            FileMetadata metadata = fileService.getFileMetadata(fileName, user.getBusinessID());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(metadata.getMimeType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION
                            // note needed, return recommended download name as original.
                            //"attachment; filename=\"" + metadata.getOriginalFilename() + "\""
                            )
                    .body(resource);
        } catch (IOException e) {
            log.error("Error retrieving file", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get file by Name 
     * public endpoint for select files
     */
    @GetMapping("/public/{fileName}")
    public ResponseEntity<Resource> getFilePublic(
            @PathVariable String fileName
            ) {
        try {

            Resource resource = fileService.getFileByName(fileName);
            // Get file metadata for content type and original filename
            FileMetadata metadata = fileService.getFileMetadataPublic(fileName);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(metadata.getMimeType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION
                            // note needed, return recommended download name as original.
                            //"attachment; filename=\"" + metadata.getOriginalFilename() + "\""
                            )
                    .body(resource);
        } catch (IOException e) {
            log.error("Error retrieving file", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get file metadata 
     */
    @GetMapping("/{fileName}/metadata")
    public ResponseEntity<FileMetadata> getFileMetadata(
            @PathVariable String fileName,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        try {
            User user = principal.getUser();

            FileMetadata metadata = fileService.getFileMetadata(fileName, user.getBusinessID());
            return ResponseEntity.ok(metadata);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

}