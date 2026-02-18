package com.malcolm.medicaliot.controller;

import com.malcolm.medicaliot.model.DoctorAvailability;
import com.malcolm.medicaliot.service.DoctorAvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/availability")
public class DoctorAvailabilityController {

    @Autowired
    private DoctorAvailabilityService service;

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<DoctorAvailability>> getDoctorAvailability(@PathVariable Long doctorId) {
        return ResponseEntity.ok(service.getAvailableSlots(doctorId.toString()));
    }

    @PostMapping("/add")
    public ResponseEntity<DoctorAvailability> addAvailability(@RequestBody DoctorAvailability availability) {
        return ResponseEntity.ok(service.save(availability));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAvailability(@PathVariable Long id) {
        service.cancelSlot(id);
        return ResponseEntity.ok().build();
    }
}
