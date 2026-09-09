package com.hyguettelabs.medibridge.service;

import com.hyguettelabs.medibridge.domain.entity.ConsentRecord;
import com.hyguettelabs.medibridge.domain.entity.UserAccount;
import com.hyguettelabs.medibridge.domain.enums.ConsentStatus;
import com.hyguettelabs.medibridge.domain.enums.NotificationType;
import com.hyguettelabs.medibridge.domain.enums.Role;
import com.hyguettelabs.medibridge.repository.ConsentRecordRepository;
import com.hyguettelabs.medibridge.repository.UserAccountRepository;
import com.hyguettelabs.medibridge.web.dto.CoordinationDtos;
import com.hyguettelabs.medibridge.web.error.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ConsentService {

    private final ConsentRecordRepository consents;
    private final UserAccountRepository users;
    private final PrivacyGuard privacyGuard;
    private final AccessAuditService audit;
    private final NotificationService notifications;

    public ConsentService(ConsentRecordRepository consents,
                          UserAccountRepository users,
                          PrivacyGuard privacyGuard,
                          AccessAuditService audit,
                          NotificationService notifications) {
        this.consents = consents;
        this.users = users;
        this.privacyGuard = privacyGuard;
        this.audit = audit;
        this.notifications = notifications;
    }

    public boolean hasActive(UUID patientId, UUID clinicianId) {
        return consents.findByPatientIdAndClinicianId(patientId, clinicianId)
                .map(ConsentRecord::isActive)
                .orElse(false);
    }

    @Transactional
    public CoordinationDtos.ConsentResponse grant(UserAccount actor, CoordinationDtos.ConsentGrantRequest request) {
        privacyGuard.assertPatient(actor);
        UserAccount clinician = users.findById(request.clinicianId())
                .orElseThrow(() -> ApiException.notFound("Clinician not found"));
        if (clinician.getRole() != Role.CLINICIAN) {
            throw ApiException.badRequest("Consent can only be granted to a clinician");
        }
        ConsentRecord record = consents.findByPatientIdAndClinicianId(actor.getId(), clinician.getId())
                .orElseGet(() -> {
                    ConsentRecord created = new ConsentRecord();
                    created.setPatient(actor);
                    created.setClinician(clinician);
                    return created;
                });
        record.setStatus(ConsentStatus.GRANTED);
        record.setGrantedAt(Instant.now());
        record.setRevokedAt(null);
        consents.save(record);
        audit.record(actor, actor.getId(), "CONSENT_GRANT", "ConsentRecord", record.getId().toString(),
                "granted to " + clinician.getEmail());
        notifications.push(clinician, NotificationType.CONSENT, "Consent granted",
                actor.displayName() + " granted access to their coordination record.");
        return toDto(record);
    }

    @Transactional
    public CoordinationDtos.ConsentResponse revoke(UserAccount actor, UUID consentId) {
        privacyGuard.assertPatient(actor);
        ConsentRecord record = consents.findById(consentId)
                .orElseThrow(() -> ApiException.notFound("Consent record not found"));
        if (!record.getPatient().getId().equals(actor.getId())) {
            throw ApiException.forbidden("You can only revoke your own consent");
        }
        record.setStatus(ConsentStatus.REVOKED);
        record.setRevokedAt(Instant.now());
        consents.save(record);
        audit.record(actor, actor.getId(), "CONSENT_REVOKE", "ConsentRecord", record.getId().toString(),
                "revoked from " + record.getClinician().getEmail());
        notifications.push(record.getClinician(), NotificationType.CONSENT, "Consent revoked",
                actor.displayName() + " revoked access to their coordination record.");
        return toDto(record);
    }

    @Transactional(readOnly = true)
    public List<CoordinationDtos.ConsentResponse> mine(UserAccount actor) {
        if (actor.getRole() == Role.PATIENT) {
            return consents.findByPatientIdOrderByGrantedAtDesc(actor.getId()).stream().map(this::toDto).toList();
        }
        if (actor.getRole() == Role.CLINICIAN) {
            return consents.findByClinicianIdOrderByGrantedAtDesc(actor.getId()).stream().map(this::toDto).toList();
        }
        return consents.findAll().stream().map(this::toDto).toList();
    }

    private CoordinationDtos.ConsentResponse toDto(ConsentRecord c) {
        return new CoordinationDtos.ConsentResponse(
                c.getId(),
                c.getPatient().getId(),
                c.getPatient().displayName(),
                c.getClinician().getId(),
                c.getClinician().displayName(),
                c.getStatus(),
                c.getGrantedAt(),
                c.getRevokedAt()
        );
    }
}
