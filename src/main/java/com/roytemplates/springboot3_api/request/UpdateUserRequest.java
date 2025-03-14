package com.roytemplates.springboot3_api.request;

import org.checkerframework.checker.units.qual.N;
import org.springframework.web.multipart.MultipartFile;

import com.roytemplates.springboot3_api.model.ProfileStatus;
import com.roytemplates.springboot3_api.model.UserRole;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    
    @Nullable
    private String firstName;
    @Nullable
    private String lastName;
    @Nullable
    private String email;
    @Nullable
    private MultipartFile profilePicture;
    @Nullable
    private String nofitications;
    @Nullable
    private String phoneNumber;

    @Nullable
    private String password; // set by user and business admin
    @Nullable
    private String currentPassword; // for changing password verification

    @Nullable
    private String businessID; //  set by admin (can only set to their own businessID, unless it was empty)
    @Nullable
    private UserRole role; // set by admin (cannot set to SUPER_ADMIN)
    @Nullable
    private ProfileStatus profileStatus; // set by SUPER_ADMIN 


}