package com.malcolm.medicaliot.audit;

import com.malcolm.medicaliot.event.SecurityAlertEvent;
import com.malcolm.medicaliot.model.SecurityEvent;
import com.malcolm.medicaliot.repository.SecurityRepository;
import com.malcolm.medicaliot.service.LockdownService;
import com.malcolm.medicaliot.service.BlockchainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class SecurityEventListener {

    @Autowired
    private SecurityRepository securityRepository;

    @Autowired
    private LockdownService lockdownService;

    @Autowired
    private BlockchainService blockchainService;

    @Async
    @EventListener
    public void handleSecurityAlert(SecurityAlertEvent event) {
        // 1. Persist to Relational DB
        SecurityEvent entity = new SecurityEvent();
        entity.setEventType(event.getEventType());
        entity.setSeverity(event.getSeverity());
        entity.setDescription(event.getDescription());
        entity.setTriggeredByIp("SYSTEM_INTERNAL"); // Or pass IP in event
        securityRepository.save(entity);

        // 2. Log to Immutable Blockchain
        // Transaction: "User X triggered Event Y"
        blockchainService.logTransaction(
                event.getUsername() != null ? event.getUsername() : "SYSTEM",
                "EVENT:" + event.getEventType(),
                event.getDescription());

        // 3. Automated Response (Intrusion Detection)
        if ("CRITICAL".equals(event.getSeverity()) || "INTRUSION_DETECTED".equals(event.getEventType())) {
            lockdownService.enableLockdown(
                    "Automatic Lockdown Triggered by: " + event.getEventType(),
                    "SYSTEM_EVENT_LISTENER");
        }

        System.out.println("AUDIT LOG: [" + event.getSeverity() + "] " + event.getDescription());
    }
}
