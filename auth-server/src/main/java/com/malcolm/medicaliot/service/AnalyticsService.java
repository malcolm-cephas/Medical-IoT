package com.malcolm.medicaliot.service;

import com.malcolm.medicaliot.dto.SensorDataDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Service
public class AnalyticsService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Value("${analytics.url}")
    private String analyticsUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("null")
    public void analyzeData(SensorDataDto data) {
        try {
            // Asynchronous call or fire-and-forget for now
            // In production, use Kafka/RabbitMQ
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(analyticsUrl, data,
                    (Class<Map<String, Object>>) (Class<?>) Map.class);
            Map<String, Object> result = response.getBody();
            System.out.println("Analytics Response: " + result);

            if (result != null && "HIGH".equals(result.get("risk_level"))) {
                messagingTemplate.convertAndSend("/topic/alerts", result);
            }
        } catch (Exception e) {
            System.err.println("Failed to contact Analytics Service: " + e.getMessage());
        }
    }
}
