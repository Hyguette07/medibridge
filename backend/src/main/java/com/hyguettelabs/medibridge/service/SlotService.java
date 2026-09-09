package com.hyguettelabs.medibridge.service;

import com.hyguettelabs.medibridge.domain.entity.AvailabilitySlot;
import com.hyguettelabs.medibridge.domain.entity.Facility;
import com.hyguettelabs.medibridge.domain.entity.UserAccount;
import com.hyguettelabs.medibridge.repository.AvailabilitySlotRepository;
import com.hyguettelabs.medibridge.repository.FacilityRepository;
import com.hyguettelabs.medibridge.web.dto.CoordinationDtos;
import com.hyguettelabs.medibridge.web.error.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class SlotService {

    private final AvailabilitySlotRepository slots;
    private final FacilityRepository facilities;
    private final PrivacyGuard privacyGuard;
    private final AccessAuditService audit;

    public SlotService(AvailabilitySlotRepository slots,
                       FacilityRepository facilities,
                       PrivacyGuard privacyGuard,
                       AccessAuditService audit) {
        this.slots = slots;
        this.facilities = facilities;
        this.privacyGuard = privacyGuard;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<CoordinationDtos.SlotResponse> list(UUID clinicianId, UUID facilityId, boolean openOnly) {
        Instant from = Instant.now().minus(1, ChronoUnit.HOURS);
        Instant to = Instant.now().plus(60, ChronoUnit.DAYS);
        List<AvailabilitySlot> found;
        if (clinicianId != null) {
            found = slots.findByClinicianIdAndStartsAtGreaterThanEqualOrderByStartsAtAsc(clinicianId, from);
        } else if (facilityId != null) {
            found = slots.findByFacilityIdAndStartsAtGreaterThanEqualOrderByStartsAtAsc(facilityId, from);
        } else {
            found = slots.findByBookedFalseAndStartsAtGreaterThanEqualAndStartsAtLessThanEqualOrderByStartsAtAsc(from, to);
        }
        return found.stream()
                .filter(s -> !openOnly || !s.isBooked())
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public CoordinationDtos.SlotResponse create(UserAccount actor, CoordinationDtos.SlotRequest request) {
        privacyGuard.assertClinician(actor);
        if (!request.endsAt().isAfter(request.startsAt())) {
            throw ApiException.badRequest("Slot end must be after start");
        }
        Facility facility = facilities.findById(request.facilityId())
                .orElseThrow(() -> ApiException.notFound("Facility not found"));
        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setClinician(actor);
        slot.setFacility(facility);
        slot.setStartsAt(request.startsAt());
        slot.setEndsAt(request.endsAt());
        slots.save(slot);
        audit.record(actor, null, "CREATE_SLOT", "AvailabilitySlot", slot.getId().toString(), facility.getName());
        return toDto(slot);
    }

    @Transactional
    public void delete(UserAccount actor, UUID id) {
        privacyGuard.assertClinician(actor);
        AvailabilitySlot slot = slots.findById(id).orElseThrow(() -> ApiException.notFound("Slot not found"));
        if (!slot.getClinician().getId().equals(actor.getId())) {
            throw ApiException.forbidden("You can only remove your own availability");
        }
        if (slot.isBooked()) {
            throw ApiException.conflict("A booked slot cannot be deleted; cancel the appointment instead");
        }
        slots.delete(slot);
        audit.record(actor, null, "DELETE_SLOT", "AvailabilitySlot", id.toString(), null);
    }

    CoordinationDtos.SlotResponse toDto(AvailabilitySlot s) {
        return new CoordinationDtos.SlotResponse(
                s.getId(),
                s.getClinician().getId(),
                s.getClinician().displayName(),
                s.getFacility().getId(),
                s.getFacility().getName(),
                s.getStartsAt(),
                s.getEndsAt(),
                s.isBooked()
        );
    }
}
