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

/**
 * Controller for Doctor-specific operations.
 * Handles setting availability slots and managing assigned appointments.
 */
@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    @Autowired
    private DoctorAvailabilityService availabilityService;

    @Autowired
    private AppointmentService appointmentService;

    /**
     * Endpoint for a doctor to set their availability for specific days.
     * This defines the "Office Hours" slots that patients can book.
     * 
     * @param doctorId The ID of the doctor (from header).
     * @param dto      Data Transfer Object containing day of week, start time, and
     *                 end time.
     * @return A list of created availability slots or an error message.
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
     * Retrieves all availability slots configured for a specific doctor.
     * 
     * @param doctorId The ID of the doctor.
     * @return List of availability objects.
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
     * Retrieves all appointments assigned to the requesting doctor.
     * 
     * @param doctorId The ID of the doctor (from header).
     * @return List of appointments.
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
     * Marks a specific appointment as completed.
     * 
     * @param appointmentId The ID of the appointment to complete.
     * @param doctorId      The ID of the doctor performing the action.
     * @return The updated appointment object.
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
     * Cancels a specific availability slot.
     * This effectively removes it from the list of bookable times.
     * 
     * @param slotId The ID of the slot to cancel.
     * @return The cancelled slot object.
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
