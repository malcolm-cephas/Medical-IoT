package com.malcolm.medicaliot.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import com.malcolm.medicaliot.service.WatermarkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

/**
 * WatermarkController
 *
 * Provides endpoints for:
 * 1. Decoding a suspicious leaked image to find the hidden doctor ID
 * 2. Verifying whether a specific doctor's ID is in a given image
 * 3. Health check to confirm the feature is active
 *
 * These are ADMIN-only investigation tools for post-breach traceability.
 * New endpoints only — does not change any existing endpoint.
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
     *
     * Upload any suspicious image here.
     * The system extracts the hidden 64-bit doctor ID from it.
     * Used by admin to investigate: "whose copy of this image was leaked?"
     *
     * How to use in Postman:
     * POST http://localhost:8080/api/watermark/decode
     * Body → form-data → key: file, value: (select your image file)
     */
    @PostMapping("/decode")
    public ResponseEntity<?> decodeImage(@RequestParam("file") MultipartFile file) {
        try {
            byte[] imageBytes = file.getBytes();
            String extractedBits = watermarkService.decodeWatermark(imageBytes);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "extracted_bits", extractedBits,
                    "bit_length", extractedBits.length(),
                    "message", "These 64 bits are the invisible fingerprint of the doctor who received this image",
                    "paper_concept", "Traceability — Wenying Wen et al., IEEE TCSVT 2026"));

        } catch (Exception e) {
            log.error("WATERMARK DECODE ERROR: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }

    /**
     * POST /api/watermark/verify
     *
     * Upload an image and provide a doctor ID.
     * Returns true if this image was watermarked for that specific doctor.
     * Used to confirm "was this leaked image sent to doctor X?"
     *
     * How to use in Postman:
     * POST http://localhost:8080/api/watermark/verify
     * Body → form-data
     * key: file value: (select image)
     * key: doctorId value: doctor123
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("doctorId") String doctorId) {
        try {
            byte[] imageBytes = file.getBytes();
            boolean isMatch = watermarkService.verifyWatermark(imageBytes, doctorId);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "doctor_id", doctorId,
                    "match", isMatch,
                    "message", isMatch
                            ? "CONFIRMED: This image was watermarked for doctor '" + doctorId + "'"
                            : "NOT MATCHED: This image was not watermarked for doctor '" + doctorId + "'"));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/watermark/health
     * Simple check to confirm the watermark feature is loaded and running.
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "ACTIVE",
                "feature", "Invisible Medical Image Watermarking",
                "paper", "Building an Invisible Shield — Wenying Wen et al., IEEE TCSVT 2026",
                "goals", new String[] { "Privacy Protection", "Traceability", "Invisibility" },
                "method", "LSB Steganography — blue channel, 64-bit doctor ID",
                "no_new_dependencies_required", true));
    }
    @PostMapping("/embed")
public ResponseEntity<byte[]> embedImage(
        @RequestParam("file") MultipartFile file,
        @RequestParam("doctorId") String doctorId) {
    try {
        byte[] watermarked = watermarkService.embedWatermark(file.getBytes(), doctorId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        return new ResponseEntity<>(watermarked, headers, HttpStatus.OK);
    } catch (Exception e) {
        return ResponseEntity.status(500).build();
    }
}
}