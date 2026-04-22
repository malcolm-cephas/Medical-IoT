package com.malcolm.medicaliot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Service
@Slf4j
public class WatermarkService {

    public byte[] embedWatermark(byte[] imageBytes, String doctorId) {
        try {
            int[] bits = convertIdToBits(doctorId);

            BufferedImage original = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (original == null) {
                log.warn("WATERMARK: Could not read image — returning original unchanged");
                return imageBytes;
            }

            // Force consistent RGB format — this is the key fix
            BufferedImage image = new BufferedImage(
                original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.drawImage(original, 0, 0, null);
            g.dispose();

            int width = image.getWidth();
            int totalPixels = width * image.getHeight();
            int step = totalPixels / 64;

            for (int i = 0; i < 64; i++) {
                int pixelIndex = i * step;
                int x = pixelIndex % width;
                int y = pixelIndex / width;

                int rgb = image.getRGB(x, y);
                int red   = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8)  & 0xFF;
                int blue  =  rgb        & 0xFF;

                // Embed bit into LSB of blue channel
                blue = (blue & 0xFE) | bits[i];

                image.setRGB(x, y, (red << 16) | (green << 8) | blue);
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", output);

            log.info("WATERMARK: Embedded ID for doctor '{}' — {} bytes",
                    doctorId, output.size());
            return output.toByteArray();

        } catch (Exception e) {
            log.warn("WATERMARK: Embed failed — {}", e.getMessage());
            return imageBytes;
        }
    }

    public String decodeWatermark(byte[] imageBytes) {
        try {
            BufferedImage original = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (original == null) return "ERROR: Could not read image";

            // Force same consistent RGB format as embed
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

                int rgb  = image.getRGB(x, y);
                int blue = rgb & 0xFF;
                bits.append(blue & 1);
            }

            log.info("WATERMARK: Decoded bits: {}", bits);
            return bits.toString();

        } catch (Exception e) {
            log.error("WATERMARK: Decode failed — {}", e.getMessage());
            return "ERROR: " + e.getMessage();
        }
    }

    public boolean verifyWatermark(byte[] imageBytes, String doctorId) {
        String extractedBits = decodeWatermark(imageBytes);
        String expectedBits  = bitsToString(convertIdToBits(doctorId));
        boolean match = extractedBits.equals(expectedBits);
        log.info("WATERMARK: Verify '{}' — match: {}", doctorId, match);
        return match;
    }

    private int[] convertIdToBits(String doctorId) {
        long hash = 0L;
        for (char c : doctorId.toCharArray()) {
            hash = hash * 31L + c;
        }
        hash ^= (hash >>> 32);
        int[] bits = new int[64];
        for (int i = 0; i < 64; i++) {
            bits[63 - i] = (int) ((hash >> i) & 1L);
        }
        return bits;
    }

    private String bitsToString(int[] bits) {
        StringBuilder sb = new StringBuilder();
        for (int bit : bits) sb.append(bit);
        return sb.toString();
    }
}