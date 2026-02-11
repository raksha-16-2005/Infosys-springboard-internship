package com.example.demo.service;

import com.example.demo.model.AuditLog;
import com.example.demo.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String actor, String action, String details) {
        AuditLog log = new AuditLog();
        log.setActor(actor != null ? actor : "SYSTEM");
        log.setAction(action);
        log.setDetails(details);
        auditLogRepository.save(log);
    }
}

