package com.malcolm.medicaliot.service;

import com.malcolm.medicaliot.dto.AvailabilityDto;
import com.malcolm.medicaliot.model.DoctorAvailability;
import com.malcolm.medicaliot.repository.DoctorAvailabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorAvailabilityService {

    @Autowired
    private DoctorAvailabilityRepository repository;

    @Autowired
    private com.malcolm.medicaliot.repository.UserRepository userRepository;

    public List<DoctorAvailability> setAvailability(String username, AvailabilityDto dto) {
        com.malcolm.medicaliot.model.User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Doctor not found: " + username));
        Long doctorId = user.getId();
        java.util.List<DoctorAvailability> savedSchedules = new java.util.ArrayList<>();

        if (dto.getDaysOfWeek() != null && !dto.getDaysOfWeek().isEmpty()) {
            for (String day : dto.getDaysOfWeek()) {
                DoctorAvailability availability = new DoctorAvailability(
                        doctorId,
                        day,
                        dto.getStartTime(),
                        dto.getEndTime());
                savedSchedules.add(repository.save(availability));
            }
        } else if (dto.getDayOfWeek() != null) {
            DoctorAvailability availability = new DoctorAvailability(
                    doctorId,
                    dto.getDayOfWeek(),
                    dto.getStartTime(),
                    dto.getEndTime());
            savedSchedules.add(repository.save(availability));
        }
        return savedSchedules;
    }

    public List<DoctorAvailability> getAvailableSlots(String username) {
        com.malcolm.medicaliot.model.User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Doctor not found: " + username));
        return repository.findByDoctorId(user.getId());
    }

    public DoctorAvailability cancelSlot(Long slotId) {
        if (slotId == null)
            throw new IllegalArgumentException("Slot ID cannot be null");
        DoctorAvailability slot = repository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Schedule/Slot not found"));

        repository.delete(slot);
        return slot;
    }

    // Legacy support or internal use
    public Optional<DoctorAvailability> findById(Long id) {
        if (id == null)
            return Optional.empty();
        return repository.findById(id);
    }

    public DoctorAvailability save(DoctorAvailability slot) {
        if (slot == null)
            throw new IllegalArgumentException("Slot cannot be null");
        return repository.save(slot);
    }
}
