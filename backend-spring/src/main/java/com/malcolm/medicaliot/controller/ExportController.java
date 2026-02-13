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

@RestController
@RequestMapping("/api/export")
public class ExportController {

    @Autowired
    private SecurityRepository securityRepository;

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
