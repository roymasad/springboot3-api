package com.roytemplates.springboot3_api.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.roytemplates.springboot3_api.dto.OAuth2UserInfoDTO;
import com.roytemplates.springboot3_api.model.AuthProvider;
import com.roytemplates.springboot3_api.model.User;
import com.roytemplates.springboot3_api.model.UserRole;
import com.roytemplates.springboot3_api.repository.UserRepository;
import com.roytemplates.springboot3_api.service.JwtService;

import org.springframework.beans.factory.annotation.Value;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * OAuth2 login flow for mobile apps:
 * 1. Mobile app opens in-app browser to: /oauth2/authorization/google
 * 2. Spring Security redirects to Google's consent screen
 * 3. User sees Google login page and enters credentials
 * 4. After successful Google login, Google generates auth code
 * 5. Google redirects to: /login/oauth2/code/google with auth code (callback URL in google console)
 * 6. Spring Security auto handles it:
 *    - Validates auth code
 *    - Exchanges it for access token
 *    - Gets user info from Google
 * 7. -> On Success this component is triggered
 * Here we (create user if need be) and generates our own JWT using user info
 * Then return deeplink with: com.roytemplates.frontendapp://oauth2?token=xyz
 * 9. Mobile OS/App triggers deep link listener in app with passed jwt token
 */

@Slf4j
@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Value("${app.deeplink}")
    private String appDeeplink;

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public OAuth2SuccessHandler(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication ) throws IOException, java.io.IOException    
    {        
        try {
        // Get the OAuth2 user info from the provider
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        
        // Get the provider (Google, Facebook, etc.)
        String provider = ((OAuth2AuthenticationToken) authentication)
            .getAuthorizedClientRegistrationId()
            .toUpperCase();

        // Extract user info and create JWT (same logic as your old OAuth2Controller)
        OAuth2UserInfoDTO userInfo = OAuth2UserInfoDTO.extract(oauth2User, provider);

        User user = userRepository.findByEmail(userInfo.getEmail())
            .orElseGet(() -> createNewUser(userInfo, AuthProvider.valueOf(provider)));

        // set the user's role to PENDING if it is null
        if (user.getRole() == null) {
            user.setRole(UserRole.PENDING);
            userRepository.save(user);
        }


        String token = jwtService.generateToken(user);

        // Redirect to the deep link with the token
        String targetUrl = UriComponentsBuilder.fromUriString(appDeeplink)
            .queryParam("token", token)
            .queryParam("provider", provider.toLowerCase())
            .build().toUriString();

        response.sendRedirect(targetUrl);
        
        } catch (Exception e) {
            log.error("Error in OAuth2SuccessHandler", e);
            response.sendRedirect(appDeeplink + "?error=auth_failed");
        }
    }

        /**
     * Creates a new user in the database from OAuth2 user information.
     *
     * @param userInfo DTO containing user information from OAuth2 provider
     * @param provider The OAuth2 provider (GOOGLE, APPLE, etc.)
     * @return The newly created user entity
     */
    private User createNewUser(OAuth2UserInfoDTO userInfo, AuthProvider provider) {
        User user = new User();
        user.setEmail(userInfo.getEmail());
        user.setFirstName(userInfo.getName());
        user.setProvider(provider);
        user.setEmailVerified(true);
        return userRepository.save(user);
    }
}
