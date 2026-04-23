package com.malcolm.medicaliot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
@Slf4j
public class WatermarkService {

    /**
     * Embeds a 64-bit watermark (derived from doctorId) into the image.
     * Uses LSB (Least Significant Bit) steganography on the Blue channel.
     */
    public byte[] embedWatermark(byte[] imageBytes, String doctorId) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (image == null) {
            throw new IOException("Invalid image data");
        }

        String fingerprint = generate64BitFingerprint(doctorId);
        int width = image.getWidth();
        int height = image.getHeight();
        int totalPixels = width * height;

        if (totalPixels < 64) {
            throw new IOException("Image too small for 64-bit watermark");
        }

        // Spread 64 bits evenly across the image
        int interval = totalPixels / 64;

        for (int i = 0; i < 64; i++) {
            int pixelIndex = i * interval;
            int x = pixelIndex % width;
            int y = pixelIndex / width;

            int rgb = image.getRGB(x, y);
            int bit = fingerprint.charAt(i) == '1' ? 1 : 0;

            // Clear the LSB of the Blue channel and set it to the watermark bit
            // Blue is the last 8 bits: 0x000000FF
            int newRgb = (rgb & 0xFFFFFFFE) | bit; 
            // Note: Standard getRGB returns ARGB. The LSB of the entire int is the LSB of Blue.
            
            image.setRGB(x, y, newRgb);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos); // PNG is lossless, crucial for LSB
        return baos.toByteArray();
    }

    /**
     * Decodes the 64-bit watermark from the image.
     */
    public String decodeWatermark(byte[] imageBytes) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (image == null) {
            throw new IOException("Invalid image data");
        }

        int width = image.getWidth();
        int height = image.getHeight();
        int totalPixels = width * height;
        int interval = totalPixels / 64;

        StringBuilder fingerprint = new StringBuilder();

        for (int i = 0; i < 64; i++) {
            int pixelIndex = i * interval;
            int x = pixelIndex % width;
            int y = pixelIndex / width;

            int rgb = image.getRGB(x, y);
            int bit = rgb & 1; // Extract the LSB
            fingerprint.append(bit);
        }

        return fingerprint.toString();
    }

    /**
     * Verifies if the image contains the watermark for the specific doctor.
     */
    public boolean verifyWatermark(byte[] imageBytes, String doctorId) throws IOException {
        String expectedFingerprint = generate64BitFingerprint(doctorId);
        String actualFingerprint = decodeWatermark(imageBytes);
        return expectedFingerprint.equals(actualFingerprint);
    }

    /**
     * Generates a stable 64-bit binary string from a doctorId using SHA-256.
     */
    private String generate64BitFingerprint(String doctorId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(doctorId.getBytes(StandardCharsets.UTF_8));
            
            // Take the first 8 bytes (64 bits)
            StringBuilder binary = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                String b = Integer.toBinaryString(hash[i] & 0xFF);
                while (b.length() < 8) {
                    b = "0" + b;
                }
                binary.append(b);
            }
            return binary.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 not found, falling back to simple hash");
            // Fallback to a simple 64-bit pattern if SHA-256 fails (shouldn't happen)
            long longHash = doctorId.hashCode();
            String b = Long.toBinaryString(longHash);
            while (b.length() < 64) b = "0" + b;
            return b;
        }
    }
}
