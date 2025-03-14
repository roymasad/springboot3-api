package com.roytemplates.springboot3_api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.roytemplates.springboot3_api.model.Business;
import com.roytemplates.springboot3_api.model.FileMetadata;
import com.roytemplates.springboot3_api.model.User;
import com.roytemplates.springboot3_api.model.UserRole;
import com.roytemplates.springboot3_api.repository.BusinessRepository;
import com.roytemplates.springboot3_api.repository.UserRepository;
import com.roytemplates.springboot3_api.request.UpdateUserRequest;
import com.roytemplates.springboot3_api.response.RegisterResponse;
import com.roytemplates.springboot3_api.service.EmailService;
import com.roytemplates.springboot3_api.service.FileService;
import com.roytemplates.springboot3_api.service.FileStorageService;
import com.roytemplates.springboot3_api.service.JwtService;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("v1/users")
@RequiredArgsConstructor
public class UserController {

    // Repository for user data operationss
    private final UserRepository userRepository;

    private final BusinessRepository businessRepository;

    // Service for JWT token operations
    private final JwtService jwtService;

    // Service for email sending
    private final EmailService emailService;

    private final FileStorageService fileStorageService;

    private final FileService fileService;

    private final BCryptPasswordEncoder passwordEncoder;

    //  Endpoint to retrieve registered users
    @GetMapping("/")
    public ResponseEntity<List<User>> getRegisteredUsers(@RequestHeader("Authorization") String authHeader) {
        // Extract token
        String token = authHeader.replace("Bearer ", "").trim();
        
        // Extract role and email from the token
        String role = jwtService.extractRole(token);
        String email = jwtService.extractUsername(token);
        
        if ("SUPER_ADMIN".equals(role)) {
            // Super admin gets all users
            List<User> users = userRepository.findAll();
            return ResponseEntity.ok(users);
        } else if ("ADMIN".equals(role)) {
            // Get the admin's information from the token
            Optional<User> adminUserOptional = userRepository.findByEmail(email);
            if (adminUserOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            User adminUser = adminUserOptional.get();
            // Admin gets users sharing the same businessId
            List<User> users = userRepository.findByBusinessID(adminUser.getBusinessID());
            return ResponseEntity.ok(users);
        }
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    // Endpoint to update a user's data
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable("id") String id,
            @ModelAttribute UpdateUserRequest updateRequest,
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("Content-Type") String contentTypeHeader
            ) {
        
        // For debugging for now
        String contenType = contentTypeHeader;
        // Extract and verify token information
        String token = authHeader.replace("Bearer ", "").trim();
        String email = jwtService.extractUsername(token);

        Optional<User> callingUserOpt = userRepository.findByEmail(email);
        if (callingUserOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User callingUser = callingUserOpt.get();

        // Find the user to update by id
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        User userToUpdate = userOpt.get();

        // allowed for all. the user is updating his own allowed presonal fields
        if (callingUser.getBusinessID().equals(userToUpdate.getBusinessID())) {

            // Update fields if provided
            if (updateRequest.getFirstName() != null) {
                userToUpdate.setFirstName(updateRequest.getFirstName());
            }
            if (updateRequest.getLastName() != null) {
                userToUpdate.setLastName(updateRequest.getLastName());
            }
            if (updateRequest.getNofitications() != null) {
                userToUpdate.setNofitications(updateRequest.getNofitications());
            }
            if (updateRequest.getPhoneNumber() != null) {
                userToUpdate.setPhoneNumber(updateRequest.getPhoneNumber());
            }

            // TODO: placeholder. the user must verify his new email first
            // if (updateRequest.getEmail() != null) {
            //     userToUpdate.setEmail(updateRequest.getEmail());
            // }

            if (updateRequest.getProfilePicture() != null) {
               
                try {
                // Validate file type and size
                if (!fileStorageService.validateImageFile(updateRequest.getProfilePicture())) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
                }

                // Upload a public image to the file storage service
                FileMetadata metadata = fileService.uploadImagePublic(updateRequest.getProfilePicture(), userToUpdate.getBusinessID(), userToUpdate.getId());
                String imageUrl = metadata.getStoredFilename();

                // Update the user's profile picture
                userToUpdate.setProfilePicture(imageUrl);

                }
                catch (Exception e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                }

            }


        }

        // ADMIN and SUPER_ADMIN can update the role and profileStatus
        if (callingUser.getRole() == UserRole.ADMIN || callingUser.getRole() == UserRole.SUPER_ADMIN) {
            if (updateRequest.getRole() != null) {
                userToUpdate.setRole(updateRequest.getRole());
            }
        }

        if (callingUser.getRole() == UserRole.SUPER_ADMIN) {
            if (updateRequest.getProfileStatus() != null) {
                userToUpdate.setProfileStatus(updateRequest.getProfileStatus());
            }
        }

        // if calling user is an admin but his businessid is not the same as the target user return with forbiden
        if (callingUser.getRole() == UserRole.ADMIN && !callingUser.getBusinessID().equals(userToUpdate.getBusinessID())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Update the businessID if the user is not already assigned to a business and caller is an admin
        if (updateRequest.getBusinessID() != null && !updateRequest.getBusinessID().isEmpty() &&
            (callingUser.getRole() == UserRole.ADMIN) &&
            (userToUpdate.getBusinessID() == null || userToUpdate.getBusinessID().isEmpty())) {
            
            userToUpdate.setBusinessID( updateRequest.getBusinessID());
        }
            
        // either the calling user or the business admin can change the user's password
        if (updateRequest.getPassword() != null) {

            // Validate current password
            if (updateRequest.getCurrentPassword() != null) {
                if (!passwordEncoder.matches(updateRequest.getCurrentPassword(), userToUpdate.getPassword())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }

            // Validate password
            String passwordValidationError = AuthController.validatePassword(updateRequest.getPassword());
            if (passwordValidationError != null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            if (callingUser.getRole() == UserRole.ADMIN || callingUser.getRole() == UserRole.SUPER_ADMIN) {
                // the admin/super can change the user's password
                userToUpdate.setPassword( passwordEncoder.encode(updateRequest.getPassword()) );
            }
            else if (callingUser.getId().equals(userToUpdate.getId())) {
                // the user can change his own password
                userToUpdate.setPassword( passwordEncoder.encode(updateRequest.getPassword()) );
            }
            else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        User updatedUser = userRepository.save(userToUpdate);
        return ResponseEntity.ok(updatedUser);
    }

    // Endpoint to invite a user to a business if the user is not already assigned to one
    // PS we can use here @AuthenticationPrincipal CustomUserPrincipal if we want, less code
    // since the user is already authenticated on this endpoing (no token extraction, db user checking)
    @GetMapping("/invite")
    public ResponseEntity<?> inviteUser(@RequestParam("email") String email,
                                        @RequestHeader("Authorization") String authHeader) {
        // Extract token and verify that the caller is an ADMIN.
        String token = authHeader.replace("Bearer ", "").trim();
        String adminEmail = jwtService.extractUsername(token);

        // Retrieve the calling admin's user record.
        Optional<User> adminOpt = userRepository.findByEmail(adminEmail);
        if (adminOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body("Admin user not found.");
        }
        User adminUser = adminOpt.get();

        // Decode the email parameter in case it has special characters like + that were encoded
        String decodedEmail = URLDecoder.decode(email, StandardCharsets.UTF_8);

        // Find the user to be invited by the provided email.
        Optional<User> userOpt = userRepository.findByEmail(decodedEmail);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("User with email " + decodedEmail + " not found.");
        }
        User userToInvite = userOpt.get();

        // If the user already belongs to a business, return an error.
        if (userToInvite.getBusinessID() != null && !userToInvite.getBusinessID().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body("User already assigned to a business.");
        }

        // find the business name of the businness the admin belongs to
        Optional<Business> businessOpt = businessRepository.findById(adminUser.getBusinessID());
        if (businessOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Business not found.");
        }
        Business business = businessOpt.get();

        // Set the user's businessID to the admin's businessID and save the update.
        userToInvite.setBusinessID(adminUser.getBusinessID());
        userRepository.save(userToInvite);

        // Send an invitation email to the user.
        String subject = "You have been invited to join Springboot3 API app : " + business.getName();
        String body = "You have been invited to join. Please log in with your email on the app.";
        try {
            emailService.sendEmail(decodedEmail, subject, body);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Failed to send invitation email.");
        }
        return ResponseEntity.ok("User invited successfully.");
    }
}