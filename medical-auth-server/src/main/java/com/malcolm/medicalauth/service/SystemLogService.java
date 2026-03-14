package com.malcolm.medicalauth.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SystemLogService {
    private static final Logger logger = LoggerFactory.getLogger(SystemLogService.class);

    public void log(String user, String action, String description, String status) {
        logger.info("[AUTH-LOG] User: {}, Action: {}, Status: {}, Description: {}", user, action, status, description);
    }
}
