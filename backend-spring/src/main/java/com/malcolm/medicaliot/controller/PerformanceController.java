package com.malcolm.medicaliot.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/performance")
@CrossOrigin(origins = "http://localhost:5173")
public class PerformanceController {

    @GetMapping("/metrics")
    public ResponseEntity<?> getMetrics() {
        // In a real system, these would be tracked via Micrometer/AspectJ
        // For the demo, we simulate realistic benchmarks based on our encryption tests
        Map<String, Object> metrics = new HashMap<>();

        metrics.put("encryption_time_avg_ms", 45.2);
        metrics.put("decryption_time_avg_ms", 38.7);
        metrics.put("api_latency_avg_ms", 120.5);
        metrics.put("blockchain_log_time_ms", 2100.0); // Simulated block time
        metrics.put("system_uptime_seconds", System.currentTimeMillis() / 1000 % 10000);
        metrics.put("active_users", 5);
        metrics.put("throughput_req_per_sec", 150);

        return ResponseEntity.ok(metrics);
    }
}
