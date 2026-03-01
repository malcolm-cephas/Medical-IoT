package com.malcolm.medicaliot.controller;

import com.malcolm.medicaliot.model.Prescription;
import com.malcolm.medicaliot.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing Prescriptions.
 * Exposes endpoints to create and retrieve prescription records.
 */
@RestController // Indicates that this class is a REST controller and responses are
                // automatically serialized to JSON.
@RequestMapping("/api/prescriptions") // Base URL path for all endpoints in this controller.
@RequiredArgsConstructor // Lombok annotation to generate a constructor with required arguments (final
                         // fields).
@CrossOrigin(origins = "*") // Allows Cross-Origin Resource Sharing (CORS) from any domain.
public class PrescriptionController {

    private final PrescriptionRepository prescriptionRepository;

    /**
     * Endpoint to add a new prescription.
     * 
     * @param prescription The prescription object to save (deserialized from JSON
     *                     request body).
     * @return The saved prescription object wrapped in a ResponseEntity with HTTP
     *         200 OK.
     */
    @PostMapping("/add")
    public ResponseEntity<Prescription> addPrescription(@RequestBody Prescription prescription) {
        // Saves the prescription to the database using the repository.
        // The ID will be automatically generated.
        @org.springframework.lang.NonNull Prescription saved = prescriptionRepository.save(prescription);
        return ResponseEntity.ok(saved);
    }

    /**
     * Endpoint to retrieve all prescriptions for a specific patient.
     * 
     * @param patientId The ID of the patient to filter by.
     * @return A list of prescriptions for the given patient.
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Prescription>> getForPatient(@PathVariable Long patientId) {
        // Uses a custom finder method in the repository to search by patientId.
        return ResponseEntity.ok(prescriptionRepository.findByPatientId(patientId));
    }

    /**
     * Endpoint to retrieve all prescriptions issued by a specific doctor.
     * 
     * @param doctorId The ID of the doctor to filter by.
     * @return A list of prescriptions issued by the given doctor.
     */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Prescription>> getForDoctor(@PathVariable Long doctorId) {
        // Uses a custom finder method in the repository to search by doctorId.
        return ResponseEntity.ok(prescriptionRepository.findByDoctorId(doctorId));
    }
}
