package com.malcolm.medicaliot.controller;

import com.malcolm.medicaliot.model.User;
import com.malcolm.medicaliot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (lockdownService.isLockdown()) {
            return ResponseEntity.status(403).body("SYSTEM_LOCKDOWN: Registration Temporarily Disabled.");
        }
        try {
            User savedUser = userService.registerUser(user);
            logService.log(savedUser.getUsername(), "REGISTER", "New user registered with role: " + savedUser.getRole(),
                    "SUCCESS");
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            logService.log(user.getUsername(), "REGISTER", "Registration failed: " + e.getMessage(), "FAILURE");
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {
        System.out.println("DEBUG: Login attempt for: " + user.getUsername());

        // DISABLE LOCKDOWN CHECK FOR DEBUGGING
        if (lockdownService.isLockdown()) {
            return ResponseEntity.status(403)
                    .body("SYSTEM_LOCKDOWN: Login Temporarily Disabled. Reason: " + lockdownService.getReason());
        }

        try {
            authenticationManager.authenticate(
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            user.getPassword()));
        } catch (org.springframework.security.authentication.BadCredentialsException e) {
            lockdownService.recordFailedLogin("127.0.0.1"); // In prod: request.getRemoteAddr()
            logService.log(user.getUsername(), "LOGIN", "Login failed: Invalid credentials", "FAILURE");
            throw new RuntimeException("Invalid username or password");
        }

        // Fetch full user for role
        User dbUser = userService.findByUsername(user.getUsername()).orElseThrow();
        logService.log(dbUser.getUsername(), "LOGIN", "User logged in successfully", "SUCCESS");

        String jwt = jwtService.generateToken(dbUser.getUsername(), dbUser.getRole());
        lockdownService.resetFailedLogin("127.0.0.1");

        return ResponseEntity.ok(new com.malcolm.medicaliot.dto.AuthResponse(jwt, dbUser));
    }
}
