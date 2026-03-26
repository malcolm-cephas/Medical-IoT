package com.malcolm.medicaliot.controller;

import com.malcolm.medicaliot.model.MedicalRecord;
import com.malcolm.medicaliot.service.MedicalRecordService;
import com.malcolm.medicaliot.service.IPFSService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * Controller for Medical Records.
 * Manages patient files stored securely on IPFS with metadata in MySQL database.
 */
@RestController
@RequestMapping("/api/medical-records")
@CrossOrigin(origins = "*") // Allows and prevents CORS blocks for front-end integration
@Slf4j
public class MedicalRecordController {

    @Autowired
    private MedicalRecordService recordService;

    @Autowired
    private IPFSService ipfsService;

    /**
     * Upload a clinical record to IPFS. Returns the CID and metadata.
     */
    @PostMapping("/upload")
    public ResponseEntity<MedicalRecord> uploadClinicalRecord(
            @RequestParam("patientId") String patientId,
            @RequestParam("description") String description,
            @RequestParam("file") MultipartFile file) {
        
        try {
            MedicalRecord savedRecord = recordService.processAndStoreRecord(patientId, description, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRecord);
        } catch (Exception e) {
            log.error("Failed clinical record upload flow: {}", e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get all medical records indexed for a single patient.
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<MedicalRecord>> listByPatient(@PathVariable String patientId) {
        return ResponseEntity.ok(recordService.getPatientRecords(patientId));
    }

    /**
     * Stream the raw file back from IPFS via its CID hash.
     * This endpoint hides the local gateway URL from the client frontend for security.
     */
    @GetMapping("/stream/{cid}")
    public ResponseEntity<byte[]> streamFromIpfs(@PathVariable String cid) {
        try {
            ResponseEntity<byte[]> response = ipfsService.downloadFromIpfs(cid);
            
            // Set headers based on original or default download strategy
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(response.getHeaders().getContentType());
            headers.setCacheControl(CacheControl.noCache().getHeaderValue());
            
            return new ResponseEntity<>(response.getBody(), headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error retrieving CID {} from IPFS decentralised storage.", cid);
            return ResponseEntity.notFound().build();
        }
    }
}
