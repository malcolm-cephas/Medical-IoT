package com.malcolm.mcpserver.mcp;

import com.malcolm.mcpserver.model.SensorData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;

@Service
public class BackendClient {

    private final WebClient webClient;

    public BackendClient(WebClient.Builder webClientBuilder, @Value("${app.backend.url:http://localhost:8080}") String backendUrl) {
        this.webClient = webClientBuilder
                .baseUrl(java.util.Objects.requireNonNull(backendUrl))
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    public List<SensorData> getLatestVitals(String patientId) {
        return webClient.get()
                .uri("/api/vitals/history/{patientId}", patientId)
                .retrieve()
                .bodyToFlux(SensorData.class)
                .collectList()
                .block();
    }

    public SensorData getPatientCurrentStatus(String patientId) {
        return webClient.get()
                .uri("/api/vitals/current/{patientId}", patientId)
                .retrieve()
                .bodyToMono(SensorData.class)
                .block();
    }
}
