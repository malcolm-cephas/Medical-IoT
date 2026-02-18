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

    public String encrypt(String data, String policy) {
        try {
            // 1. Get Master Public Key from Key Authority (Python Engine)
            String publicPem = keyAuthorityService.getPublicKey();
            if (publicPem == null) {
                return "MOCK_ENC[NO_KEY]:" + data;
            }

            // 2. Generate Local AES Key (128-bit)
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            SecretKey aesKey = keyGen.generateKey();

            // 3. Encrypt Data locally with AES-GCM
            // Generate IV
            byte[] iv = new byte[12];
            new SecureRandom().nextBytes(iv);

            Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, spec);

            byte[] cipherTextBytes = aesCipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // 4. Encrypt AES Key with RSA Public Key (Hybrid Encapsulation)
            // Parse PEM to PublicKey
            String pemClean = publicPem
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] encodedKey = Base64.getDecoder().decode(pemClean);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey rsaPublicKey = kf.generatePublic(keySpec);

            // Encrypt AES Key
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            rsaCipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);
            byte[] encryptedKeyBytes = rsaCipher.doFinal(aesKey.getEncoded());

            // 5. Construct JSON Payload (Matching Python format for compatibility)
            Map<String, Object> packageMap = new HashMap<>();
            packageMap.put("policy", parsePolicy(policy));
            packageMap.put("ciphertext", Base64.getEncoder().encodeToString(cipherTextBytes));
            packageMap.put("nonce", Base64.getEncoder().encodeToString(iv));
            packageMap.put("encrypted_key", Base64.getEncoder().encodeToString(encryptedKeyBytes));
            packageMap.put("status", "HYBRID_ENCRYPTION_JAVA_LOCAL");

            return "ABE:" + objectMapper.writeValueAsString(packageMap);

        } catch (Exception e) {
            System.err.println("Hybrid Encryption Failed: " + e.getMessage());
            e.printStackTrace();
            return "MOCK_ENC[ERROR]:" + data;
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
