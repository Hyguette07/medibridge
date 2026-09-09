package com.hyguettelabs.medibridge.service;

import com.hyguettelabs.medibridge.domain.entity.AuditLog;
import com.hyguettelabs.medibridge.domain.entity.UserAccount;
import com.hyguettelabs.medibridge.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Every PHI-adjacent read or write is recorded. Detail never contains care-note bodies,
 * passwords, or tokens.
 */
@Service
public class AccessAuditService {

    private static final Logger log = LoggerFactory.getLogger(AccessAuditService.class);
    private final AuditLogRepository repository;

    public AccessAuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void record(UserAccount actor, UUID subjectPatientId, String action,
                       String entityType, String entityId, String detail) {
        String email = actor == null ? "system" : actor.getEmail();
        String role = actor == null ? "SYSTEM" : actor.getRole().name();
        repository.save(AuditLog.of(email, role, action, entityType, entityId, subjectPatientId, detail));
        log.info("audit action={} actor={} subject={} entity={}:{}", action, email, subjectPatientId, entityType, entityId);
    }

    @Transactional
    public void record(String actorEmail, String action, String entityType, String entityId, String detail) {
        repository.save(AuditLog.of(actorEmail, null, action, entityType, entityId, null, detail));
        log.info("audit action={} actor={} entity={}:{}", action, actorEmail, entityType, entityId);
    }
}
