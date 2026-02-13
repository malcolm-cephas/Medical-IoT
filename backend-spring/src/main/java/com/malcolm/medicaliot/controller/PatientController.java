package com.malcolm.medicaliot.controller;

import com.malcolm.medicaliot.model.User;
import com.malcolm.medicaliot.repository.UserRepository;
import com.malcolm.medicaliot.model.SensorData;
import com.malcolm.medicaliot.repository.SensorDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SensorDataRepository sensorDataRepository;

    @GetMapping
    public ResponseEntity<?> getPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search) {

        Page<User> patientPage;
        if (search.isEmpty()) {
            patientPage = userRepository.findByRole("PATIENT", PageRequest.of(page, size));
        } else {
            patientPage = userRepository.findByRoleAndUsernameContainingIgnoreCase("PATIENT", search,
                    PageRequest.of(page, size));
        }

        List<Map<String, Object>> patientSummaries = patientPage.getContent().stream().map(user -> {
            Map<String, Object> summary = new HashMap<>();
            summary.put("id", user.getId());
            summary.put("username", user.getUsername());
            summary.put("department", user.getDepartment());

            // Get latest vitals for summary directly from DB
            java.util.Optional<SensorData> latestOpt = sensorDataRepository
                    .findFirstByPatientIdOrderByTimestampDesc(user.getUsername());

            if (latestOpt.isPresent()) {
                SensorData latest = latestOpt.get();
                summary.put("latestHeartRate", latest.getHeartRate());
                summary.put("latestSpo2", latest.getSpo2());
                summary.put("lastUpdate",
                        latest.getTimestamp() != null ? latest.getTimestamp().toString() : "Just now");
            } else {
                summary.put("latestHeartRate", "--");
                summary.put("latestSpo2", "--");
                summary.put("lastUpdate", "No data");
            }

            return summary;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("patients", patientSummaries);
        response.put("currentPage", patientPage.getNumber());
        response.put("totalItems", patientPage.getTotalElements());
        response.put("totalPages", patientPage.getTotalPages());

        return ResponseEntity.ok(response);
    }
}
