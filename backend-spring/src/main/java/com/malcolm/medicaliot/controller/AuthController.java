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

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (lockdownService.isLockdown()) {
            return ResponseEntity.status(403).body("SYSTEM_LOCKDOWN: Registration Temporarily Disabled.");
        }
        try {
            User savedUser = userService.registerUser(user);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {
        if (lockdownService.isLockdown()) {
            return ResponseEntity.status(403)
                    .body("SYSTEM_LOCKDOWN: Login Temporarily Disabled. Reason: " + lockdownService.getReason());
        }

        // --- INTRUSION DETECTION LOGIC ---
        // Verify mock credentials (admin/password)
        if ("admin".equals(user.getUsername()) && "password".equals(user.getPassword())) {
            lockdownService.resetFailedLogin("127.0.0.1"); // In prod: request.getRemoteAddr()
            return ResponseEntity.ok("Login successful (Mock)");
        }

        // If wrong
        lockdownService.recordFailedLogin("127.0.0.1");
        return ResponseEntity.status(401).body("Invalid Credentials. Failed attempts logged.");
    }
}
