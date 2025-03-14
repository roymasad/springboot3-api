package com.roytemplates.springboot3_api.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.roytemplates.springboot3_api.model.User;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service class for handling JSON Web Token (JWT) operations.
 * This service provides functionality for generating, validating, and extracting information from JWTs.
 * It uses a secret key and expiration time configured through application properties.
 *
 * The service supports:
 * - Token generation for users
 * - Token validation
 * - Claim extraction
 * - Username extraction
 * - Token expiration checking
 *
 * Configuration properties required:
 * - jwt.secret: The secret key used for signing tokens
 * - jwt.expiration: The token expiration time in milliseconds
 *
 */
@Service
public class JwtService {

    // Secret key for JWT signing, loaded from application properties
    @Value("${jwt.secret}")
    private String secretKey;

    // JWT expiration time in milliseconds, loaded from application properties
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // Extracts username (subject) from JWT token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extracts role from JWT token
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    // Generic method to extract any claim from token using a resolver function
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Generates token with default claims for a user
    public String generateToken(User user) {
        return generateToken(new HashMap<>(), user);
    }

    // Generates token with extra claims and user information
    public String generateToken(Map<String, Object> extraClaims, User user) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(user.getEmail())
                .claim("role", user.getRole())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Checks if token is expired by comparing with current date
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Extracts expiration date from token
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extracts all claims from token
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Retrieves the secret key as a Key object
    private Key getSignInKey() {
        // Decodes the secret key from Base64
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        // Creates a Key object from the decoded bytes
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Validates if token belongs to user and is not expired
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    
}
