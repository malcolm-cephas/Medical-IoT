package com.malcolm.medicaliot.controller;

import com.malcolm.medicaliot.service.FaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Controller for handling face-based biometric authentication for Doctors.
 */
@RestController
@RequestMapping("/api/face")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FaceController {

    private final FaceService faceService;

    /**
     * Doctors use this to register their face (initial setup).
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerFace(
            @RequestParam("username") String username,
            @RequestParam("file") MultipartFile file) {
        boolean success = faceService.registerFace(username, file);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Face registered successfully"));
        }
        return ResponseEntity.badRequest().body(Map.of("error", "Face registration failed. Is there a face in the image?"));
    }

    /**
     * Start a 5-minute authorized session by verifying a face.
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyFace(
            @RequestParam("username") String username,
            @RequestParam("file") MultipartFile file) {
        boolean match = faceService.verifyFace(username, file);
        if (match) {
            return ResponseEntity.ok(Map.of(
                "message", "Face verified. Prescription creation session active for 5 minutes.",
                "verified", true
            ));
        }
        return ResponseEntity.status(401).body(Map.of("error", "Face match failed", "verified", false));
    }

    /**
     * Helper to check current session status.
     */
    @GetMapping("/session-status/{username}")
    public ResponseEntity<?> checkStatus(@PathVariable String username) {
        boolean active = faceService.isSessionValid(username);
        return ResponseEntity.ok(Map.of("username", username, "session_active", active));
    }

    /**
     * Check if a user is enrolled.
     */
    @GetMapping("/enrollment-status/{username}")
    public ResponseEntity<?> checkEnrollment(@PathVariable String username) {
        boolean enrolled = faceService.isEnrolled(username);
        return ResponseEntity.ok(Map.of("username", username, "enrolled", enrolled));
    }
}
