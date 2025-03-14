package com.roytemplates.springboot3_api.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import com.roytemplates.springboot3_api.model.AuthProvider;
import com.roytemplates.springboot3_api.model.EmailVerificationToken;
import com.roytemplates.springboot3_api.model.PasswordResetToken;
import com.roytemplates.springboot3_api.model.User;
import com.roytemplates.springboot3_api.model.UserRole;
import com.roytemplates.springboot3_api.repository.EmailVerificationTokenRepository;
import com.roytemplates.springboot3_api.repository.PasswordResetTokenRepository;
import com.roytemplates.springboot3_api.repository.UserRepository;
import com.roytemplates.springboot3_api.request.LoginRequest;
import com.roytemplates.springboot3_api.request.RegisterRequest;
import com.roytemplates.springboot3_api.response.LoginResponse;
import com.roytemplates.springboot3_api.response.RegisterResponse;
import com.roytemplates.springboot3_api.security.CustomUserPrincipal;
import com.roytemplates.springboot3_api.service.EmailService;
import com.roytemplates.springboot3_api.service.JwtService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Date;
import java.io.IOException;

/**
 * Controller class handling authentication-related endpoints.
 * This class manages user registration and login functionality through REST endpoints.
 *
 * Endpoints:
 * - POST /auth/register: Registers a new user
 * - POST /auth/login: Authenticates a user and returns a JWT token
 * - POST /auth/password-reset/request: Initiates a password reset process by sending an email with a reset link
 * - GET /auth/password-reset: Displays the password reset form
 * - POST /auth/password-reset: Processes the password reset submission
 *
 * The controller uses:
 * - BCryptPasswordEncoder for password hashing
 * - JwtService for JWT token generation
 * - UserRepository for database operations
 * - PasswordResetTokenRepository for managing password reset tokens
 * - EmailService for sending emails via SendGrid integration
 *
 * @RestController marks this class as a REST controller
 * @RequestMapping("/v1/auth") sets the base path for all endpoints in this controller
 */
@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    // Repository for user data operations
    @Autowired
    private UserRepository userRepository;

    // Encoder for password hashing
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // Service for JWT token operations
    @Autowired
    private JwtService jwtService;

    // Repository for password reset tokens
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    // Repository for email verification tokens
    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    // Notification service for sending emails
    @Autowired
    private EmailService emailService;

    // Base domain loaded from properties
    @Value("${server.address}")
    private String serverAddress;

    @Value("${server.port}")
    private String serverPort;

    // externally facing name
    @Value("${servername}")
    private String serverName;

    /**
     * Validates password requirements
     * @param password The password to validate
     * @return String error message if invalid, null if valid
     */
    static public String validatePassword(String password) {
        if (password.length() < 8 || password.length() > 50) {
            return "Password must be between 8 and 50 characters";
        }

        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain at least one uppercase letter";
        }

        if (!password.matches(".*[a-z].*")) {
            return "Password must contain at least one lowercase letter";
        }

        if (!password.matches(".*[!@#$%^&*()\\-_=+\\\\|\\[{\\]};:'\",<.>/?].*")) {
            return "Password must contain at least one special character";
        }

        if (!password.matches(".*\\d.*")) {
            return "Password must contain at least one number";
        }

        return null;
    }

    // Handles user registration
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new RegisterResponse("Email is already registered", null, null));
        }

        // Validate password
        String passwordValidationError = AuthController.validatePassword(request.getPassword());
        if (passwordValidationError != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new RegisterResponse(passwordValidationError, null, null));
        }

        // Create new user with encoded password
        User user = new User(request.getFirstName(), request.getLastName(), request.getEmail(), 
                passwordEncoder.encode(request.getPassword()), UserRole.PENDING);
        
        // add jwt token 
        String token = jwtService.generateToken(user);

        // Save user
        User savedUser = userRepository.save(user);

        // Generate and save email verification token
        String verificationToken = UUID.randomUUID().toString();
        EmailVerificationToken emailToken = new EmailVerificationToken(
            verificationToken,
            savedUser.getId(),
            new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000) // 24 hours expiry
        );
        emailVerificationTokenRepository.save(emailToken);

        // Generate verification link
        String verificationLink = "https://" + serverName + "/v1/auth/verify-email?token=" + verificationToken;

        // Send verification email
        try {
            emailService.sendEmail(
                savedUser.getEmail(),
                "Email Verification",
                "Welcome to Springboot3 API Template! Please verify your email by clicking the following link (expires in 24 hours):\n" + verificationLink
            );
        } catch (IOException e) {
            // Log the error but continue with registration
            System.err.println("Failed to send verification email: " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegisterResponse("User registered successfully. Please check your email for verification link.", savedUser, token));
    }

    // Handles user login
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody  LoginRequest loginRequest) {

        // Find user by email
        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());
        
        // Return error if user not found
        if (userOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new LoginResponse("Invalid email or password", null));
        }

        User user = userOptional.get();

        // Check if email is verified for non-OAuth users
        // PS: better let the user login then sandbox him, better UX experience.
        // if (user.getProvider() == AuthProvider.EMAIL && !user.getEmailVerified()) {
        //     return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        //         .body(new LoginResponse("Please verify your email before logging in. Check your email for the verification link or request a new one.", null));
        // }

        // Verify password and generate token if valid
        if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            String token = jwtService.generateToken(user);
            return ResponseEntity.ok(new LoginResponse("User logged in successfully", user, token));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new LoginResponse("Invalid email or password", null));
        }
    }
    
    /**
     * Endpoint to request a password reset. Accepts an email address, generates a reset token,
     * stores it with an expiry time, and sends a password reset email.
     */
    @PostMapping("/password-reset/request")
    public ResponseEntity<String> requestPasswordReset(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (!userOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUserId(userOptional.get().getId());
        resetToken.setExpiryDate(new Date(System.currentTimeMillis() + 2 * 1800000));
        passwordResetTokenRepository.save(resetToken);
        String resetLink = "https://" + serverName + "/v1/auth/password-reset?token=" + token;

        try {
            emailService.sendEmail(email, "Password Reset Request", "Click the link to reset your password (Expires in 30 minutes): \n " + resetLink);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send email.");
        }
        return ResponseEntity.ok("Password reset email sent.");
    }
    
    /**
     * Displays the password reset form.
     * by returning a saved html template reset form with themeleaf
     */
    @GetMapping("/password-reset")
    public ModelAndView showResetForm(@RequestParam("token") String token) {
        ModelAndView mav = new ModelAndView("reset-password");
        mav.addObject("token", token);
        return mav;
    }
    
    /**
     * Endpoint to process password reset submissions.
     */
    @PostMapping("/password-reset")
    public ModelAndView resetPassword(@RequestParam("token") String token,
                                    @RequestParam("password") String password,
                                    @RequestParam("confirmPassword") String confirmPassword) {
        ModelAndView mav = new ModelAndView("password-reset-result");
        
        if (!password.equals(confirmPassword)) {
            mav.addObject("success", false);
            mav.addObject("message", "Passwords do not match.");
            return mav;
        }

        String passwordValidationError = validatePassword(password);
        if (passwordValidationError != null) {
            mav.addObject("success", false);
            mav.addObject("message", passwordValidationError);
            return mav;
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token);
        if (resetToken == null) {
            mav.addObject("success", false);
            mav.addObject("message", "Invalid token.");
            return mav;
        }

        if (resetToken.getExpiryDate().before(new Date())) {
            mav.addObject("success", false);
            mav.addObject("message", "Token expired.");
            return mav;
        }

        Optional<User> userOpt = userRepository.findById(resetToken.getUserId());
        if (!userOpt.isPresent()) {
            mav.addObject("success", false);
            mav.addObject("message", "Error processing request.");
            return mav;
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        passwordResetTokenRepository.delete(resetToken);

        mav.addObject("success", true);
        mav.addObject("message", "Password has been reset successfully!");
        return mav;
    }

    /**
     * Endpoint to verify user's email address using the verification token.
     * @param token The verification token sent to the user's email
     * @return Response indicating success or failure of verification
     */
    @GetMapping("/verify-email")
    public ModelAndView verifyEmail(@RequestParam("token") String token) {
        ModelAndView mav = new ModelAndView("email-verification-result");
        
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token);
        if (verificationToken == null) {
            mav.addObject("success", false);
            mav.addObject("message", "Invalid verification token.");
            return mav;
        }

        if (verificationToken.isExpired()) {
            mav.addObject("success", false);
            mav.addObject("message", "Verification token has expired. Please request a new one.");
            return mav;
        }

        Optional<User> userOpt = userRepository.findById(verificationToken.getUserId());
        if (userOpt.isEmpty()) {
            mav.addObject("success", false);
            mav.addObject("message", "User not found.");
            return mav;
        }

        User user = userOpt.get();
        user.setEmailVerified(true);
        userRepository.save(user);
        emailVerificationTokenRepository.delete(verificationToken);

        mav.addObject("success", true);
        mav.addObject("message", "Email verified successfully! You can now log in to your account.");
        return mav;
    }

    /**
     * Endpoint to resend verification email.
     * @param payload Map containing the email address
     * @return Response indicating whether the email was sent
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerificationEmail(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        User user = userOptional.get();
        if (user.getEmailVerified()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is already verified.");
        }

        // Delete any existing verification tokens for this user
        EmailVerificationToken existingToken = emailVerificationTokenRepository.findByUserId(user.getId());
        if (existingToken != null) {
            emailVerificationTokenRepository.delete(existingToken);
        }

        // Generate new verification token
        String verificationToken = UUID.randomUUID().toString();
        EmailVerificationToken emailToken = new EmailVerificationToken(
            verificationToken,
            user.getId(),
            new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000) // 24 hours expiry
        );
        emailVerificationTokenRepository.save(emailToken);

        // Generate verification link
        String verificationLink = "https://" + serverName + "/v1/auth/verify-email?token=" + verificationToken;

        try {
            emailService.sendEmail(
                email,
                "Email Verification",
                "Please verify your email by clicking the following link (expires in 24 hours):\n" + verificationLink
            );
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send verification email.");
        }

        return ResponseEntity.ok("Verification email sent successfully.");
    }

    /**
     * Endpoint to get current user information from JWT token.
     * Expects Bearer token in Authorization header.
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token format");
        }

        // Extract token from header
        String token = authHeader.substring(7);
        
        try {
            // Extract email from token
            String userEmail = jwtService.extractUsername(token);
            
            // Find user by email
            Optional<User> userOpt = userRepository.findByEmail(userEmail);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            // if the caller reached this point he is already authenticated by jwt auto filters
            User user = userOpt.get();

            return ResponseEntity.ok(new LoginResponse("User logged in successfully", user, token));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
    }
}
