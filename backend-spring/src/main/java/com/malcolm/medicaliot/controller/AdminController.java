package com.malcolm.medicaliot.controller;

import com.malcolm.medicaliot.model.SystemLog;
import com.malcolm.medicaliot.service.SystemLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controller for Administrative operations.
 * Provides endpoints for monitoring system logs and other admin-only tasks.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private SystemLogService logService;

    /**
     * Retrieves all system logs.
     * Restricted to users with the 'ADMIN' role.
     * 
     * @return List of all system log entries.
     */
    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SystemLog>> getSystemLogs() {
        return ResponseEntity.ok(logService.getAllLogs());
    }
}
