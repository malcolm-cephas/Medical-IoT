package com.malcolm.medicaliot.service;

import com.malcolm.medicaliot.model.FaceVerificationSession;
import com.malcolm.medicaliot.model.User;
import com.malcolm.medicaliot.repository.FaceVerificationSessionRepository;
import com.malcolm.medicaliot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FaceService {

    private final UserRepository userRepository;
    private final FaceVerificationSessionRepository sessionRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${face.service.url:http://localhost:5050}")
    private String faceServiceUrl;

    /**
     * Extracts embedding for a doctor's face and stores it in the database.
     */
    public boolean registerFace(String username, MultipartFile file) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", file.getResource());

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(faceServiceUrl + "/extract_embedding", requestEntity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String embedding = response.getBody().get("embedding").toString();
                Optional<User> userOpt = userRepository.findByUsername(username);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    user.setFaceEmbedding(embedding);
                    userRepository.save(user);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Verifies an uploaded live photo against stored doctor embeddings.
     */
    public boolean verifyFace(String username, MultipartFile file) {
        try {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty() || userOpt.get().getFaceEmbedding() == null) {
                return false;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", file.getResource());
            body.add("target_embedding", userOpt.get().getFaceEmbedding());

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(faceServiceUrl + "/verify_face", requestEntity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Boolean match = (Boolean) response.getBody().get("match");
                if (Boolean.TRUE.equals(match)) {
                    // Create a new successful verification session
                    FaceVerificationSession session = new FaceVerificationSession(username, LocalDateTime.now(), true);
                    sessionRepository.save(session);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Checks if the doctor has a valid, active biometric session within the last 5 minutes.
     */
    public boolean isSessionValid(String username) {
        Optional<FaceVerificationSession> sessionOpt = sessionRepository.findTopByUsernameAndActiveTrueOrderByVerifiedAtDesc(username);
        if (sessionOpt.isPresent()) {
            FaceVerificationSession session = sessionOpt.get();
            if (!session.isExpired()) {
                return true;
            } else {
                session.setActive(false);
                sessionRepository.save(session);
            }
        }
        return false;
    }

    /**
     * Checks if the user has biometric data registered.
     */
    public boolean isEnrolled(String username) {
        return userRepository.findByUsername(username)
                .map(u -> u.getFaceEmbedding() != null)
                .orElse(false);
    }
}
