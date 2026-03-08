package com.malcolm.medicaliot.repository;

import com.malcolm.medicaliot.model.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SensorDataRepository extends JpaRepository<SensorData, Long> {
    List<SensorData> findByPatientIdOrderByTimestampAsc(String patientId);

    java.util.Optional<SensorData> findFirstByPatientIdOrderByTimestampDesc(String patientId);

    // For getting the latest vitals across all patients
    List<SensorData> findTopByPatientIdOrderByTimestampDesc(String patientId);
}
