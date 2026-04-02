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
    private final com.malcolm.medicaliot.service.FaceService faceService;
    private final com.malcolm.medicaliot.repository.SystemLogRepository logRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Endpoint to add a new prescription.
     * Doctors MUST have a valid face-verification session (active within 5 minutes).
     */
    @PostMapping("/add")
    public ResponseEntity<?> addPrescription(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Id", required = false) String doctorUsername,
            @RequestBody Prescription prescription) {
        
        if (prescription == null) {
            return ResponseEntity.badRequest().build();
        }

        // Logic for Doctors: Require Biometric 2FA session for prescription creation
        if ("DOCTOR".equalsIgnoreCase(role) || doctorUsername != null) {
            boolean isVerified = faceService.isSessionValid(doctorUsername);
            
            if (!isVerified) {
                logRepository.save(new com.malcolm.medicaliot.model.SystemLog(
                    doctorUsername, "PRESCRIPTION_CREATE_DENIED", 
                    "Doctor failed biometric session check", "FAILURE"
                ));
                return ResponseEntity.status(403)
                        .body(Map.of(
                            "error", "Biometric 2FA Required", 
                            "message", "Please perform Face ID verification before creating a prescription (session valid for 5 mins)",
                            "2fa_required", true
                        ));
            }
        }
        
        Prescription saved = prescriptionRepository.save(prescription);
        
        // Audit log for successful creation
        if (doctorUsername != null) {
            logRepository.save(new com.malcolm.medicaliot.model.SystemLog(
                doctorUsername, "PRESCRIPTION_CREATE", 
                "Prescription created with biometric verification", "SUCCESS"
            ));
        }
        
        return ResponseEntity.ok(saved);
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
