package com.malcolm.medicaliot.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import jakarta.annotation.PostConstruct;
import java.util.Map;

@Service
public class KeyAuthorityService {

    @Value("${analytics.url}")
    private String analyticsBaseUrl; // e.g., http://localhost:4242/analyze

    private String cachedPublicKey;
    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void init() {
        fetchPublicKey();
    }

    public void fetchPublicKey() {
        try {
            // Base URL is .../analyze. We need .../public-key
            // Since analytics.url is configurable, usually host:port/analyze
            // We strip /analyze
            String baseUrl = analyticsBaseUrl.replace("/analyze", "");
            String url = baseUrl + "/public-key";

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.containsKey("public_key")) {
                this.cachedPublicKey = response.get("public_key").toString();
                System.out.println("KEY AUTHORITY: Successfully fetched Master Public Key.");
            }
        } catch (Exception e) {
            System.err.println("KEY AUTHORITY: Failed to fetch Public Key from Python Engine. Encryption may fail.");
            // e.printStackTrace();
        }
    }

    public String getPublicKey() {
        if (cachedPublicKey == null) {
            fetchPublicKey();
        }
        return cachedPublicKey;
    }
}
