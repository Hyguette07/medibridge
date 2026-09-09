package com.hyguettelabs.medibridge.repository;

import com.hyguettelabs.medibridge.domain.entity.Facility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FacilityRepository extends JpaRepository<Facility, UUID> {
    List<Facility> findByActiveTrueOrderByNameAsc();
}
