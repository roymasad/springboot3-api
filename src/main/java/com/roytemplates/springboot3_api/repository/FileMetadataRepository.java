package com.roytemplates.springboot3_api.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.roytemplates.springboot3_api.model.FileMetadata;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing file metadata in MongoDB
 */
@Repository
public interface FileMetadataRepository extends MongoRepository<FileMetadata, String> {
    
    /**
     * Find file by its stored filename
     */
    Optional<FileMetadata> findByStoredFilename(String storedFilename);
    
    /**
     * Find file by its hash
     */
    Optional<FileMetadata> findByFileHash(String fileHash);
    
    /**
     * Find all files for a specific business
     */
    List<FileMetadata> findByBusinessId(String businessId);
    
    /**
     * Find all files uploaded by a specific user
     */
    List<FileMetadata> findByUploadedBy(String userId);
    
    /**
     * Find all active files for a specific business
     */
    List<FileMetadata> findByBusinessIdAndStatus(String businessId, FileMetadata.FileStatus status);
    
    /**
     * Check if a file hash already exists
     */
    boolean existsByFileHash(String fileHash);
}