package com.malcolm.medicaliot.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;

@Service
public class TwoFactorService {
    private final ConcurrentHashMap<String, String> codes = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public String generateCode(String username) {
        String code = String.format("%06d", random.nextInt(1000000));
        codes.put(username, code);
        // In a real system, you'd send this via SMS/Email
        System.out.println("2FA Code for " + username + ": " + code);
        return code;
    }

    public boolean verifyCode(String username, String code) {
        String savedCode = codes.get(username);
        if (savedCode != null && savedCode.equals(code)) {
            codes.remove(username);
            return true;
        }
        return false;
    }
}
