package com.malcolm.medicaliot.controller;

import com.malcolm.medicaliot.model.SecurityEvent;
import com.malcolm.medicaliot.repository.SecurityRepository;
import com.malcolm.medicaliot.service.LockdownService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

/**
 * Controller for Security Audit and Control.
 * Manages the immutable audit trail (blockchain), system lockdown status,
 * and security event logging monitoring.
 */
@RestController
@RequestMapping("/api/security")
public class SecurityController {

    @Autowired
    private LockdownService lockdownService;

    @Autowired
    private SecurityRepository securityRepository;

    @Autowired
    private com.malcolm.medicaliot.service.BlockchainService blockchainService;

    /**
     * Retrieves the full blockchain audit trail.
     * 
     * @return List of blocks representing the immutable history of critical events.
     */
    @GetMapping("/audit-trail")
    public ResponseEntity<List<com.malcolm.medicaliot.service.BlockchainService.Block>> getAuditTrail() {
        return ResponseEntity.ok(blockchainService.getChain());
    }

    /**
     * Checks the current system lockdown status.
     * 
     * @return Map containing 'isLockdown' boolean and the 'reason' string.
     */
    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        return ResponseEntity.ok(Map.of(
                "isLockdown", lockdownService.isLockdown(),
                "reason", lockdownService.getReason()));
    }

    /**
     * Triggers a system-wide lockdown.
     * Restricted to ADMIN role.
     * This blocks non-essential and sensitive operations.
     * 
     * @param body    Map containing the 'reason' for lockdown.
     * @param request HttpServletRequest to capture the admin's IP.
     * @return Confirmation message.
     */
    @PostMapping("/lockdown")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> enableLockdown(@RequestBody Map<String, String> body, HttpServletRequest request) {
        // In real app, check if user is ADMIN here
        String reason = body.getOrDefault("reason", "Manual Admin Lockdown");
        lockdownService.enableLockdown(reason, request.getRemoteAddr());
        return ResponseEntity.ok("System Locked Down");
    }

    /**
     * Lifts a system-wide lockdown.
     * Restricted to ADMIN role.
     * 
     * @param request HttpServletRequest to capture the admin's IP.
     * @return Confirmation message.
     */
    @PostMapping("/unlock")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> disableLockdown(HttpServletRequest request) {
        // In real app, check if user is ADMIN here
        lockdownService.disableLockdown(request.getRemoteAddr());
        return ResponseEntity.ok("System Unlocked");
    }

    /**
     * Retrieves recent security events/alerts.
     * Restricted to ADMIN role.
     * 
     * @return List of the top 10 most recent security events.
     */
    @GetMapping("/events")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SecurityEvent>> getEvents() {
        return ResponseEntity.ok(securityRepository.findTop10ByOrderByTimestampDesc());
    }
}
