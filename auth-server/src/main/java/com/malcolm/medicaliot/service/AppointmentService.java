package com.malcolm.medicaliot.service;

import com.malcolm.medicaliot.model.Appointment;
import com.malcolm.medicaliot.model.DoctorAvailability;
import com.malcolm.medicaliot.repository.AppointmentRepository;
import com.malcolm.medicaliot.repository.DoctorAvailabilityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private SystemLogService logService;

    @Autowired
    private DoctorAvailabilityRepository availabilityRepository;

    @Transactional
    public Map<String, Object> bookAppointment(Long doctorId, String patientIdStr,
            java.time.LocalDateTime appointmentTime) {
        Long patientId = Long.parseLong(patientIdStr);

        // 1. Check if doctor has office hours on this day of week
        String dayOfWeek = appointmentTime.getDayOfWeek().toString();
        List<DoctorAvailability> schedule = availabilityRepository.findByDoctorIdAndDayOfWeek(doctorId, dayOfWeek);

        if (schedule.isEmpty()) {
            throw new RuntimeException("Doctor does not work on " + dayOfWeek);
        }

        // 2. Check if time is within office hours
        java.time.LocalTime time = appointmentTime.toLocalTime();
        boolean withinHours = schedule.stream()
                .anyMatch(s -> !time.isBefore(s.getStartTime()) && !time.isAfter(s.getEndTime()));

        if (!withinHours) {
            throw new RuntimeException("Time is outside of doctor's office hours");
        }

        // 3. Check for double booking
        List<Appointment> existing = appointmentRepository.findByDoctorId(doctorId);
        boolean isDoubleBooked = existing.stream().anyMatch(a -> a.getAppointmentTime().equals(appointmentTime) &&
                !"CANCELLED".equals(a.getStatus()));

        if (isDoubleBooked) {
            throw new RuntimeException("Doctor is already booked at this time");
        }

        // Create Appointment
        Appointment appointment = new Appointment(
                doctorId,
                patientId,
                appointmentTime,
                "General Checkup",
                "CONFIRMED");
        appointment = appointmentRepository.save(appointment);

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("message", "Appointment booked successfully");
        response.put("appointmentId", appointment.getId());

        logService.log(patientIdStr, "BOOK_APPOINTMENT",
                "Booked appointment with Doctor " + doctorId + " for " + appointmentTime, "SUCCESS");

        return response;
    }

    // For legacy/simple controller support
    public Appointment bookAppointment(Long doctorId,
            Long patientId, java.time.LocalDateTime time, String reason) {
        Appointment appointment = new Appointment(doctorId, patientId, time, reason, "PENDING");
        return appointmentRepository.save(appointment);
    }

    public List<Appointment> getDoctorAppointments(String doctorIdStr) {
        Long doctorId = Long.parseLong(doctorIdStr);
        return appointmentRepository.findByDoctorId(doctorId);
    }

    // Legacy support
    public List<Appointment> getAppointmentsForDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId);
    }

    public List<Appointment> getPatientAppointments(String patientIdStr) {
        Long patientId = Long.parseLong(patientIdStr);
        return appointmentRepository.findByPatientId(patientId);
    }

    // Legacy support
    public List<Appointment> getAppointmentsForPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId);
    }

    public Appointment completeAppointment(Long appointmentId, String doctorIdStr) {
        if (appointmentId == null)
            throw new IllegalArgumentException("Appointment ID cannot be null");
        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Verify doctor owns this appointment
        if (!appt.getDoctorId().toString().equals(doctorIdStr)) {
            throw new RuntimeException("Unauthorized: This appointment does not belong to you.");
        }

        appt.setStatus("COMPLETED");
        Appointment savedAppt = appointmentRepository.save(appt);
        logService.log(doctorIdStr, "COMPLETE_APPOINTMENT",
                "Completed appointment #" + appointmentId + " for patient " + appt.getPatientId(), "SUCCESS");
        return savedAppt;
    }

    @Transactional
    public Appointment cancelAppointment(Long appointmentId, String patientIdStr) {
        if (appointmentId == null)
            throw new IllegalArgumentException("Appointment ID cannot be null");
        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Verify patient owns this appointment
        if (!appt.getPatientId().toString().equals(patientIdStr)) {
            throw new RuntimeException("Unauthorized: This appointment does not belong to you.");
        }

        appt.setStatus("CANCELLED");

        // Free up the slot?
        // Logic would be complex to find the exact slot again unless we link them.
        // For now, simpler to just mark appointment cancelled.

        Appointment savedAppt = appointmentRepository.save(appt);
        logService.log(patientIdStr, "CANCEL_APPOINTMENT",
                "Cancelled appointment #" + appointmentId + " with Doctor " + appt.getDoctorId(), "SUCCESS");
        return savedAppt;
    }

    public Appointment updateStatus(Long appointmentId, String status) {
        if (appointmentId == null)
            throw new IllegalArgumentException("Appointment ID cannot be null");
        Appointment appointment = appointmentRepository.findById(appointmentId).orElseThrow();
        appointment.setStatus(status);
        return appointmentRepository.save(appointment);
    }
}
