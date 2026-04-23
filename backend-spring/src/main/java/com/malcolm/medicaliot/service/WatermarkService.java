package com.malcolm.medicaliot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * WatermarkService
 *
 * Implements robust, invisible digital watermarking for medical images.
 * Uses LSB (Least Significant Bit) steganography on the Blue channel.
 *
 * Security Features:
 * 1. Fingerprinting: Converts doctor ID into a stable 64-bit sequence.
 * 2. Normalization: Forces TYPE_INT_RGB to prevent watermark loss from transparency.
 * 3. Lossless Storage: Uses PNG for internal processing to avoid compression artifacts.
 */
@Service
@Slf4j
public class WatermarkService {

    /**
     * Embeds a 64-bit invisible watermark into an image.
     */
    public byte[] embedWatermark(byte[] imageBytes, String doctorId) throws IOException {
        try {
            BufferedImage original = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (original == null) {
                throw new IOException("Could not read image data");
            }

            // Normalization: Ensure consistent RGB format (fixes transparency issues)
            BufferedImage image = new BufferedImage(
                original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.drawImage(original, 0, 0, null);
            g.dispose();

            String fingerprint = generate64BitFingerprint(doctorId);
            int width = image.getWidth();
            int height = image.getHeight();
            int totalPixels = width * height;

            if (totalPixels < 64) {
                throw new IOException("Image too small for forensic watermarking");
            }

            // Spread 64 bits evenly across the image for robustness
            int step = totalPixels / 64;

            for (int i = 0; i < 64; i++) {
                int pixelIndex = i * step;
                int x = pixelIndex % width;
                int y = pixelIndex / width;

                int rgb = image.getRGB(x, y);
                int red   = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8)  & 0xFF;
                int blue  =  rgb        & 0xFF;

                // Embed the bit into the Least Significant Bit of the Blue channel
                int bit = fingerprint.charAt(i) == '1' ? 1 : 0;
                blue = (blue & 0xFE) | bit;

                image.setRGB(x, y, (red << 16) | (green << 8) | blue);
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", output); // PNG is required for lossless LSB
            
            log.info("WATERMARK: Embedded forensic ID for user '{}'", doctorId);
            return output.toByteArray();

        } catch (Exception e) {
            log.error("WATERMARK: Embedding failed - {}", e.getMessage());
            return imageBytes; // Fallback to original image on error
        }
    }

    /**
     * Decodes the 64-bit watermark from an image.
     */
    public String decodeWatermark(byte[] imageBytes) throws IOException {
        try {
            BufferedImage original = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (original == null) return "ERROR: Invalid image";

            // Normalization: Must match the format used during embedding
            BufferedImage image = new BufferedImage(
                original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.drawImage(original, 0, 0, null);
            g.dispose();

            int width = image.getWidth();
            int totalPixels = width * image.getHeight();
            int step = totalPixels / 64;

            StringBuilder bits = new StringBuilder();
            for (int i = 0; i < 64; i++) {
                int pixelIndex = i * step;
                int x = pixelIndex % width;
                int y = pixelIndex / width;

                int rgb = image.getRGB(x, y);
                int blue = rgb & 0xFF;
                bits.append(blue & 1); // Extract LSB
            }

            return bits.toString();
        } catch (Exception e) {
            log.error("WATERMARK: Decoding failed - {}", e.getMessage());
            return "ERROR";
        }
    }

    /**
     * Verifies if a specific doctor ID is embedded in the image.
     */
    public boolean verifyWatermark(byte[] imageBytes, String doctorId) throws IOException {
        String expectedFingerprint = generate64BitFingerprint(doctorId);
        String actualFingerprint = decodeWatermark(imageBytes);
        return expectedFingerprint.equals(actualFingerprint);
    }

    /**
     * Generates a stable 64-bit binary string from a doctorId using SHA-256.
     * This provides a consistent "fingerprint" for the same user across different images.
     */
    private String generate64BitFingerprint(String doctorId) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(doctorId.getBytes(StandardCharsets.UTF_8));
            
            // Extract first 64 bits (8 bytes)
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
            log.error("SHA-256 not found, falling back to basic hash");
            long longHash = doctorId.hashCode();
            String b = Long.toBinaryString(longHash);
            while (b.length() < 64) b = "0" + b;
            return b;
        }
    }
}
