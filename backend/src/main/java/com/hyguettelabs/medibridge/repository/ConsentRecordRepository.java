package com.hyguettelabs.medibridge.repository;

import com.hyguettelabs.medibridge.domain.entity.ConsentRecord;
import com.hyguettelabs.medibridge.domain.enums.ConsentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConsentRecordRepository extends JpaRepository<ConsentRecord, UUID> {
    Optional<ConsentRecord> findByPatientIdAndClinicianId(UUID patientId, UUID clinicianId);
    List<ConsentRecord> findByPatientIdOrderByGrantedAtDesc(UUID patientId);
    List<ConsentRecord> findByClinicianIdOrderByGrantedAtDesc(UUID clinicianId);
    long countByClinicianIdAndStatus(UUID clinicianId, ConsentStatus status);
    long countByStatus(ConsentStatus status);
}
