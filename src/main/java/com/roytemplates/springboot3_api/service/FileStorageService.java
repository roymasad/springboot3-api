package com.roytemplates.springboot3_api.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.roytemplates.springboot3_api.model.FileMetadata;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;

/**
 * Service for handling physical file storage operations
 */
@Slf4j
@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    private final Tika tika;
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
        "image/jpeg",
        "image/png",
        "image/webp"
    );

    public FileStorageService(@Value("${UPLOAD_PATH}") String uploadPath) {
        this.fileStorageLocation = Paths.get(uploadPath).toAbsolutePath().normalize();
        this.tika = new Tika();
        
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    /**
     * Store a file and return its metadata
     */
    public FileMetadata storeFile(MultipartFile file, String businessId, String userId) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalFilename);
        String storedFilename = generateUniqueFilename(extension);
        String mimeType = detectMimeType(file);
        String fileHash = calculateFileHash(file);
        
        // Create file metadata
        FileMetadata metadata = FileMetadata.builder()
            .originalFilename(originalFilename)
            .storedFilename(storedFilename)
            .fileHash(fileHash)
            .mimeType(mimeType)
            .fileSize(file.getSize())
            .uploadedBy(userId)
            .businessId(businessId)
            .uploadDate(java.time.LocalDateTime.now())
            .fileType(isImageFile(mimeType) ? FileMetadata.FileType.IMAGE : FileMetadata.FileType.GENERIC)
            .status(FileMetadata.FileStatus.ACTIVE)
            .build();

        // Compress image if applicable
        byte[] fileBytes;
        if (isImageFile(mimeType)) {
            fileBytes = compressImage(file.getBytes(), extension);
            metadata.setFileSize((long) fileBytes.length);
        } else {
            fileBytes = file.getBytes();
        }

        // Store the file
        Path targetLocation = this.fileStorageLocation.resolve(storedFilename);
        Files.write(targetLocation, fileBytes);
        
        return metadata;
    }

    /**
     * Store a public file and return its metadata
     */
    public FileMetadata storeFilePublic(MultipartFile file, String businessId, String userId) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalFilename);
        String storedFilename = generateUniqueFilename(extension);
        String mimeType = detectMimeType(file);
        String fileHash = calculateFileHash(file);
        
        // Create file metadata
        FileMetadata metadata = FileMetadata.builder()
            .originalFilename(originalFilename)
            .storedFilename(storedFilename)
            .fileHash(fileHash)
            .mimeType(mimeType)
            .fileSize(file.getSize())
            .uploadedBy(userId)
            .businessId(businessId)
            .uploadDate(java.time.LocalDateTime.now())
            .fileType(isImageFile(mimeType) ? FileMetadata.FileType.IMAGE : FileMetadata.FileType.GENERIC)
            .status(FileMetadata.FileStatus.ACTIVE)
            .publicAccess(true)
            .build();

        // Compress image if applicable
        byte[] fileBytes;
        if (isImageFile(mimeType)) {
            fileBytes = compressImage(file.getBytes(), extension);
            metadata.setFileSize((long) fileBytes.length);
        } else {
            fileBytes = file.getBytes();
        }

        // Store the file
        Path targetLocation = this.fileStorageLocation.resolve(storedFilename);
        Files.write(targetLocation, fileBytes);
        
        return metadata;
    }


    /**
     * Load a file as a resource
     */
    public Path loadFileAsResource(String storedFilename) {
        try {
            Path filePath = this.fileStorageLocation.resolve(storedFilename).normalize();
            if (Files.exists(filePath)) {
                return filePath;
            } else {
                throw new RuntimeException("File not found: " + storedFilename);
            }
        } catch (Exception ex) {
            throw new RuntimeException("File not found: " + storedFilename, ex);
        }
    }

    /**
     * Delete a file
     */
    public void deleteFile(String storedFilename) {
        try {
            Path filePath = this.fileStorageLocation.resolve(storedFilename).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete file: " + storedFilename, ex);
        }
    }

    /**
     * Validate if file is an image
     */
    public boolean validateImageFile(MultipartFile file) throws IOException {
        String mimeType = detectMimeType(file);
        return isImageFile(mimeType);
    }

    /**
     * Generate a unique filename
     */
    private String generateUniqueFilename(String extension) {
        return UUID.randomUUID().toString() + (extension != null ? "." + extension : "");
    }

    /**
     * Calculate SHA-256 hash of file content
     */
    private String calculateFileHash(MultipartFile file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            try (InputStream is = file.getInputStream()) {
                while ((bytesRead = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }
            
            byte[] hashBytes = digest.digest();
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("Could not generate file hash", ex);
        }
    }

    /**
     * Detect MIME type of file
     */
    private String detectMimeType(MultipartFile file) throws IOException {
        return tika.detect(file.getInputStream());
    }

    /**
     * Check if file is an image based on MIME type
     */
    private boolean isImageFile(String mimeType) {
        return ALLOWED_IMAGE_TYPES.contains(mimeType.toLowerCase());
    }

    /**
     * Compress image with specified quality
     */
    private byte[] compressImage(byte[] imageData, String extension) throws IOException {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(imageData));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Handle WebP separately as it might use a different API
        if ("webp".equalsIgnoreCase(extension)) {
            ImageIO.write(originalImage, "webp", outputStream);
            return outputStream.toByteArray();
        }

        // For JPEG and PNG
        ImageWriter writer = ImageIO.getImageWritersByFormatName(extension).next();
        ImageWriteParam writeParam = writer.getDefaultWriteParam();

        if (writeParam.canWriteCompressed()) {
            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            writeParam.setCompressionQuality(0.8f); // 80% quality
        }

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(originalImage, null, null), writeParam);
            writer.dispose();
        }

        return outputStream.toByteArray();
    }
}