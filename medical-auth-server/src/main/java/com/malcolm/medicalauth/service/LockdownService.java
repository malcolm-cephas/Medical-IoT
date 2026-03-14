package com.malcolm.medicalauth.service;

import com.malcolm.medicalauth.model.SecurityEvent;
import com.malcolm.medicalauth.repository.SecurityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LockdownService {
    
    @Autowired
    private SecurityRepository securityRepository;
    
    @Autowired
    private SystemLogService logService;

    private boolean isLockdownActive = false;
    private String lockdownReason = "";
    private final Map<String, Integer> failedLoginAttempts = new ConcurrentHashMap<>();
    private static final int MAX_ATTEMPTS = 5;

    public boolean isLockdown() { return isLockdownActive; }
    public String getReason() { return lockdownReason; }

    public synchronized void enableLockdown(String reason, String ip) {
        if (!isLockdownActive) {
            this.isLockdownActive = true;
            this.lockdownReason = reason;
            logEvent("LOCKDOWN_ENABLED", "CRITICAL", "System Lockdown Initiated: " + reason, ip);
        }
    }

    public synchronized void disableLockdown(String ip) {
        if (isLockdownActive) {
            this.isLockdownActive = false;
            this.lockdownReason = "";
            logEvent("LOCKDOWN_DISABLED", "HIGH", "System Lockdown Lifted", ip);
        }
    }

    public void recordFailedLogin(String username, String ip) {
        int attempts = failedLoginAttempts.getOrDefault(username, 0) + 1;
        failedLoginAttempts.put(username, attempts);
        
        logEvent("FAILED_LOGIN", "WARN", "Failed login for: " + username + " (Attempt #" + attempts + ")", ip);
        
        if (attempts >= MAX_ATTEMPTS) {
            enableLockdown("Brute force detected for user: " + username, ip);
            failedLoginAttempts.remove(username);
        }
    }

    public void resetFailedLogin(String username) {
        failedLoginAttempts.remove(username);
    }

    private void logEvent(String type, String severity, String description, String ip) {
        SecurityEvent event = new SecurityEvent();
        event.setEventType(type);
        event.setSeverity(severity);
        event.setDescription(description);
        event.setTriggeredByIp(ip);
        securityRepository.save(event);
        logService.log("SYSTEM", type, description, severity.equals("CRITICAL") ? "FAILURE" : "INFO");
    }
}
