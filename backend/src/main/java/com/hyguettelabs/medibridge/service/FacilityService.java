package com.hyguettelabs.medibridge.service;

import com.hyguettelabs.medibridge.domain.entity.Facility;
import com.hyguettelabs.medibridge.domain.entity.UserAccount;
import com.hyguettelabs.medibridge.repository.FacilityRepository;
import com.hyguettelabs.medibridge.web.dto.CoordinationDtos;
import com.hyguettelabs.medibridge.web.error.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class FacilityService {

    private final FacilityRepository facilities;
    private final PrivacyGuard privacyGuard;
    private final AccessAuditService audit;

    public FacilityService(FacilityRepository facilities, PrivacyGuard privacyGuard, AccessAuditService audit) {
        this.facilities = facilities;
        this.privacyGuard = privacyGuard;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public List<CoordinationDtos.FacilityResponse> list() {
        return facilities.findByActiveTrueOrderByNameAsc().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<CoordinationDtos.FacilityResponse> listAll(UserAccount actor) {
        privacyGuard.assertAdmin(actor);
        return facilities.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    public CoordinationDtos.FacilityResponse create(UserAccount actor, CoordinationDtos.FacilityRequest request) {
        privacyGuard.assertAdmin(actor);
        Facility facility = apply(new Facility(), request);
        facilities.save(facility);
        audit.record(actor, null, "CREATE_FACILITY", "Facility", facility.getId().toString(), facility.getName());
        return toDto(facility);
    }

    @Transactional
    public CoordinationDtos.FacilityResponse update(UserAccount actor, UUID id, CoordinationDtos.FacilityRequest request) {
        privacyGuard.assertAdmin(actor);
        Facility facility = facilities.findById(id).orElseThrow(() -> ApiException.notFound("Facility not found"));
        apply(facility, request);
        facilities.save(facility);
        audit.record(actor, null, "UPDATE_FACILITY", "Facility", id.toString(), facility.getName());
        return toDto(facility);
    }

    private Facility apply(Facility facility, CoordinationDtos.FacilityRequest request) {
        facility.setName(request.name().trim());
        facility.setType(request.type());
        facility.setAddress(request.address());
        facility.setCity(request.city());
        facility.setPhone(request.phone());
        facility.setActive(request.active() == null || request.active());
        return facility;
    }

    private CoordinationDtos.FacilityResponse toDto(Facility f) {
        return new CoordinationDtos.FacilityResponse(
                f.getId(), f.getName(), f.getType(), f.getAddress(), f.getCity(), f.getPhone(), f.isActive());
    }
}
