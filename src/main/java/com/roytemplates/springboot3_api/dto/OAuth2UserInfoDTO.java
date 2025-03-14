package com.roytemplates.springboot3_api.dto;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
// Essential Spring Security OAuth2 imports
import org.springframework.security.oauth2.core.user.OAuth2User;

// Lombok annotations to reduce boilerplate code
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;


// DTO representing user information obtained from OAuth2 provider
@Data  // Lombok annotation that generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor  // Generates a no-args constructor
@AllArgsConstructor // Generates a constructor with all arguments
public class OAuth2UserInfoDTO {
    private String email;
    private String name;
    private String pictureUrl;
    private String provider;
    
    // Extract user information from OAuth2 user
    public static OAuth2UserInfoDTO extract(OAuth2User oauth2User, String provider) {
        
        if (oauth2User == null) {
            throw new OAuth2AuthenticationException("OAuth2 user cannot be null");
        }
        
        OAuth2UserInfoDTO userInfo = new OAuth2UserInfoDTO();
        userInfo.setEmail(oauth2User.getAttribute("email"));
        userInfo.setName(oauth2User.getAttribute("name"));
        userInfo.setPictureUrl(oauth2User.getAttribute("picture"));
        userInfo.setProvider(provider);
        return userInfo;
    }
}
