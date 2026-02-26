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

/**
 * Controller for managing Patient Consent.
 * Handles the workflow of doctors requesting access and patients
 * granting/denying it.
 */
@RestController
@RequestMapping("/api/consent")
public class ConsentController {

    @Autowired
    private ConsentRepository consentRepository;

    /**
     * Doctor requests access to a patient's data.
     * Creates a new consent record with status 'PENDING'.
     * 
     * @param body Map containing 'patientId' and 'doctorId'.
     * @return The created PatientConsent object or an error if already changes.
     */
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

    /**
     * Patient responds to a doctor's access request.
     * Updates the status to 'APPROVED' or 'REJECTED'.
     * 
     * @param body Map containing 'consentId' and 'status'.
     * @return The updated PatientConsent object.
     */
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

    /**
     * Lists all consent requests for a specific patient.
     * Used by the patient dashboard to show pending/active requests.
     * 
     * @param patientId The ID of the patient.
     * @return List of PatientConsent objects.
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<PatientConsent>> getPatientConsents(@PathVariable String patientId) {
        return ResponseEntity.ok(consentRepository.findByPatientId(patientId));
    }

    /**
     * Checks the specific consent status between a patient and a doctor.
     * Used by the doctor's dashboard to verify access rights.
     * 
     * @param patientId The patient ID.
     * @param doctorId  The doctor ID.
     * @return The PatientConsent object if found, or 404.
     */
    @GetMapping("/check")
    public ResponseEntity<?> checkStatus(@RequestParam String patientId, @RequestParam String doctorId) {
        Optional<PatientConsent> consent = consentRepository.findByPatientIdAndDoctorId(patientId, doctorId);
        if (consent.isPresent()) {
            return ResponseEntity.ok(consent.get());
        }
        return ResponseEntity.notFound().build();
    }
}
