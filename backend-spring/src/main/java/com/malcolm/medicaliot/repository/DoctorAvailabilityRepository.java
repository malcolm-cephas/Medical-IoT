package com.malcolm.medicaliot.repository;

import com.malcolm.medicaliot.model.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {
    List<DoctorAvailability> findByDoctorIdAndStatus(String doctorId, String status);

    List<DoctorAvailability> findByDoctorIdAndStatusAndFromTimeAfter(
            String doctorId, String status, LocalDateTime fromTime);

    List<DoctorAvailability> findByStatus(String status);
}
