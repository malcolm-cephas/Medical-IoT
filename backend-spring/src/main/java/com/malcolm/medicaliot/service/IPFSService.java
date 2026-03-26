package com.malcolm.medicaliot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

/**
 * Service to interface with local IPFS node (Kubo) RPC API.
 */
@Service
@Slf4j
public class IPFSService {

    private static final String IPFS_API_URL = "http://127.0.0.1:5001/api/v0/add";
    private static final String IPFS_GET_URL = "http://127.0.0.1:8080/ipfs/";

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Uploads a raw MultipartFile to IPFS.
     * Returns the Content Identifier (CID).
     */
    public String uploadToIpfs(MultipartFile file) {
        log.info("IPFS_TX: Uploading file: {}", file.getOriginalFilename());
        return uploadInternal(file.getOriginalFilename(), file.getResource());
    }

    /**
     * Alternative store method for raw string data (e.g., encrypted JSON blobs).
     * Used by SensorController for IoT data persistence.
     */
    public String store(String data) {
        log.info("IPFS_TX: Uploading raw string data blob");
        ByteArrayResource resource = new ByteArrayResource(data.getBytes()) {
            @Override
            public String getFilename() {
                return "encrypted_vitals.json";
            }
        };
        return uploadInternal("encrypted_vitals.json", resource);
    }

    private String uploadInternal(String filename, Object resource) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", resource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            @SuppressWarnings("unchecked")
        ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(IPFS_API_URL, requestEntity, (Class<Map<String, Object>>) (Class<?>) Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (response.getStatusCode().is2xxSuccessful() && responseBody != null) {
                String cid = (String) responseBody.get("Hash");
                log.info("IPFS_RX: Successfully indexed {}. CID: {}", filename, cid);
                return cid;
            } else {
                throw new RuntimeException("IPFS Node rejection: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("IPFS_ERR: Connection failed to {}. Error: {}", IPFS_API_URL, e.getMessage());
            throw new RuntimeException("Localized IPFS node error: " + e.getMessage());
        }
    }

    public ResponseEntity<byte[]> downloadFromIpfs(String cid) {
        String url = IPFS_GET_URL + cid;
        try {
            ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);
            if (response.getBody() == null) {
                throw new RuntimeException("Empty response from IPFS for CID: " + cid);
            }
            return response;
        } catch (Exception e) {
            log.error("IPFS_ERR: Failed to fetch CID {}: {}", cid, e.getMessage());
            throw new RuntimeException("Gateway retrieval failed for index: " + cid);
        }
    }
}
