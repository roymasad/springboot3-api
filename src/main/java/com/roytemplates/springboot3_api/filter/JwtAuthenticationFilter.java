package com.roytemplates.springboot3_api.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.roytemplates.springboot3_api.model.Business;
import com.roytemplates.springboot3_api.model.ProfileStatus;
import com.roytemplates.springboot3_api.model.User;
import com.roytemplates.springboot3_api.model.UserRole;
import com.roytemplates.springboot3_api.repository.BusinessRepository;
import com.roytemplates.springboot3_api.repository.UserRepository;
import com.roytemplates.springboot3_api.service.JwtService;

/**
 * Provides a filter that intercepts each incoming HTTP request and validates a JWT token 
 * present in the "Authorization" header. If the token is valid, it sets the current user 
 * authentication in the Spring Security context, enabling unlocked access to protected 
 * resources and securing your application from unauthorized requests.
 *
 * <p>This class extends {@code OncePerRequestFilter}, ensuring that it is executed exactly 
 * once per request within a single request thread. It depends on a {@code JwtService} 
 * for token validation/extraction and a {@code UserDetailsService} to load user information.
 */

/**
 * Service responsible for JWT token operations such as token extraction, validation, 
 * and user retrieval.
 */

/**
 * Loads user-specific data, typically from a persistence mechanism, by their username 
 * for authentication and verification purposes.
 */

/**
 * Filters the incoming request, checks for a valid JWT token, and sets up user authentication 
 * if the token is successfully validated. Otherwise, the request proceeds without an 
 * authenticated context.
 *
 * @param request     the incoming {@code HttpServletRequest}
 * @param response    the outgoing {@code HttpServletResponse}
 * @param filterChain the {@code FilterChain} to pass the request to the next filter
 * @throws ServletException if the filtering process cannot proceed
 * @throws IOException      if an I/O error occurs during filtering
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService customUserDetailsService; // Ensure it's your CustomUserDetailsService
    
    @Autowired
    private BusinessRepository businessRepository;

    @Autowired
    private UserRepository userRepository;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
         
        //System.out.println(request.getRequestURI());

        //System.out.println(request.getAuthType());

        //System.out.println(request.getMethod());

        //System.out.println(request.getContentType());
        
        //Skip token validation for public authentication endpoints
        if (request.getRequestURI().startsWith("/v1/auth")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        String jwt = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7).trim();
            username = jwtService.extractUsername(jwt);
        }

        // If username is found and we're not authenticated yet, load user details and set authentication object.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
            if (jwtService.validateToken(jwt, userDetails)) {
                // Retrieve User and Business entities
                Optional<User> userOptional = userRepository.findByEmail(username);
                if (userOptional.isEmpty()) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
                    return;
                }
                User user = userOptional.get();
                Business business = businessRepository.findById(user.getBusinessID()).orElse(null);
        
                //Check user profile status and business deleted status
                if ( !user.getRole().equals(UserRole.SUPER_ADMIN) &&
                     (user.getProfileStatus() != ProfileStatus.ACTIVE || business.isDeleted())
                   ) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
                    return;
                }
        
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}