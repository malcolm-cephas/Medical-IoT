package com.malcolm.medicaliot.service;

import com.malcolm.medicaliot.model.Appointment;
import com.malcolm.medicaliot.model.DoctorAvailability;
import com.malcolm.medicaliot.model.User;
import com.malcolm.medicaliot.repository.AppointmentRepository;
import com.malcolm.medicaliot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorAvailabilityService availabilityService;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Map<String, Object> bookAppointment(Long slotId, String patientId) {
        // Get the slot and mark it as booked
        DoctorAvailability slot = availabilityService.markSlotAsBooked(slotId);

        // Create the appointment
        Appointment appointment = new Appointment();
        appointment.setDoctorId(slot.getDoctorId());
        appointment.setPatientId(patientId);
        appointment.setSlotId(slotId);
        appointment.setStatus("SCHEDULED");
        appointment.setAppointmentTime(slot.getFromTime());

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Get doctor and patient details
        User doctor = userRepository.findByUsername(slot.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        User patient = userRepository.findByUsername(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found"));

        // Build response
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Appointment booked successfully");

        Map<String, Object> appointmentDetails = new HashMap<>();
        appointmentDetails.put("appointmentId", savedAppointment.getId());
        appointmentDetails.put("doctorName", doctor.getUsername());
        appointmentDetails.put("doctorDepartment", doctor.getDepartment());
        appointmentDetails.put("patientName", patient.getUsername());
        appointmentDetails.put("fromTime", slot.getFromTime());
        appointmentDetails.put("toTime", slot.getToTime());
        appointmentDetails.put("status", savedAppointment.getStatus());

        response.put("appointment", appointmentDetails);

        return response;
    }

    public List<Appointment> getPatientAppointments(String patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    public List<Appointment> getDoctorAppointments(String doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    @Transactional
    public Appointment cancelAppointment(Long appointmentId, String userId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Verify the user is either the patient or doctor
        if (!appointment.getPatientId().equals(userId) && !appointment.getDoctorId().equals(userId)) {
            throw new RuntimeException("Unauthorized to cancel this appointment");
        }

        appointment.setStatus("CANCELLED");

        // Free up the slot
        availabilityService.cancelSlot(appointment.getSlotId());

        return appointmentRepository.save(appointment);
    }

    @Transactional
    public Appointment completeAppointment(Long appointmentId, String doctorId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getDoctorId().equals(doctorId)) {
            throw new RuntimeException("Unauthorized to complete this appointment");
        }

        appointment.setStatus("COMPLETED");
        return appointmentRepository.save(appointment);
    }
}
