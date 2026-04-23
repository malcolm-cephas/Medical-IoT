package com.malcolm.medicaliot.controller;

import com.malcolm.medicaliot.service.WatermarkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/watermark")
@CrossOrigin(origins = "*")
@Slf4j
public class WatermarkController {

    @Autowired
    private WatermarkService watermarkService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "ACTIVE",
            "feature", "Invisible Medical Image Watermarking",
            "paper", "Building an Invisible Shield — Wenying Wen et al., IEEE TCSVT 2026"
        ));
    }

    @PostMapping("/embed")
    public ResponseEntity<byte[]> embed(
            @RequestParam("file") MultipartFile file,
            @RequestParam("doctorId") String doctorId) {
        try {
            byte[] watermarkedImage = watermarkService.embedWatermark(file.getBytes(), doctorId);
            return ResponseEntity.ok()
                    .contentType(java.util.Objects.requireNonNull(MediaType.IMAGE_PNG))
                    .body(watermarkedImage);
        } catch (Exception e) {
            log.error("Embedding failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verify(
            @RequestParam("file") MultipartFile file,
            @RequestParam("doctorId") String doctorId) {
        try {
            boolean match = watermarkService.verifyWatermark(file.getBytes(), doctorId);
            return ResponseEntity.ok(Map.of(
                "match", match,
                "message", match ? "CONFIRMED: This image was watermarked for doctor " + doctorId 
                                 : "NOT MATCHED: This image was not watermarked for doctor " + doctorId
            ));
        } catch (Exception e) {
            log.error("Verification failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/decode")
    public ResponseEntity<Map<String, Object>> decode(@RequestParam("file") MultipartFile file) {
        try {
            String bits = watermarkService.decodeWatermark(file.getBytes());
            return ResponseEntity.ok(Map.of(
                "extracted_bits", bits,
                "bit_length", bits.length(),
                "message", "These 64 bits are the invisible fingerprint of the doctor who received this image"
            ));
        } catch (Exception e) {
            log.error("Decoding failed", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
