package com.malcolm.medicaliot.controller;

import com.malcolm.medicaliot.service.LockdownService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/emergency")
public class EmergencyOverrideController {

    @Autowired
    private LockdownService lockdownService;

    @PostMapping("/override")
    public ResponseEntity<?> emergencyOverride(@RequestBody Map<String, String> body) {
        String doctorId = body.get("doctorId");
        String patientId = body.get("patientId");
        String reason = body.get("reason");

        if (reason == null || reason.isEmpty()) {
            return ResponseEntity.badRequest().body("Emergency Reason is MANDATORY.");
        }

        // Log the "Break-Glass" event securely
        lockdownService.logEvent("EMERGENCY_OVERRIDE", "CRITICAL",
                "Doc " + doctorId + " accessed Patient " + patientId + ". Reason: " + reason,
                "127.0.0.1");

        // In a real system, we would issue a temporary JWT with "EMERGENCY_ACCESS"
        // scope.
        // For this demo, we return success so the frontend can display the data.
        return ResponseEntity.ok(Map.of(
                "status", "GRANTED",
                "message", "Emergency Access Granted. Event Logged.",
                "emergencyToken", "EMERGENCY_TEMP_TOKEN_" + System.currentTimeMillis()));
    }
}
