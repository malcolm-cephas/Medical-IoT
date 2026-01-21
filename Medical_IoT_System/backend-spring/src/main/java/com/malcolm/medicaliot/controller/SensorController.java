package com.malcolm.medicaliot.controller;

import com.malcolm.medicaliot.dto.SensorDataDto;
import com.malcolm.medicaliot.service.ABEService;
import com.malcolm.medicaliot.service.AnalyticsService;
import com.malcolm.medicaliot.service.IPFSService;
import com.malcolm.medicaliot.service.BlockchainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    // Simple in-memory storage for demo
    private final List<SensorDataDto> history = new ArrayList<>();

    @PostMapping("/upload")
    public ResponseEntity<?> uploadData(@RequestBody SensorDataDto data) {
        // 0. Store in History
        history.add(data);

        // 1. Receive Data
        System.out.println("Received Data for Patient: " + data.getPatientId());

        // 2. Encrypt using CP-ABE (Mock Policy for now)
        String policy = "(Role:Doctor AND Dept:Cardiology)";
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
    }

    @GetMapping("/history/{patientId}")
    public ResponseEntity<?> getHistory(@PathVariable String patientId) {
        // Filter history by patientId
        List<SensorDataDto> patientHistory = history.stream()
                .filter(d -> d.getPatientId().equals(patientId))
                .collect(Collectors.toList());
        return ResponseEntity.ok(patientHistory);
    }
}
