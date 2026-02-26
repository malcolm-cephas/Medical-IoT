package com.malcolm.medicaliot.controller;

import com.malcolm.medicaliot.dto.SensorDataDto;
import com.malcolm.medicaliot.service.ABEService;
import com.malcolm.medicaliot.service.AnalyticsService;
import com.malcolm.medicaliot.service.IPFSService;
import com.malcolm.medicaliot.service.BlockchainService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import com.malcolm.medicaliot.model.SensorData;
import com.malcolm.medicaliot.repository.SensorDataRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Controller for handling IoT Sensor Data.
 * This is the core ingestion point for vital signs from medical devices.
 * It manages the entire pipeline: Storage, Broadcasting, Encryption,
 * Decentralized Storage, and Audit Logging.
 */
@RestController
@RequestMapping("/api/sensor")
public class SensorController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // For real-time WebSocket broadcasting

    @Autowired
    private ABEService abeService; // For CP-ABE Encryption

    @Autowired
    private AnalyticsService analyticsService; // For triggering Python-based analytics

    @Autowired
    private IPFSService ipfsService; // For storing encrypted blobs on IPFS

    @Autowired
    private BlockchainService blockchainService; // For logging hashes to the blockchain

    @Autowired
    private com.malcolm.medicaliot.repository.ConsentRepository consentRepository;

    @Autowired
    private com.malcolm.medicaliot.service.LockdownService lockdownService;

    @Autowired
    private SensorDataRepository sensorDataRepository;

    /**
     * Uploads new sensor data to the system.
     * Executes a Multi-Step Pipeline:
     * 1. Persistence to relational DB (MySQL) for quick access.
     * 2. Real-time broadcast via WebSockets.
     * 3. CP-ABE Encryption using dynamic consent policies.
     * 4. Decentralized storage of encrypted data (IPFS).
     * 5. Immutable audit log of the transaction (Blockchain).
     * 6. Asynchronous initiation of predictive analytics.
     *
     * @param data DTO containing the collected vital signs.
     * @return Response status of the processing pipeline including IPFS CID and
     *         Blockchain TxHash.
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadData(@Valid @RequestBody SensorDataDto data) {
        try {
            // Preliminary Checks
            if (lockdownService == null)
                return ResponseEntity.status(500).body("Error: LockdownService is NULL");
            if (lockdownService.isLockdown()) {
                return ResponseEntity.status(403).body("SYSTEM_LOCKDOWN: Data Ingestion Paused.");
            }

            // 0. Store in Persistent History (Relational DB)
            try {
                SensorData entity = new SensorData();
                entity.setPatientId(data.getPatientId());
                entity.setHeartRate(data.getHeartRate());
                entity.setSpo2(data.getSpo2());
                entity.setTemperature(data.getTemperature());
                entity.setSystolicBP(data.getSystolicBP());
                entity.setDiastolicBP(data.getDiastolicBP());
                entity.setHumidity(data.getHumidity());
                sensorDataRepository.save(entity);
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Database Error: " + e.getMessage());
            }

            // Broadcast real-time update to connected Frontend clients
            try {
                // Topic for specific patient detail view
                messagingTemplate.convertAndSend("/topic/vitals/" + data.getPatientId(), data);
                // Topic for general ward view (summary updates)
                messagingTemplate.convertAndSend("/topic/ward", data);
            } catch (Exception e) {
                System.err.println("WebSocket Broadcast Failed: " + e.getMessage());
                // Non-critical failure, continue pipeline
            }

            // 1. Receive Data (Log placeholder)
            // System.out.println("Trace: Log Progress for Patient: " +
            // data.getPatientId());

            // 2. Encrypt using CP-ABE with Dynamic Consent
            String encryptedData;
            try {
                // Build Policy: Always allow Doctor in Cardiology. OR any user with a specific
                // consent token.
                StringBuilder policyBuilder = new StringBuilder("((Role:Doctor AND Dept:Cardiology)");
                List<com.malcolm.medicaliot.model.PatientConsent> consents = consentRepository
                        .findByPatientId(data.getPatientId());

                // Append dynamic consents if enabled/approved
                if (consents != null) {
                    for (com.malcolm.medicaliot.model.PatientConsent consent : consents) {
                        if ("APPROVED".equals(consent.getStatus())) {
                            policyBuilder.append(" OR (Consent:").append(consent.getPolicyToken()).append(")");
                        }
                    }
                }
                policyBuilder.append(")");
                String policy = policyBuilder.toString();

                // Encrypt only sensitive fields (HR, SpO2) into a string payload
                String sensitiveData = "HR:" + data.getHeartRate() + ",SpO2:" + data.getSpo2();
                encryptedData = abeService.encrypt(sensitiveData, policy);
            } catch (Exception e) {
                return ResponseEntity.status(500).body("ABE Encryption Error: " + e.getMessage());
            }

            // 3. Store Encrypted Blob in IPFS
            String cid = "N/A";
            try {
                cid = ipfsService.store(encryptedData);
            } catch (Exception e) {
                return ResponseEntity.status(500).body("IPFS Storage Error: " + e.getMessage());
            }

            // 4. Log Hash to Blockchain
            String txHash = "N/A";
            try {
                txHash = blockchainService.logTransaction(data.getPatientId(), cid, "Vitals Upload");
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Blockchain Log Error: " + e.getMessage());
            }

            // 5. Send to Python Analytics (Asynchronously using Java 21 Virtual Threads for
            // high concurrency)
            try {
                Thread.ofVirtual().start(() -> analyticsService.analyzeData(data));
            } catch (Exception e) {
                System.err.println("Analytics Trigger Failed: " + e.getMessage());
            }

            return ResponseEntity.ok("Data processed. IPFS CID: " + cid + ", TxHash: " + txHash);
        } catch (Exception e) {
            System.err.println("CRITICAL Controller Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Unknown Error: " + e.getLocalizedMessage());
        }
    }

    /**
     * Retrieves historical sensor data for a patient.
     * Protected by dynamic policy evaluation check.
     * 
     * @param patientId ID of the patient.
     * @return List of historical sensor data records.
     */
    @GetMapping("/history/{patientId}")
    @PreAuthorize("@policyEngineService.evaluateAccess(authentication.name, #patientId, 'READ')")
    public ResponseEntity<?> getHistory(@PathVariable String patientId) {
        // Filter history by patientId from database
        List<SensorData> patientHistory = sensorDataRepository.findByPatientIdOrderByTimestampAsc(patientId);

        // Convert Entities to DTOs for response
        List<SensorDataDto> dtos = patientHistory.stream().map(d -> {
            SensorDataDto dto = new SensorDataDto();
            dto.setPatientId(d.getPatientId());
            dto.setHeartRate(d.getHeartRate());
            dto.setSpo2(d.getSpo2());
            dto.setTemperature(d.getTemperature());
            dto.setSystolicBP(d.getSystolicBP());
            dto.setDiastolicBP(d.getDiastolicBP());
            dto.setHumidity(d.getHumidity());
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}
