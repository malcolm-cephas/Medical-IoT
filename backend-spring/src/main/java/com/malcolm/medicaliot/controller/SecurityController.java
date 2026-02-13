package com.malcolm.medicaliot.controller;

import com.malcolm.medicaliot.model.SecurityEvent;
import com.malcolm.medicaliot.repository.SecurityRepository;
import com.malcolm.medicaliot.service.LockdownService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest; // Correct import for IP address

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/security")
public class SecurityController {

    @Autowired
    private LockdownService lockdownService;

    @Autowired
    private SecurityRepository securityRepository;

    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        return ResponseEntity.ok(Map.of(
                "isLockdown", lockdownService.isLockdown(),
                "reason", lockdownService.getReason()));
    }

    @PostMapping("/lockdown")
    public ResponseEntity<?> enableLockdown(@RequestBody Map<String, String> body, HttpServletRequest request) {
        // In real app, check if user is ADMIN here
        String reason = body.getOrDefault("reason", "Manual Admin Lockdown");
        lockdownService.enableLockdown(reason, request.getRemoteAddr());
        return ResponseEntity.ok("System Locked Down");
    }

    @PostMapping("/unlock")
    public ResponseEntity<?> disableLockdown(HttpServletRequest request) {
        // In real app, check if user is ADMIN here
        lockdownService.disableLockdown(request.getRemoteAddr());
        return ResponseEntity.ok("System Unlocked");
    }

    @GetMapping("/events")
    public ResponseEntity<List<SecurityEvent>> getEvents() {
        return ResponseEntity.ok(securityRepository.findTop10ByOrderByTimestampDesc());
    }
}
