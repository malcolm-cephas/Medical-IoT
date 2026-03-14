package com.malcolm.medicalauth.controller;

import com.malcolm.medicalauth.service.BiometricService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Biometric Auth endpoints in the Auth Server.
 */
@RestController
@RequestMapping("/api/auth/biometric")
@CrossOrigin(origins = "*")
public class BiometricController {

    private final BiometricService biometricService;

    @Autowired
    public BiometricController(BiometricService biometricService) {
        this.biometricService = biometricService;
    }

    @PostMapping("/enroll")
    public ResponseEntity<?> enroll(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        String descriptor = (String) request.get("descriptor");

        try {
            if (username == null || descriptor == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username and descriptor required"));
            }

            biometricService.enroll(username, descriptor);
            return ResponseEntity.ok(Map.of("message", "Biometric enrollment successful"));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Internal Error"));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody Map<String, Object> request) {
        try {
            String username = (String) request.get("username");
            @SuppressWarnings("unchecked")
            List<Double> descriptor = (List<Double>) request.get("descriptor");

            if (username == null || descriptor == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username and descriptor required"));
            }

            boolean isValid = biometricService.verify(username, descriptor);
            return ResponseEntity.ok(Map.of("valid", isValid));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal Error: " + e.getMessage()));
        }
    }

    @GetMapping("/status/{username}")
    public ResponseEntity<?> getStatus(@PathVariable String username) {
        try {
            boolean enrolled = biometricService.hasBiometrics(username);
            return ResponseEntity.ok(Map.of("enrolled", enrolled));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to check status: " + e.getMessage()));
        }
    }
}
