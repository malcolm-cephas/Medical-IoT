package com.malcolm.medicaliot.controller;

import com.malcolm.medicaliot.dto.SensorDataDto;
import com.malcolm.medicaliot.service.ABEService;
import com.malcolm.medicaliot.service.AnalyticsService;
import com.malcolm.medicaliot.service.IPFSService;
import com.malcolm.medicaliot.service.BlockchainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import com.malcolm.medicaliot.model.SensorData;
import com.malcolm.medicaliot.repository.SensorDataRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/sensor")
public class SensorController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ABEService abeService;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private IPFSService ipfsService;

    @Autowired
    private BlockchainService blockchainService;

    @Autowired
    private com.malcolm.medicaliot.repository.ConsentRepository consentRepository;

    @Autowired
    private com.malcolm.medicaliot.service.LockdownService lockdownService;

    @Autowired
    private SensorDataRepository sensorDataRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadData(@RequestBody SensorDataDto data) {
        try {
            if (lockdownService == null)
                return ResponseEntity.status(500).body("Error: LockdownService is NULL");
            if (lockdownService.isLockdown()) {
                return ResponseEntity.status(403).body("SYSTEM_LOCKDOWN: Data Ingestion Paused.");
            }

            // 0. Store in Persistent History
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

            // Broadcast real-time update
            try {
                messagingTemplate.convertAndSend("/topic/vitals/" + data.getPatientId(), data);
                messagingTemplate.convertAndSend("/topic/ward", data);
            } catch (Exception e) {
                System.err.println("WebSocket Broadcast Failed: " + e.getMessage());
                // Non-critical, continue
            }

            // 1. Receive Data
            // System.out.println("Trace: Log Progress for Patient: " +
            // data.getPatientId()); // Removed as per instruction

            // 2. Encrypt using CP-ABE with Dynamic Consent
            String encryptedData;
            try {
                StringBuilder policyBuilder = new StringBuilder("((Role:Doctor AND Dept:Cardiology)");
                List<com.malcolm.medicaliot.model.PatientConsent> consents = consentRepository
                        .findByPatientId(data.getPatientId());
                if (consents != null) {
                    for (com.malcolm.medicaliot.model.PatientConsent consent : consents) {
                        if ("APPROVED".equals(consent.getStatus())) {
                            policyBuilder.append(" OR (Consent:").append(consent.getPolicyToken()).append(")");
                        }
                    }
                }
                policyBuilder.append(")");
                String policy = policyBuilder.toString();
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

            // 5. Send to Python Analytics (Asynchronously using Java 21 Virtual Threads)
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

    @GetMapping("/history/{patientId}")
    @PreAuthorize("@policyEngineService.evaluateAccess(authentication.name, #patientId, 'READ')")
    public ResponseEntity<?> getHistory(@PathVariable String patientId) {
        // Filter history by patientId from database
        List<SensorData> patientHistory = sensorDataRepository.findByPatientIdOrderByTimestampAsc(patientId);

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
