package com.malcolm.medicaliot.controller;

import com.malcolm.medicaliot.service.WatermarkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import java.util.Objects;

/**
 * WatermarkController
 *
 * Provides endpoints for forensic investigation of medical images:
 * 1. Decode: Extracts the hidden 64-bit doctor ID fingerprint from a suspicious image.
 * 2. Verify: Confirms if a specific image was watermarked for a given doctor.
 * 3. Health: Checks feature status and methodology.
 *
 * These tools are intended for system administrators to trace the source of data leaks.
 */
@RestController
@RequestMapping("/api/watermark")
@CrossOrigin(origins = "*")
@Slf4j
public class WatermarkController {

    @Autowired
    private WatermarkService watermarkService;

    /**
     * POST /api/watermark/decode
     * Extracts the hidden fingerprint from an image.
     */
    @PostMapping("/decode")
    public ResponseEntity<Map<String, Object>> decode(@RequestParam("file") MultipartFile file) {
        try {
            String bits = watermarkService.decodeWatermark(file.getBytes());
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "extracted_bits", bits,
                "bit_length", bits.length(),
                "message", "These 64 bits are the invisible fingerprint of the doctor who received this image",
                "reference", "IEEE TCSVT 2026 - Building an Invisible Shield"
            ));
        } catch (Exception e) {
            log.error("Decoding failed: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    /**
     * POST /api/watermark/verify
     * Verifies if a specific doctor ID is embedded in the image.
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verify(
            @RequestParam("file") MultipartFile file,
            @RequestParam("doctorId") String doctorId) {
        try {
            boolean match = watermarkService.verifyWatermark(file.getBytes(), doctorId);
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "match", match,
                "doctorId", doctorId,
                "message", match ? "CONFIRMED: This image was watermarked for doctor " + doctorId 
                                 : "NOT MATCHED: This image was not watermarked for doctor " + doctorId
            ));
        } catch (Exception e) {
            log.error("Verification failed: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    /**
     * POST /api/watermark/embed
     * Manual embedding tool (primarily for testing purposes).
     */
    @PostMapping("/embed")
    public ResponseEntity<byte[]> embed(
            @RequestParam("file") MultipartFile file,
            @RequestParam("doctorId") String doctorId) {
        try {
            byte[] watermarkedImage = watermarkService.embedWatermark(file.getBytes(), doctorId);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            return new ResponseEntity<>(watermarkedImage, headers, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Manual embedding failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/watermark/health
     * Feature status and metadata.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "ACTIVE",
            "feature", "Invisible Medical Image Watermarking",
            "methodology", "LSB Steganography (Blue Channel)",
            "capacity", "64-bit Stable Fingerprint",
            "goals", new String[] { "Privacy Protection", "Forensic Traceability", "Visual Invisibility" }
        ));
    }
}
