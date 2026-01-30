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

@RestController
@RequestMapping("/api/sensor")
@CrossOrigin(origins = "http://localhost:5173") // Allow Vite Frontend
public class SensorController {

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
            if (lockdownService.isLockdown()) {
                return ResponseEntity.status(403).body("SYSTEM_LOCKDOWN: Data Ingestion Paused.");
            }

            // 0. Store in Persistent History
            SensorData entity = new SensorData();
            entity.setPatientId(data.getPatientId());
            entity.setHeartRate(data.getHeartRate());
            entity.setSpo2(data.getSpo2());
            entity.setTemperature(data.getTemperature());
            entity.setSystolicBP(data.getSystolicBP());
            entity.setDiastolicBP(data.getDiastolicBP());
            sensorDataRepository.save(entity);

            // 1. Receive Data
            System.out.println("Received Data for Patient: " + data.getPatientId());

            // 2. Encrypt using CP-ABE with Dynamic Consent
            // Base Policy: Specialists (Doctors in Cardiology)
            StringBuilder policyBuilder = new StringBuilder("((Role:Doctor AND Dept:Cardiology)");

            // Dynamic Policy: Add Consent Tokens from Approved Doctors
            List<com.malcolm.medicaliot.model.PatientConsent> consents = consentRepository
                    .findByPatientId(data.getPatientId());
            if (consents != null) {
                for (com.malcolm.medicaliot.model.PatientConsent consent : consents) {
                    if ("APPROVED".equals(consent.getStatus())) {
                        policyBuilder.append(" OR (Consent:").append(consent.getPolicyToken()).append(")");
                    }
                }
            }
            policyBuilder.append(")"); // Close the outer OR block logic (conceptually)

            String policy = policyBuilder.toString();
            System.out.println("Generated Dynamic ABE Policy: " + policy);

            String sensitiveData = "HR:" + data.getHeartRate() + ",SpO2:" + data.getSpo2();

            String encryptedData = abeService.encrypt(sensitiveData, policy);
            System.out.println("Encrypted Data: " + encryptedData);

            // 3. Store Encrypted Blob in IPFS
            String cid = ipfsService.store(encryptedData);

            // 4. Log Hash to Blockchain
            String txHash = blockchainService.logTransaction(data.getPatientId(), cid, "Vitals Upload");

            // 5. Send to Python Analytics (Asynchronously)
            new Thread(() -> analyticsService.analyzeData(data)).start();

            return ResponseEntity.ok("Data processed. IPFS CID: " + cid + ", TxHash: " + txHash);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error processing data: " + e.getMessage());
        }
    }

    @GetMapping("/history/{patientId}")
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
            return dto;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}
