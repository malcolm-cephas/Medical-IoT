package com.malcolm.medicaliot.controller;

import com.malcolm.medicaliot.dto.AvailabilityDto;
import com.malcolm.medicaliot.model.Appointment;
import com.malcolm.medicaliot.model.DoctorAvailability;
import com.malcolm.medicaliot.service.AppointmentService;
import com.malcolm.medicaliot.service.DoctorAvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    @Autowired
    private DoctorAvailabilityService availabilityService;

    @Autowired
    private AppointmentService appointmentService;

    /**
     * Doctor sets their availability
     * Adapted from the NestJS repository's set-availability endpoint
     */
    @PostMapping("/set-availability")
    public ResponseEntity<?> setAvailability(
            @RequestHeader("X-User-Id") String doctorId,
            @RequestBody AvailabilityDto dto) {
        try {
            List<DoctorAvailability> availabilities = availabilityService.setAvailability(doctorId, dto);
            return ResponseEntity.ok(Map.of(
                    "message", "Availability set successfully for " + availabilities.size() + " days",
                    "slots", availabilities));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "example", Map.of(
                            "dayOfWeek", "MONDAY",
                            "startTime", "10:00:00",
                            "endTime", "20:00:00")));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all available slots for a specific doctor
     */
    @GetMapping("/{doctorId}/slots")
    public ResponseEntity<?> getDoctorSlots(@PathVariable String doctorId) {
        try {
            List<DoctorAvailability> slots = availabilityService.getAvailableSlots(doctorId);
            return ResponseEntity.ok(slots);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch slots"));
        }
    }

    /**
     * Get all appointments for a doctor
     */
    @GetMapping("/appointments")
    public ResponseEntity<?> getDoctorAppointments(@RequestHeader("X-User-Id") String doctorId) {
        try {
            List<Appointment> appointments = appointmentService.getDoctorAppointments(doctorId);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch appointments"));
        }
    }

    /**
     * Complete an appointment
     */
    @PostMapping("/appointments/{appointmentId}/complete")
    public ResponseEntity<?> completeAppointment(
            @PathVariable Long appointmentId,
            @RequestHeader("X-User-Id") String doctorId) {
        try {
            Appointment appointment = appointmentService.completeAppointment(appointmentId, doctorId);
            return ResponseEntity.ok(Map.of(
                    "message", "Appointment marked as completed",
                    "appointment", appointment));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Cancel a slot
     */
    @PostMapping("/slots/{slotId}/cancel")
    public ResponseEntity<?> cancelSlot(@PathVariable Long slotId) {
        try {
            DoctorAvailability slot = availabilityService.cancelSlot(slotId);
            return ResponseEntity.ok(Map.of(
                    "message", "Slot cancelled successfully",
                    "slot", slot));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }
}
