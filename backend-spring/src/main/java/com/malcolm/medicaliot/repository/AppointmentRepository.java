package com.malcolm.medicaliot.repository;

import com.malcolm.medicaliot.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientId(String patientId);
    
    List<Appointment> findByDoctorId(String doctorId);
    
    List<Appointment> findByPatientIdAndStatus(String patientId, String status);
    
    List<Appointment> findByDoctorIdAndStatus(String doctorId, String status);
}
