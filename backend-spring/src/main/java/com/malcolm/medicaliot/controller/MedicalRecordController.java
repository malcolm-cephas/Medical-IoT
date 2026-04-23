package com.malcolm.medicaliot.controller;

import com.malcolm.medicaliot.model.MedicalRecord;
import com.malcolm.medicaliot.service.MedicalRecordService;
import com.malcolm.medicaliot.service.IPFSService;
import com.malcolm.medicaliot.service.WatermarkService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/**
 * Controller for Medical Records.
 * Manages patient files stored securely on IPFS with metadata in MySQL database.
 * Includes automated invisible watermarking for traceability.
 */
@RestController
@RequestMapping("/api/medical-records")
@CrossOrigin(origins = "*")
@Slf4j
public class MedicalRecordController {

    @Autowired
    private MedicalRecordService recordService;

    @Autowired
    private IPFSService ipfsService;

    @Autowired
    private WatermarkService watermarkService;

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
     * Automatically embeds an invisible watermark of the requesting doctor's ID for traceability.
     */
    @GetMapping("/stream/{cid}")
    public ResponseEntity<byte[]> streamFromIpfs(@PathVariable String cid, HttpServletRequest request) {
        try {
            ResponseEntity<byte[]> response = ipfsService.downloadFromIpfs(cid);
            byte[] imageBytes = response.getBody();

            // ── WATERMARK FEATURE ──
            // If the requester has an X-User-Id, we embed it invisibly into the blue channel LSB.
            String doctorId = request.getHeader("X-User-Id");
            if (doctorId != null && !doctorId.isEmpty() && imageBytes != null) {
                try {
                    imageBytes = watermarkService.embedWatermark(imageBytes, doctorId);
                    log.info("Invisible watermark embedded for user: {}", doctorId);
                } catch (Exception e) {
                    log.error("Watermark embedding failed: {}", e.getMessage());
                    // Fallback: return un-watermarked image if embedding fails
                }
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(response.getHeaders().getContentType());
            headers.setCacheControl(CacheControl.noCache().getHeaderValue());
            
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error retrieving CID {} from IPFS decentralised storage.", cid);
            return ResponseEntity.notFound().build();
        }
    }
}
