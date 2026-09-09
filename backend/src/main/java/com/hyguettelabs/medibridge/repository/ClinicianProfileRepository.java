package com.hyguettelabs.medibridge.repository;

import com.hyguettelabs.medibridge.domain.entity.ClinicianProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClinicianProfileRepository extends JpaRepository<ClinicianProfile, UUID> {
    Optional<ClinicianProfile> findByUserId(UUID userId);
    List<ClinicianProfile> findAllByOrderBySpecialtyAsc();
}
