package com.malcolm.mcpserver.repository;

import com.malcolm.mcpserver.model.PatientConsent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ConsentRepository extends JpaRepository<PatientConsent, Long> {
    List<PatientConsent> findByPatientId(String patientId);
    List<PatientConsent> findByDoctorIdAndStatus(String doctorId, String status);
    Optional<PatientConsent> findByPatientIdAndDoctorId(String patientId, String doctorId);
}
