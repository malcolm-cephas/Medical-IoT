package com.malcolm.medicaliot.controller;

import com.malcolm.medicaliot.model.DoctorAvailability;
import com.malcolm.medicaliot.service.DoctorAvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controller for managing Doctor Availability Slots.
 * Provides endpoints to add, retrieve, and delete availability.
 * Supplemented by DoctorController for doctor-specific workflows.
 */
@RestController
@RequestMapping("/api/availability")
public class DoctorAvailabilityController {

    @Autowired
    private DoctorAvailabilityService service;

    /**
     * Retrieves availability slots for a given doctor.
     * 
     * @param doctorId The doctor's ID.
     * @return List of availability slots.
     */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<DoctorAvailability>> getDoctorAvailability(@PathVariable Long doctorId) {
        return ResponseEntity.ok(service.getAvailableSlots(doctorId.toString()));
    }

    /**
     * Adds a new availability slot.
     * 
     * @param availability The availability object to save.
     * @return The saved object.
     */
    @PostMapping("/add")
    public ResponseEntity<DoctorAvailability> addAvailability(@RequestBody DoctorAvailability availability) {
        return ResponseEntity.ok(service.save(availability));
    }

    /**
     * Deletes a specific availability slot.
     * 
     * @param id The ID of the slot to remove.
     * @return Empty response on success.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAvailability(@PathVariable Long id) {
        service.cancelSlot(id);
        return ResponseEntity.ok().build();
    }
}
