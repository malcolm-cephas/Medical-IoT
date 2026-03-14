package com.malcolm.medicaliot.controller;

import com.malcolm.medicaliot.model.Prescription;
import com.malcolm.medicaliot.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing Prescriptions.
 * Exposes endpoints to create and retrieve prescription records.
 */
@RestController
@RequestMapping("/api/prescriptions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PrescriptionController {

    private final PrescriptionRepository prescriptionRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private com.malcolm.medicaliot.service.TwoFactorService twoFactorService;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String AUTH_SERVER_URL = "http://localhost:9000/api/auth/biometric/verify";

    /**
     * Endpoint to add a new prescription.
     * Includes support for 2FA via 6-digit code or Face-ID biometrics.
     */
    @PostMapping("/add")
    public ResponseEntity<?> addPrescription(
            @RequestHeader(value = "X-User-Id", required = false) String doctorUsername,
            @RequestHeader(value = "X-2FA-Code", required = false) String tfaCode,
            @RequestHeader(value = "X-Biometric-Data", required = false) String biometricDataJson,
            @RequestBody Prescription prescription) {
        
        if (prescription == null) {
            return ResponseEntity.badRequest().build();
        }

        // Logic for Doctors: Require 2FA if doctorId is present
        if (doctorUsername != null) {
            boolean isVerified = false;
            
            // 1. Check traditional 2FA (TOTP / Email Code)
            if (tfaCode != null && !tfaCode.isEmpty()) {
                if (twoFactorService.verifyCode(doctorUsername, tfaCode)) {
                    isVerified = true;
                }
            }
            
            // 2. Check Biometric 2FA (if code fails or is missing)
            if (!isVerified && biometricDataJson != null && !biometricDataJson.isEmpty()) {
                try {
                    // Convert biometric data string back to list of doubles
                    String clean = biometricDataJson.replace("[", "").replace("]", "");
                    String[] parts = clean.split(",");
                    List<Double> descriptor = new java.util.ArrayList<>();
                    for (String part : parts) descriptor.add(Double.parseDouble(part.trim()));
                    
                    // Delegate verification to the Dedicated Auth Server (Port 9000)
                    Map<String, Object> verifyRequest = new HashMap<>();
                    verifyRequest.put("username", doctorUsername);
                    verifyRequest.put("descriptor", descriptor);
                    
                    ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(AUTH_SERVER_URL, verifyRequest, (Class<Map<String, Object>>) (Class<?>) Map.class);
                    Map<String, Object> responseBody = response.getBody();
                    if (response.getStatusCode().is2xxSuccessful() && responseBody != null) {
                        Boolean isValid = (Boolean) responseBody.get("valid");
                        if (Boolean.TRUE.equals(isValid)) {
                            isVerified = true;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("REMOTE_AUTH_ERROR: Biometric verification failed: " + e.getMessage());
                }
            }

            if (!isVerified) {
                return ResponseEntity.status(403)
                        .body(Map.of(
                            "error", "2FA Verification Required", 
                            "message", "Please provide a valid 6-digit code or Face ID",
                            "2fa_required", true
                        ));
            }
        }
        
        return ResponseEntity.ok(prescriptionRepository.save(prescription));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Prescription>> getForPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(prescriptionRepository.findByPatientId(patientId));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Prescription>> getForDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(prescriptionRepository.findByDoctorId(doctorId));
    }
}
