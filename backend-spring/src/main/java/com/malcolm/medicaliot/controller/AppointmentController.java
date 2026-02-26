package com.malcolm.medicaliot.controller;

import com.malcolm.medicaliot.model.Appointment;
import com.malcolm.medicaliot.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Generic Controller for Appointment Management.
 * Provides basic CRUD operations for appointments.
 * Note: Role-specific logic (Patient/Doctor) is often handled in their
 * respective controllers.
 */
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService service;

    /**
     * Books a new appointment.
     * 
     * @param request The appointment details including doctorId, patientId, and
     *                time.
     * @return The created appointment or error message.
     */
    @PostMapping("/book")
    public ResponseEntity<?> bookAppointment(@RequestBody Appointment request) {
        try {
            // Assume the request body contains basic fields. In a real app, use DTOs.
            // Check status is "PENDING"
            request.setStatus("PENDING");
            // Basic validation
            if (request.getDoctorId() == null || request.getPatientId() == null
                    || request.getAppointmentTime() == null) {
                return ResponseEntity.badRequest().body("Doctor ID, Patient ID, and Time are required.");
            }

            Long doctorId = request.getDoctorId();
            Long patientId = request.getPatientId();
            return ResponseEntity.ok(service.bookAppointment(doctorId, patientId,
                    request.getAppointmentTime(), request.getReason()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Retrieves all appointments for a specific doctor.
     * 
     * @param doctorId The doctor's unique identifier.
     * @return List of appointments.
     */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<Appointment>> getDoctorAppointments(@PathVariable Long doctorId) {
        return ResponseEntity.ok(service.getAppointmentsForDoctor(doctorId));
    }

    /**
     * Retrieves all appointments for a specific patient.
     * 
     * @param patientId The patient's unique identifier.
     * @return List of appointments.
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Appointment>> getPatientAppointments(@PathVariable Long patientId) {
        return ResponseEntity.ok(service.getAppointmentsForPatient(patientId));
    }

    /**
     * Updates the status of an appointment (e.g., CONFIRM, CANCEL, COMPLETE).
     * 
     * @param id     The appointment ID.
     * @param status The new status string.
     * @return The updated appointment.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Appointment> updateStatus(@PathVariable Long id, @RequestParam String status) {
        Long appointmentId = id;
        return ResponseEntity.ok(service.updateStatus(appointmentId, status));
    }
}
