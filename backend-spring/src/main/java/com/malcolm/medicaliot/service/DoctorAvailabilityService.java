package com.malcolm.medicaliot.service;

import com.malcolm.medicaliot.dto.AvailabilityDto;
import com.malcolm.medicaliot.model.DoctorAvailability;
import com.malcolm.medicaliot.repository.DoctorAvailabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DoctorAvailabilityService {

    @Autowired
    private DoctorAvailabilityRepository availabilityRepository;

    public DoctorAvailability setAvailability(String doctorId, AvailabilityDto dto) {
        // Validate time range
        if (dto.getToTime().isBefore(dto.getFromTime()) || dto.getToTime().isEqual(dto.getFromTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        DoctorAvailability availability = new DoctorAvailability();
        availability.setDoctorId(doctorId);
        availability.setFromTime(dto.getFromTime());
        availability.setToTime(dto.getToTime());
        availability.setStatus("AVAILABLE");

        return availabilityRepository.save(availability);
    }

    public List<DoctorAvailability> getAvailableSlots(String doctorId) {
        return availabilityRepository.findByDoctorIdAndStatusAndFromTimeAfter(
                doctorId, "AVAILABLE", LocalDateTime.now());
    }

    public List<DoctorAvailability> getAllAvailableSlots() {
        return availabilityRepository.findByStatus("AVAILABLE");
    }

    public DoctorAvailability markSlotAsBooked(Long slotId) {
        DoctorAvailability slot = availabilityRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        if (!"AVAILABLE".equals(slot.getStatus())) {
            throw new RuntimeException("Slot is not available");
        }

        slot.setStatus("BOOKED");
        return availabilityRepository.save(slot);
    }

    public DoctorAvailability cancelSlot(Long slotId) {
        DoctorAvailability slot = availabilityRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        slot.setStatus("CANCELLED");
        return availabilityRepository.save(slot);
    }
}
