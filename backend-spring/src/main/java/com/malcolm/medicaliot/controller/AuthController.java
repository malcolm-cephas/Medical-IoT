package com.malcolm.medicaliot.controller;

import com.malcolm.medicaliot.model.User;
import com.malcolm.medicaliot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Authentication and User Registration.
 * Handles login requests, issues JWT tokens, and processes new user sign-ups.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private com.malcolm.medicaliot.service.LockdownService lockdownService;

    @Autowired
    private com.malcolm.medicaliot.service.SystemLogService logService;

    @Autowired
    private org.springframework.security.authentication.AuthenticationManager authenticationManager;

    @Autowired
    private com.malcolm.medicaliot.security.JwtService jwtService;

    /**
     * Registers a new user in the system.
     * checks if the system is in lockdown before proceeding.
     *
     * @param user The user object containing registration details.
     * @return The registered user or an error message.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        // Block registration if system is in emergency lockdown
        if (lockdownService.isLockdown()) {
            return ResponseEntity.status(403).body("SYSTEM_LOCKDOWN: Registration Temporarily Disabled.");
        }
        try {
            User savedUser = userService.registerUser(user);
            // Log the successful registration event
            logService.log(savedUser.getUsername(), "REGISTER", "New user registered with role: " + savedUser.getRole(),
                    "SUCCESS");
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            logService.log(user.getUsername(), "REGISTER", "Registration failed: " + e.getMessage(), "FAILURE");
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Authenticates a user and returns a JWT token.
     * 
     * @param user The login request containing username and password.
     * @return AuthResponse containing the JWT token and user details.
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {
        System.out.println("DEBUG: Login attempt for: " + user.getUsername());

        // Check for system lockdown (disabled here for debugging if needed)
        // DISABLE LOCKDOWN CHECK FOR DEBUGGING
        if (lockdownService.isLockdown()) {
            return ResponseEntity.status(403)
                    .body("SYSTEM_LOCKDOWN: Login Temporarily Disabled. Reason: " + lockdownService.getReason());
        }

        try {
            // Authenticate the user using Spring Security's AuthenticationManager
            // This will try to verify username/password against the database (via
            // CustomUserDetailsService)
            authenticationManager.authenticate(
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            user.getPassword()));
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            // Handle invalid credentials
            lockdownService.recordFailedLogin("127.0.0.1"); // In prod: request.getRemoteAddr()
            logService.log(user.getUsername(), "LOGIN", "Login failed: Invalid credentials", "FAILURE");
            throw new RuntimeException("Invalid username or password");
        }

        // Fetch full user details from DB to retrieve the role and other info
        User dbUser = userService.findByUsername(user.getUsername()).orElseThrow();
        logService.log(dbUser.getUsername(), "LOGIN", "User logged in successfully", "SUCCESS");

        // Generate JWT token containing the username and role
        String jwt = jwtService.generateToken(dbUser.getUsername(), dbUser.getRole());
        lockdownService.resetFailedLogin("127.0.0.1");

        // Return token and user details to the client
        return ResponseEntity.ok(new com.malcolm.medicaliot.dto.AuthResponse(jwt, dbUser));
    }
}
