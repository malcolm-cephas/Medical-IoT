package com.malcolm.medicaliot.controller;

import com.malcolm.medicaliot.model.SecurityEvent;
import com.malcolm.medicaliot.repository.SecurityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for Data Export.
 * Allows administrators to export security logs and other sensitive data
 * for external auditing or compliance reporting.
 */
@RestController
@RequestMapping("/api/export")
public class ExportController {

    @Autowired
    private SecurityRepository securityRepository;

    /**
     * Exports all security logs as a CSV file.
     * Useful for integration with SIEM tools or manual review.
     * 
     * @return A CSV-formatted string as a file download attachment.
     */
    @GetMapping("/logs/csv")
    public ResponseEntity<String> exportLogs() {
        List<SecurityEvent> events = securityRepository.findAll();
        StringBuilder csv = new StringBuilder("ID,EventType,Severity,Description,IP,Timestamp\n");
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        for (SecurityEvent event : events) {
            csv.append(event.getId()).append(",")
                    .append(event.getEventType()).append(",")
                    .append(event.getSeverity()).append(",")
                    .append("\"").append(event.getDescription().replace("\"", "\"\"")).append("\",")
                    .append(event.getTriggeredByIp()).append(",")
                    .append(event.getTimestamp() != null ? event.getTimestamp().format(formatter) : "")
                    .append("\n");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"security_logs.csv\"")
                .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                .body(csv.toString());
    }
}
