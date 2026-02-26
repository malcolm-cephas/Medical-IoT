package com.malcolm.medicaliot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.malcolm.medicaliot.security.KeyAuthorityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

@Service
public class ABEService {

    @Autowired
    private KeyAuthorityService keyAuthorityService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @org.springframework.beans.factory.annotation.Value("${analytics.url}")
    private String analyticsBaseUrl; // e.g., http://localhost:4242/analyze

    @Autowired
    private org.springframework.web.client.RestTemplate restTemplate;

    public String encrypt(String data, String policy) {
        try {
            // DELEGATION MODE: Send data + policy to Python CP-ABE Service
            // This ensures mathematically correct CP-ABE (BN-254 curve) instead of local
            // RSA simulation.

            String baseUrl = analyticsBaseUrl.replace("/analyze", "");
            String url = baseUrl + "/abe/encrypt";

            Map<String, String> request = new HashMap<>();
            request.put("data", data);
            request.put("policy", policy);

            // Expecting JSON response: { "ciphertext": "...", "status": "success" }
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

            if (response != null && response.containsKey("ciphertext")) {
                // Return the ABE package string directly from the authority
                return (String) response.get("ciphertext");
            } else {
                throw new RuntimeException("Empty response from ABE Authority");
            }

        } catch (Exception e) {
            System.err.println("CP-ABE Delegation Failed: " + e.getMessage());
            // FAIL-CLOSED: Do not allow unencrypted or mock data in production flow.
            throw new RuntimeException("CRITICAL: ABE Encryption Service Unavailable. Upload Aborted for Security.");
        }
    }

    public String decrypt(String cipherText, String userJsonAttributes) {
        // Decryption requires User Secret Keys (USK) and Python Engine logic.
        // In this architecture, this happens on the client side or specialized service.
        return "Decryption requires Local Python Engine or Client-Side Tool";
    }

    private List<String> parsePolicy(String policy) {
        // Mock policy parsing to match existing logic
        if (policy.contains("Doctor"))
            return Arrays.asList("doctor", "cardiology");
        if (policy.contains("Nurse"))
            return Arrays.asList("nurse");
        return Arrays.asList("public");
    }
}
