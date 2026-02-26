package com.malcolm.medicaliot.controller;

import com.malcolm.medicaliot.service.LockdownService;
import com.malcolm.medicaliot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for Access Revocation.
 * Handles the removal of specific attributes or permissions from users.
 * Key part of the dynamic access control system.
 */
@RestController
@RequestMapping("/api/access")
public class RevocationController {

    @Autowired
    private UserService userService;

    @Autowired
    private LockdownService lockdownService;

    /**
     * Revokes a specific attribute from a user.
     * This immediately affects their ability to decrypt CP-ABE data requiring that
     * attribute.
     * 
     * @param body Map containing 'username', 'attribute', and 'adminId'.
     * @return Success message or error if user/attribute not found.
     */
    @PostMapping("/revoke")
    public ResponseEntity<?> revokeAccess(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String attribute = body.get("attribute"); // e.g., "Department:Cardiology"
        String adminId = body.get("adminId");

        if (username == null || attribute == null) {
            return ResponseEntity.badRequest().body("Username and Attribute are required.");
        }

        boolean success = userService.revokeAttribute(username, attribute);

        if (success) {
            lockdownService.logEvent("ATTRIBUTE_REVOCATION", "HIGH",
                    "Admin " + adminId + " revoked '" + attribute + "' from user " + username,
                    "127.0.0.1");
            return ResponseEntity.ok("Attribute revoked successfully.");
        } else {
            return ResponseEntity.badRequest().body("User not found or attribute not present.");
        }
    }
}
