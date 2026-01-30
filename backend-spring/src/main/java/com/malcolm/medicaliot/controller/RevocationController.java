package com.malcolm.medicaliot.controller;

import com.malcolm.medicaliot.service.LockdownService;
import com.malcolm.medicaliot.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/access")
@CrossOrigin(origins = "http://localhost:5173")
public class RevocationController {

    @Autowired
    private UserService userService;

    @Autowired
    private LockdownService lockdownService;

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
