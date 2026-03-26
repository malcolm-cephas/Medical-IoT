package com.malcolm.medicaliot.repository;

import com.malcolm.medicaliot.model.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    List<MedicalRecord> findByPatientId(String patientId);
    boolean existsByIpfsHash(String ipfsHash);
}
