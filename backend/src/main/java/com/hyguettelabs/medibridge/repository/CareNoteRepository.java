package com.hyguettelabs.medibridge.repository;

import com.hyguettelabs.medibridge.domain.entity.CareNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CareNoteRepository extends JpaRepository<CareNote, UUID> {
    List<CareNote> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}
