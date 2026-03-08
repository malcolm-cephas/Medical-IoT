package com.malcolm.medicaliot.controller;

import com.malcolm.medicaliot.service.TwoFactorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/2fa")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TwoFactorController {

    private final TwoFactorService twoFactorService;

    @PostMapping("/request")
    public ResponseEntity<Map<String, String>> requestCode(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        if (username == null)
            return ResponseEntity.badRequest().build();

        String code = twoFactorService.generateCode(username);
        return ResponseEntity.ok(Map.of("message", "Code generated successfully. Check logs.", "code_hint", code));
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Boolean>> verifyCode(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String code = request.get("code");

        boolean isValid = twoFactorService.verifyCode(username, code);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }
}
