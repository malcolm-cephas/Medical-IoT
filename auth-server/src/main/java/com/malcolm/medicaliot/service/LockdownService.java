package com.malcolm.medicaliot.service;

import com.malcolm.medicaliot.model.SecurityEvent;
import com.malcolm.medicaliot.repository.SecurityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LockdownService {

    @Autowired
    private SecurityRepository securityRepository;

    @Autowired
    private SystemLogService logService;

    // In-memory flag for speed (backed by DB log)
    private boolean isLockdownActive = false;
    private String lockdownReason = "";

    public boolean isLockdown() {
        return isLockdownActive;
    }

    public String getReason() {
        return lockdownReason;
    }

    public synchronized void enableLockdown(String reason, String ip) {
        if (!isLockdownActive) {
            this.isLockdownActive = true;
            this.lockdownReason = reason;
            logEvent("LOCKDOWN_ENABLED", "CRITICAL", "System Lockdown Initiated: " + reason, ip);
            System.err.println("!!! SYSTEM ENTERING LOCKDOWN MODE !!! Reason: " + reason);
        }
    }

    public synchronized void disableLockdown(String adminIp) {
        if (isLockdownActive) {
            this.isLockdownActive = false;
            this.lockdownReason = "";
            logEvent("LOCKDOWN_DISABLED", "HIGH", "System Lockdown Lifted by Admin", adminIp);
            System.out.println(">>> System Lockdown Lifted.");
        }
    }

    // Intrusion Detection
    private final java.util.Map<String, Integer> failedLoginAttempts = new java.util.concurrent.ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 100;

    public void recordFailedLogin(String ip) {
        int attempts = failedLoginAttempts.getOrDefault(ip, 0) + 1;
        failedLoginAttempts.put(ip, attempts);

        logEvent("FAILED_LOGIN", "WARN", "Failed Login Attempt #" + attempts, ip);

        if (attempts >= MAX_ATTEMPTS) {
            enableLockdown("INTRUSION DETECTED: Too many failed logins from IP " + ip, ip);
            failedLoginAttempts.remove(ip); // Reset after locking to avoid spam
        }
    }

    public void resetFailedLogin(String ip) {
        failedLoginAttempts.remove(ip);
    }

    public void logEvent(String type, String severity, String description, String ip) {
        SecurityEvent event = new SecurityEvent();
        event.setEventType(type);
        event.setSeverity(severity);
        event.setDescription(description);
        event.setTriggeredByIp(ip);
        securityRepository.save(event);
        logService.log("SYSTEM", type, description + " [Source: " + ip + "]",
                severity.equals("CRITICAL") || severity.equals("HIGH") ? "FAILURE" : "INFO");
    }
}
