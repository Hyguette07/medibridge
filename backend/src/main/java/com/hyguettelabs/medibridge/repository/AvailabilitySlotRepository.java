package com.hyguettelabs.medibridge.repository;

import com.hyguettelabs.medibridge.domain.entity.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, UUID> {
    List<AvailabilitySlot> findByClinicianIdAndStartsAtGreaterThanEqualOrderByStartsAtAsc(UUID clinicianId, Instant from);
    List<AvailabilitySlot> findByBookedFalseAndStartsAtGreaterThanEqualAndStartsAtLessThanEqualOrderByStartsAtAsc(Instant from, Instant to);
    List<AvailabilitySlot> findByFacilityIdAndStartsAtGreaterThanEqualOrderByStartsAtAsc(UUID facilityId, Instant from);
    long countByClinicianIdAndBookedFalseAndStartsAtGreaterThanEqual(UUID clinicianId, Instant from);
}
