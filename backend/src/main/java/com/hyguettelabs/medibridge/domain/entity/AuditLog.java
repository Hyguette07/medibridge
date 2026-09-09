package com.hyguettelabs.medibridge.domain.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs", indexes = @Index(name = "audit_subject_idx", columnList = "subject_patient_id, created_at"))
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "actor_email", length = 255)
    private String actorEmail;

    @Column(name = "actor_role", length = 32)
    private String actorRole;

    @Column(nullable = false, length = 80)
    private String action;

    @Column(name = "entity_type", length = 80)
    private String entityType;

    @Column(name = "entity_id", length = 64)
    private String entityId;

    @Column(name = "subject_patient_id")
    private UUID subjectPatientId;

    @Column(length = 500)
    private String detail;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public static AuditLog of(String actorEmail, String actorRole, String action,
                              String entityType, String entityId, UUID subjectPatientId, String detail) {
        AuditLog log = new AuditLog();
        log.actorEmail = actorEmail;
        log.actorRole = actorRole;
        log.action = action;
        log.entityType = entityType;
        log.entityId = entityId;
        log.subjectPatientId = subjectPatientId;
        log.detail = detail;
        return log;
    }

    public UUID getId() { return id; }
    public String getActorEmail() { return actorEmail; }
    public String getActorRole() { return actorRole; }
    public String getAction() { return action; }
    public String getEntityType() { return entityType; }
    public String getEntityId() { return entityId; }
    public UUID getSubjectPatientId() { return subjectPatientId; }
    public String getDetail() { return detail; }
    public Instant getCreatedAt() { return createdAt; }
}
