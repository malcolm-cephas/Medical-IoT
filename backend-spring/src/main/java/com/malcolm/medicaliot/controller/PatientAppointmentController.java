package com.malcolm.medicaliot.controller;

import com.malcolm.medicaliot.model.Appointment;
import com.malcolm.medicaliot.model.DoctorAvailability;
import com.malcolm.medicaliot.model.User;
import com.malcolm.medicaliot.repository.UserRepository;
import com.malcolm.medicaliot.service.AppointmentService;
import com.malcolm.medicaliot.service.DoctorAvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patient")
public class PatientAppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private DoctorAvailabilityService availabilityService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get all doctors
     * Adapted from the NestJS repository's all-doctors endpoint
     */
    @GetMapping("/all-doctors")
    public ResponseEntity<?> getAllDoctors() {
        try {
            List<User> doctors = userRepository
                    .findByRole("DOCTOR", org.springframework.data.domain.PageRequest.of(0, 100))
                    .getContent();

            List<Map<String, Object>> doctorList = doctors.stream().map(doctor -> {
                Map<String, Object> doctorInfo = new HashMap<>();
                doctorInfo.put("id", doctor.getId());
                doctorInfo.put("username", doctor.getUsername());
                doctorInfo.put("department", doctor.getDepartment());
                doctorInfo.put("role", doctor.getRole());
                return doctorInfo;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(doctorList);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch doctors"));
        }
    }

    /**
     * Get available slots for a specific doctor
     * Adapted from the NestJS repository's all-doctors/:doctorId/slots endpoint
     */
    @GetMapping("/all-doctors/{doctorId}/slots")
    public ResponseEntity<?> getDoctorSlots(@PathVariable String doctorId) {
        try {
            List<DoctorAvailability> slots = availabilityService.getAvailableSlots(doctorId);
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch slots for doctor"));
        }
    }

    /**
     * Book an appointment
     * Adapted from the NestJS repository's book-appointment/:slotId endpoint
     */
    @PostMapping("/book-appointment/{slotId}")
    public ResponseEntity<?> bookAppointment(
            @PathVariable Long slotId,
            @RequestHeader("X-User-Id") String patientId) {
        try {
            Map<String, Object> result = appointmentService.bookAppointment(slotId, patientId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of(
                    "error", "Failed to book appointment: " + e.getMessage()));
        }
    }

    /**
     * Get all appointments for a patient
     */
    @GetMapping("/appointments")
    public ResponseEntity<?> getPatientAppointments(@RequestHeader("X-User-Id") String patientId) {
        try {
            List<Appointment> appointments = appointmentService.getPatientAppointments(patientId);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch appointments"));
        }
    }

    /**
     * Cancel an appointment
     */
    @PostMapping("/appointments/{appointmentId}/cancel")
    public ResponseEntity<?> cancelAppointment(
            @PathVariable Long appointmentId,
            @RequestHeader("X-User-Id") String patientId) {
        try {
            Appointment appointment = appointmentService.cancelAppointment(appointmentId, patientId);
            return ResponseEntity.ok(Map.of(
                    "message", "Appointment cancelled successfully",
                    "appointment", appointment));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }
}
