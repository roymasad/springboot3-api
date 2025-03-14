package com.roytemplates.springboot3_api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.roytemplates.springboot3_api.model.FileMetadata;
import com.roytemplates.springboot3_api.model.User;
import com.roytemplates.springboot3_api.repository.FileMetadataRepository;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Service for handling file operations and business logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileStorageService fileStorageService;
    private final FileMetadataRepository fileMetadataRepository;

    /**
     * Upload a generic file
     */
    public FileMetadata uploadFile(MultipartFile file, String businessId, String userId) throws IOException {
        // Store the file and get metadata
        FileMetadata metadata = fileStorageService.storeFile(file, businessId, userId);
        
        // Save metadata to database
        return fileMetadataRepository.save(metadata);
    }

    /**
     * Upload an image file with validation
     */
    public FileMetadata uploadImage(MultipartFile file, String businessId, String userId) throws IOException {
        // Validate that the file is an image
        if (!fileStorageService.validateImageFile(file)) {
            throw new IllegalArgumentException("File must be an image (PNG, JPEG, or WebP)");
        }
        
        // Store the file and get metadata
        FileMetadata metadata = fileStorageService.storeFile(file, businessId, userId);
        
        // Save metadata to database
        return fileMetadataRepository.save(metadata);
    }

        /**
     * Upload an image file with validation
     */
    public FileMetadata uploadImagePublic(MultipartFile file, String businessId, String userId) throws IOException {
        // Validate that the file is an image
        if (!fileStorageService.validateImageFile(file)) {
            throw new IllegalArgumentException("File must be an image (PNG, JPEG, or WebP)");
        }
        
        // Store the file and get metadata
        FileMetadata metadata = fileStorageService.storeFilePublic(file, businessId, userId);
        
        // Save metadata to database
        return fileMetadataRepository.save(metadata);
    }

    /**
     * Get file by filename and business with caching
     */
    @Cacheable(value = "files", key = "#fileName")
    public Resource getFileByNameAndBusiness(String fileName, String businessId) throws IOException {
        try {

            FileMetadata metadata = fileMetadataRepository.findByStoredFilename(fileName)
                .orElseThrow(() -> new RuntimeException("File not found"));
                
            // Validate business access
            if (!metadata.getBusinessId().equals(businessId)) {
                throw new AccessDeniedException("Access denied to file");
            }
            
            Path filePath = fileStorageService.loadFileAsResource(metadata.getStoredFilename());
            return new UrlResource(filePath.toUri());

        } catch (IOException e) {
            log.error("Error loading file", e);
            throw e;
        }
    }

    /**
     * Get public file by name with caching
     */
    @Cacheable(value = "files", key = "#fileName")
    public Resource getFileByName(String fileName) throws IOException {
        try {

            FileMetadata metadata = fileMetadataRepository.findByStoredFilename(fileName)
                .orElseThrow(() -> new RuntimeException("File not found"));
            
            if (metadata.isPublicAccess()) {

                Path filePath = fileStorageService.loadFileAsResource(metadata.getStoredFilename());
                return new UrlResource(filePath.toUri());

            } else {
                throw new AccessDeniedException("Access denied to file");
            }

        } catch (IOException e) {
            log.error("Error loading file", e);
            throw e;
        }
    }

    /**
     * Get file by ID with caching
     */
    @Cacheable(value = "files", key = "#fileId")
    public Resource getFileById(String fileId) throws IOException {
        try {

            FileMetadata metadata = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));
            
            Path filePath = fileStorageService.loadFileAsResource(metadata.getStoredFilename());
            return new UrlResource(filePath.toUri());

        } catch (IOException e) {
            log.error("Error loading file", e);
            throw e;
        }
    }

    /**
     * Get file metadata by ID
     */
    public FileMetadata getFileMetadata(String fileName, String businessId) {
        FileMetadata metadata = fileMetadataRepository.findByStoredFilename(fileName)
            .orElseThrow(() -> new RuntimeException("File not found"));
            
        // Validate business access
        if (!metadata.getBusinessId().equals(businessId)) {
            throw new AccessDeniedException("Access denied to file metadata");
        }
        
        return metadata;
    }

    /**
     * Get public file metadata by ID
     */
    public FileMetadata getFileMetadataPublic(String fileName) {

        FileMetadata metadata = fileMetadataRepository.findByStoredFilename(fileName)
            .orElseThrow(() -> new RuntimeException("File not found"));
        
        return metadata;
    }

    /**
     * Get all files for a business
     */
    public List<FileMetadata> getBusinessFiles(String businessId) {
        return fileMetadataRepository.findByBusinessIdAndStatus(businessId, FileMetadata.FileStatus.ACTIVE);
    }

    /**
     * Delete a file (soft delete)
     */
    public void deleteFile(String fileId, String businessId) {
        FileMetadata metadata = fileMetadataRepository.findById(fileId)
            .orElseThrow(() -> new RuntimeException("File not found"));
            
        // Validate business access
        if (!metadata.getBusinessId().equals(businessId)) {
            throw new AccessDeniedException("Access denied to delete file");
        }
        
        // Soft delete - update status
        metadata.setStatus(FileMetadata.FileStatus.DELETED);
        fileMetadataRepository.save(metadata);
    }
}