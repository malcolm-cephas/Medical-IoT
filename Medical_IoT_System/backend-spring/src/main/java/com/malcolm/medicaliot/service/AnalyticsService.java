package com.malcolm.medicaliot.service;

import com.malcolm.medicaliot.dto.SensorDataDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Service
public class AnalyticsService {

    @Value("${analytics.url}")
    private String analyticsUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void analyzeData(SensorDataDto data) {
        try {
            // Asynchronous call or fire-and-forget for now
            // In production, use Kafka/RabbitMQ
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(analyticsUrl, data,
                    (Class<Map<String, Object>>) (Class<?>) Map.class);
            System.out.println("Analytics Response: " + response.getBody());
        } catch (Exception e) {
            System.err.println("Failed to contact Analytics Service: " + e.getMessage());
        }
    }
}
