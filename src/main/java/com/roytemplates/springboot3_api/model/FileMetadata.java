package com.roytemplates.springboot3_api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Entity representing metadata for stored files
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "files")
public class FileMetadata {
    
    @Id
    private String id;

    @Indexed
    private String originalFilename;
    
    @Indexed(unique = true)
    private String storedFilename;
    
    @Indexed
    private String fileHash;
    
    private String mimeType;
    
    private Long fileSize;
    
    @Indexed
    private String uploadedBy; // User ID
    
    @Indexed
    private String businessId;
    
    private LocalDateTime uploadDate;
    
    @Indexed
    private FileType fileType;
    
    private FileStatus status;

    private boolean publicAccess;

    /**
     * Enum representing the type of file
     */
    public enum FileType {
        GENERIC,
        IMAGE
    }

    /**
     * Enum representing the status of the file
     */
    public enum FileStatus {
        ACTIVE,
        DELETED
    }
}