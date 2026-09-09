package com.hyguettelabs.medibridge.repository;

import com.hyguettelabs.medibridge.domain.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findBySubjectPatientIdOrderByCreatedAtDesc(UUID subjectPatientId);
    long countBySubjectPatientId(UUID subjectPatientId);
    List<AuditLog> findTop100ByOrderByCreatedAtDesc();
}
