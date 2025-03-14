package com.roytemplates.springboot3_api.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.roytemplates.springboot3_api.model.Business;
import com.roytemplates.springboot3_api.repository.BusinessRepository;
import com.roytemplates.springboot3_api.security.CustomUserPrincipal;
import com.roytemplates.springboot3_api.service.FileService;
import com.roytemplates.springboot3_api.service.FileStorageService;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.HashMap;

/**
 * Controller for managing businesses.
 */
@Slf4j
@RestController
@RequestMapping("/v1/business")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessRepository businessRepository;
    private final FileService fileService;
    private final FileStorageService fileStorageService;

    /**
     * Creates a new business.
     *
     * @param business The business to create.
     * @param logo The optional logo image file.
     * @param wallpaper The optional wallpaper image file.
     * @return The created business.
     */
    @PostMapping(value = "/", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Business> createBusiness(
            @RequestPart(value = "name") String name,
            @RequestPart(value = "adminID", required = false) String adminID,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "website", required = false) String website,
            @RequestPart(value = "email", required = false) String email,
            @RequestPart(value = "instaLink", required = false) String instaLink,
            @RequestPart(value = "fbLink", required = false) String fbLink,
            @RequestPart(value = "twitterLink", required = false) String twitterLink,
            @RequestPart(value = "address", required = false) String address,
            @RequestPart(value = "contactInfo", required = false) String contactInfo,
            @RequestPart(value = "brandColorRGB", required = false) String brandColorRGB,
            @RequestPart(value = "logoImage", required = false) MultipartFile logo,
            @RequestPart(value = "wallpaperImage", required = false) MultipartFile wallpaper) {
    
        try {
            // Create new business object with the provided fields
            Business business = new Business();
            business.setName(name);
            business.setAdminID(adminID);
            business.setDescription(description);
            business.setWebsite(website);
            business.setEmail(email);
            business.setInstaLink(instaLink);
            business.setFbLink(fbLink);
            business.setTwitterLink(twitterLink);
            business.setAddress(address);
            business.setContactInfo(contactInfo);
            business.setBrandColorRGB(brandColorRGB);
            
            // Save the business first to get an ID
            Business savedBusiness = businessRepository.save(business);
            
            // Handle file uploads with the generated business ID
            if (logo != null && fileStorageService.validateImageFile(logo)) {
                String logoUrl = fileService.uploadImage(logo, savedBusiness.getId(), null).getStoredFilename();
                savedBusiness.setLogoImage(logoUrl);
            }
        
            if (wallpaper != null && fileStorageService.validateImageFile(wallpaper)) {
                String wallpaperUrl = fileService.uploadImage(wallpaper, savedBusiness.getId(), null).getStoredFilename();
                savedBusiness.setWallpaperImage(wallpaperUrl);
            }
        
            // Save again with the updated image URLs
            savedBusiness = businessRepository.save(savedBusiness);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedBusiness);
        } catch (IOException e) {
            log.error("Error uploading images", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    /**
     * Updates an existing business.
     *
     * @param id The ID of the business to update.
     * @param business The business details to update.
     * @param logo The optional logo image file.
     * @param wallpaper The optional wallpaper image file.
     * @param principal The authenticated user.
     * @return The updated business.
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Business> updateBusiness(
            @PathVariable("id") String id,
            @RequestPart(value = "name", required = false) String name,
            @RequestPart(value = "adminId", required = false) String adminId,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "website", required = false) String website,
            @RequestPart(value = "email", required = false) String email,
            @RequestPart(value = "instaLink", required = false) String instaLink,
            @RequestPart(value = "fbLink", required = false) String fbLink,
            @RequestPart(value = "twitterLink", required = false) String twitterLink,
            @RequestPart(value = "address", required = false) String address,
            @RequestPart(value = "contactInfo", required = false) String contactInfo,
            @RequestPart(value = "brandColorRGB", required = false) String brandColorRGB,
            @RequestPart(value = "deleted", required = false) String deleted,
            @RequestPart(value = "logoImage", required = false) MultipartFile logoImage,
            @RequestPart(value = "wallpaperImage", required = false) MultipartFile wallpaperImage,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
    
        Optional<Business> optionalBusiness = businessRepository.findById(id);
        if (optionalBusiness.isPresent()) {
            Business existingBusiness = optionalBusiness.get();
    
            // Check if user has permission to update this business
            if (!principal.getUser().getRole().toString().equals("SUPER_ADMIN") && 
                !existingBusiness.getId().equals(principal.getUser().getBusinessID())
                ) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
    
            try {
                // Update fields only if they are provided
                if (name != null) existingBusiness.setName(name);
                if (adminId != null) existingBusiness.setAdminID(adminId);
                if (description != null) existingBusiness.setDescription(description);
                if (website != null) existingBusiness.setWebsite(website);
                if (email != null) existingBusiness.setEmail(email);
                if (instaLink != null) existingBusiness.setInstaLink(instaLink);
                if (fbLink != null) existingBusiness.setFbLink(fbLink);
                if (twitterLink != null) existingBusiness.setTwitterLink(twitterLink);
                if (address != null) existingBusiness.setAddress(address);
                if (contactInfo != null) existingBusiness.setContactInfo(contactInfo);
                if (brandColorRGB != null) existingBusiness.setBrandColorRGB(brandColorRGB);
                
                // Only allow SUPER_ADMIN to change deleted status
                if (deleted != null && principal.getUser().getRole().toString().equals("SUPER_ADMIN")) {
                    
                    if (deleted.equals("true")) {
                        existingBusiness.setDeleted(true);
                    } else if (deleted.equals("false")) {
                        existingBusiness.setDeleted(false);
                    }
                }
    
                // Handle file uploads
                if (logoImage != null && fileStorageService.validateImageFile(logoImage)) {
                    String logoUrl = fileService.uploadImage(logoImage, existingBusiness.getId(), 
                        principal.getUser().getId()).getStoredFilename();
                    existingBusiness.setLogoImage(logoUrl);
                }
            
                if (wallpaperImage != null && fileStorageService.validateImageFile(wallpaperImage)) {
                    String wallpaperUrl = fileService.uploadImage(wallpaperImage, existingBusiness.getId(), 
                        principal.getUser().getId()).getStoredFilename();
                    existingBusiness.setWallpaperImage(wallpaperUrl);
                }
            
                Business updatedBusiness = businessRepository.save(existingBusiness);
                return ResponseEntity.ok(updatedBusiness);
            } catch (IOException e) {
                log.error("Error uploading images", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    /**
     * Lists all businesses.
     *
     * @return A list of businesses.
     */
    @GetMapping("/")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<Business>> listBusinesses() {
        // Modified to only return non-deleted businesses
        List<Business> businesses = businessRepository.findByDeletedFalse();
        return ResponseEntity.ok(businesses);
    }

    /**
     * Soft deletes a business.
     *
     * @param id The ID of the business to delete.
     * @return A response indicating success or failure.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteBusiness(@PathVariable("id") String id) {
        Optional<Business> optionalBusiness = businessRepository.findById(id);
        if (optionalBusiness.isPresent()) {
            Business business = optionalBusiness.get();
            business.setDeleted(true);
            businessRepository.save(business);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Retrieves business information excluding sensitive fields.
     *
     * @param id The ID of the business to retrieve
     * @param principal The authenticated user
     * @return The business information without sensitive fields
     */
    @GetMapping("/{id}/info")
    @PreAuthorize("hasAnyRole('DEFAULT', 'ADMIN')")
    public ResponseEntity<?> getBusinessInfo(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CustomUserPrincipal principal) {
        
        // Check if user's businessID matches the requested business
        if (!principal.getUser().getBusinessID().equals(id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }

        Optional<Business> optionalBusiness = businessRepository.findById(id);
        if (optionalBusiness.isPresent()) {
            Business business = optionalBusiness.get();
            
            // Create a DTO with only the required fields
            var businessInfo = new HashMap<String, Object>();
            businessInfo.put("id", business.getId());
            businessInfo.put("name", business.getName());
            businessInfo.put("logoImage", business.getLogoImage());
            businessInfo.put("description", business.getDescription());
            businessInfo.put("wallpaperImage", business.getWallpaperImage());
            businessInfo.put("website", business.getWebsite());
            businessInfo.put("email", business.getEmail());
            businessInfo.put("instaLink", business.getInstaLink());
            businessInfo.put("fbLink", business.getFbLink());
            businessInfo.put("twitterLink", business.getTwitterLink());
            businessInfo.put("address", business.getAddress());
            businessInfo.put("contactInfo", business.getContactInfo());
            businessInfo.put("brandColorRGB", business.getBrandColorRGB());
            
            return ResponseEntity.ok(businessInfo);
        }
        
        return ResponseEntity.notFound().build();
    }
}