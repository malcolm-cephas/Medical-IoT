package com.malcolm.medicaliot.controller;

import com.malcolm.medicaliot.model.PatientConsent;
import com.malcolm.medicaliot.repository.ConsentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/consent")
public class ConsentController {

    @Autowired
    private ConsentRepository consentRepository;

    // Doctor requests access
    @PostMapping("/request")
    public ResponseEntity<?> requestAccess(@RequestBody Map<String, String> body) {
        String patientId = body.get("patientId");
        String doctorId = body.get("doctorId"); // In real app, get from SecurityContext

        // Check if already exists
        if (consentRepository.findByPatientIdAndDoctorId(patientId, doctorId).isPresent()) {
            return ResponseEntity.badRequest().body("Request already exists.");
        }

        PatientConsent consent = new PatientConsent();
        consent.setPatientId(patientId);
        consent.setDoctorId(doctorId);
        consent.setStatus("PENDING");

        return ResponseEntity.ok(consentRepository.save(consent));
    }

    // Patient approves/rejects
    @PostMapping("/respond")
    public ResponseEntity<?> respondToRequest(@RequestBody Map<String, String> body) {
        Long consentId = Long.parseLong(body.get("consentId"));
        String status = body.get("status"); // APPROVED / REJECTED

        Optional<PatientConsent> optionalConsent = consentRepository.findById(consentId);
        if (optionalConsent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PatientConsent consent = optionalConsent.get();
        consent.setStatus(status);
        if ("APPROVED".equals(status)) {
            consent.setApprovedAt(LocalDateTime.now());
        }

        return ResponseEntity.ok(consentRepository.save(consent));
    }

    // List pending requests for a patient
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<PatientConsent>> getPatientConsents(@PathVariable String patientId) {
        return ResponseEntity.ok(consentRepository.findByPatientId(patientId));
    }

    // Check status for a specific doctor
    @GetMapping("/check")
    public ResponseEntity<?> checkStatus(@RequestParam String patientId, @RequestParam String doctorId) {
        Optional<PatientConsent> consent = consentRepository.findByPatientIdAndDoctorId(patientId, doctorId);
        if (consent.isPresent()) {
            return ResponseEntity.ok(consent.get());
        }
        return ResponseEntity.notFound().build();
    }
}
