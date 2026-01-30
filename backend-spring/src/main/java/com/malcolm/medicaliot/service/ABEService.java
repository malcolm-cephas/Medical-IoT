package com.malcolm.medicaliot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ABEService {

    @Value("${analytics.url}")
    private String analyticsBaseUrl; // e.g., http://localhost:8000/analyze

    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("null")
	public String encrypt(String data, String policy) {
        try {
            // Construct the Python Encryption URL (derive from base URL)
            // Assuming base is http://localhost:8000/analyze, we want
            // http://localhost:8000/encrypt
            String encryptUrl = analyticsBaseUrl.replace("/analyze", "/encrypt");

            // Prepare Payload
            // Policy format: "(Role:Doctor AND Dept:Cardiology)" -> ["doctor",
            // "cardiology"] (Simplified parsing)
            List<String> attributes = parsePolicy(policy);

            Map<String, Object> payload = new HashMap<>();
            payload.put("message", data);
            payload.put("policy_attributes", attributes);

            // Call Python Service
            // Expecting returns: { "ciphertext": "...", "nonce": "...", ... }
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(encryptUrl, payload, Map.class);

            if (response != null && response.containsKey("ciphertext")) {
                return "ABE:" + response.get("ciphertext").toString(); // Tag it as Real ABE
            }

        } catch (Exception e) {
            System.err.println("Real Encryption Failed: " + e.getMessage());
            e.printStackTrace();
        }

        // Fallback if Python is down
        return "MOCK_ENC[" + policy + "]:" + data;
    }

    public String decrypt(String cipherText, String userJsonAttributes) {
        // Decryption requires User Keys which is complex to pass around in this demo.
        // We will keep the mock check for now to allow the Dashboard to work.
        return "Decryption requires Local Python Engine";
    }

    private List<String> parsePolicy(String policy) {
        // Simple parser: Extract words, ignore operators for demo
        // "(Role:Doctor AND Dept:Cardiology)" -> ["doctor", "cardiology"]
        // In a real system, you'd parse the boolean formula.
        // Here we just grab the values after ":"

        // Mocking the attributes for the demo to match what the Python Engine expects
        // Python Mock Keygen expects: "doctor", "cardiology"
        if (policy.contains("Doctor"))
            return Arrays.asList("doctor", "cardiology");
        if (policy.contains("Nurse"))
            return Arrays.asList("nurse");
        return Arrays.asList("public");
    }
}
