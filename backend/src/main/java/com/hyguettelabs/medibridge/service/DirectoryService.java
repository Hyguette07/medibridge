package com.hyguettelabs.medibridge.service;

import com.hyguettelabs.medibridge.Disclaimer;
import com.hyguettelabs.medibridge.domain.entity.ClinicianProfile;
import com.hyguettelabs.medibridge.domain.entity.PatientProfile;
import com.hyguettelabs.medibridge.domain.entity.UserAccount;
import com.hyguettelabs.medibridge.domain.enums.AppointmentStatus;
import com.hyguettelabs.medibridge.domain.enums.ConsentStatus;
import com.hyguettelabs.medibridge.domain.enums.NotificationType;
import com.hyguettelabs.medibridge.domain.enums.Role;
import com.hyguettelabs.medibridge.repository.AppointmentRepository;
import com.hyguettelabs.medibridge.repository.AuditLogRepository;
import com.hyguettelabs.medibridge.repository.AvailabilitySlotRepository;
import com.hyguettelabs.medibridge.repository.ClinicianProfileRepository;
import com.hyguettelabs.medibridge.repository.ConsentRecordRepository;
import com.hyguettelabs.medibridge.repository.FacilityRepository;
import com.hyguettelabs.medibridge.repository.PatientProfileRepository;
import com.hyguettelabs.medibridge.repository.UserAccountRepository;
import com.hyguettelabs.medibridge.web.dto.CoordinationDtos;
import com.hyguettelabs.medibridge.web.error.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class DirectoryService {

    private final ClinicianProfileRepository clinicians;
    private final PatientProfileRepository patients;
    private final UserAccountRepository users;
    private final ConsentService consents;
    private final PrivacyGuard privacyGuard;
    private final AccessAuditService audit;
    private final NotificationService notifications;
    private final AuditLogRepository auditLogs;
    private final AppointmentRepository appointments;
    private final AvailabilitySlotRepository slots;
    private final ConsentRecordRepository consentRecords;
    private final FacilityRepository facilities;
    private final AppointmentService appointmentService;

    public DirectoryService(ClinicianProfileRepository clinicians,
                            PatientProfileRepository patients,
                            UserAccountRepository users,
                            ConsentService consents,
                            PrivacyGuard privacyGuard,
                            AccessAuditService audit,
                            NotificationService notifications,
                            AuditLogRepository auditLogs,
                            AppointmentRepository appointments,
                            AvailabilitySlotRepository slots,
                            ConsentRecordRepository consentRecords,
                            FacilityRepository facilities,
                            AppointmentService appointmentService) {
        this.clinicians = clinicians;
        this.patients = patients;
        this.users = users;
        this.consents = consents;
        this.privacyGuard = privacyGuard;
        this.audit = audit;
        this.notifications = notifications;
        this.auditLogs = auditLogs;
        this.appointments = appointments;
        this.slots = slots;
        this.consentRecords = consentRecords;
        this.facilities = facilities;
        this.appointmentService = appointmentService;
    }

    @Transactional(readOnly = true)
    public List<CoordinationDtos.ClinicianResponse> listClinicians() {
        return clinicians.findAllByOrderBySpecialtyAsc().stream().map(this::toClinician).toList();
    }

    @Transactional(readOnly = true)
    public List<CoordinationDtos.PatientSummaryResponse> consentedPatients(UserAccount actor) {
        privacyGuard.assertClinician(actor);
        return consentRecords.findByClinicianIdOrderByGrantedAtDesc(actor.getId()).stream()
                .filter(c -> c.getStatus() == ConsentStatus.GRANTED)
                .map(c -> toPatient(c.getPatient(), true))
                .toList();
    }

    @Transactional(readOnly = true)
    public CoordinationDtos.PatientSummaryResponse getPatient(UserAccount actor, UUID patientId) {
        UserAccount patient = users.findById(patientId).orElseThrow(() -> ApiException.notFound("Patient not found"));
        if (patient.getRole() != Role.PATIENT) {
            throw ApiException.notFound("Patient not found");
        }
        boolean active = consents.hasActive(patientId, actor.getId());
        privacyGuard.assertCanAccessPatientRecord(actor, patient, active);
        audit.record(actor, patientId, "VIEW_PATIENT", "PatientProfile", patientId.toString(), "limited demographics");
        if (actor.getRole() == Role.CLINICIAN) {
            notifications.push(patient, NotificationType.ACCESS, "Record accessed",
                    actor.displayName() + " viewed your coordination profile.");
        }
        return toPatient(patient, active);
    }

    @Transactional(readOnly = true)
    public List<CoordinationDtos.AuditEntryResponse> myAudit(UserAccount actor) {
        privacyGuard.assertPatient(actor);
        audit.record(actor, actor.getId(), "VIEW_OWN_AUDIT", "AuditLog", actor.getId().toString(), "patient timeline");
        return auditLogs.findBySubjectPatientIdOrderByCreatedAtDesc(actor.getId()).stream()
                .map(this::toAudit)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CoordinationDtos.AuditEntryResponse> patientAudit(UserAccount actor, UUID patientId) {
        UserAccount patient = users.findById(patientId).orElseThrow(() -> ApiException.notFound("Patient not found"));
        boolean active = consents.hasActive(patientId, actor.getId());
        privacyGuard.assertCanAccessPatientRecord(actor, patient, active);
        if (actor.getRole() == Role.CLINICIAN) {
            privacyGuard.assertClinicianMayReadNotes(actor, active);
        }
        audit.record(actor, patientId, "VIEW_AUDIT", "AuditLog", patientId.toString(), "clinician/admin viewed timeline");
        return auditLogs.findBySubjectPatientIdOrderByCreatedAtDesc(patientId).stream()
                .map(this::toAudit)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CoordinationDtos.AuditEntryResponse> adminAudit(UserAccount actor) {
        privacyGuard.assertAdmin(actor);
        return auditLogs.findTop100ByOrderByCreatedAtDesc().stream().map(this::toAudit).toList();
    }

    @Transactional(readOnly = true)
    public Object dashboard(UserAccount actor) {
        return switch (actor.getRole()) {
            case PATIENT -> patientDashboard(actor);
            case CLINICIAN -> clinicianDashboard(actor);
            case ADMIN -> adminDashboard();
        };
    }

    @Transactional(readOnly = true)
    public List<CoordinationDtos.UserAdminRow> listUsers(UserAccount actor) {
        privacyGuard.assertAdmin(actor);
        return users.findAll().stream()
                .map(u -> new CoordinationDtos.UserAdminRow(
                        u.getId(), u.getEmail(), u.getRole().name(), u.getFirstName(), u.getLastName(), u.isEnabled()))
                .toList();
    }

    @Transactional
    public CoordinationDtos.UserAdminRow setEnabled(UserAccount actor, UUID id, boolean enabled) {
        privacyGuard.assertAdmin(actor);
        UserAccount target = users.findById(id).orElseThrow(() -> ApiException.notFound("User not found"));
        target.setEnabled(enabled);
        users.save(target);
        audit.record(actor, target.getRole() == Role.PATIENT ? target.getId() : null,
                enabled ? "ENABLE_USER" : "DISABLE_USER", "UserAccount", id.toString(), target.getEmail());
        return new CoordinationDtos.UserAdminRow(
                target.getId(), target.getEmail(), target.getRole().name(),
                target.getFirstName(), target.getLastName(), target.isEnabled());
    }

    private CoordinationDtos.PatientDashboard patientDashboard(UserAccount actor) {
        Instant now = Instant.now();
        List<CoordinationDtos.AppointmentResponse> upcoming = appointments.findByPatientIdOrderByCreatedAtDesc(actor.getId())
                .stream()
                .filter(a -> a.getStatus() == AppointmentStatus.BOOKED && a.getSlot().getStartsAt().isAfter(now.minusSeconds(1)))
                .map(appointmentService::toDto)
                .toList();
        long consentsActive = consentRecords.findByPatientIdOrderByGrantedAtDesc(actor.getId()).stream()
                .filter(c -> c.getStatus() == ConsentStatus.GRANTED)
                .count();
        return new CoordinationDtos.PatientDashboard(
                Disclaimer.TEXT,
                upcoming,
                notifications.unread(actor),
                auditLogs.countBySubjectPatientId(actor.getId()),
                consentsActive
        );
    }

    private CoordinationDtos.ClinicianDashboard clinicianDashboard(UserAccount actor) {
        Instant start = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = start.plusSeconds(86_400);
        List<CoordinationDtos.AppointmentResponse> today = appointments.findByClinicianIdOrderByCreatedAtDesc(actor.getId())
                .stream()
                .filter(a -> a.getStatus() == AppointmentStatus.BOOKED)
                .filter(a -> {
                    Instant t = a.getSlot().getStartsAt();
                    return !t.isBefore(start) && t.isBefore(end);
                })
                .map(appointmentService::toDto)
                .toList();
        return new CoordinationDtos.ClinicianDashboard(
                Disclaimer.TEXT,
                today,
                slots.countByClinicianIdAndBookedFalseAndStartsAtGreaterThanEqual(actor.getId(), Instant.now()),
                consentRecords.countByClinicianIdAndStatus(actor.getId(), ConsentStatus.GRANTED),
                notifications.unread(actor)
        );
    }

    private CoordinationDtos.AdminDashboard adminDashboard() {
        return new CoordinationDtos.AdminDashboard(
                Disclaimer.TEXT,
                users.count(),
                facilities.count(),
                appointments.countByStatus(AppointmentStatus.BOOKED),
                consentRecords.countByStatus(ConsentStatus.GRANTED)
        );
    }

    private CoordinationDtos.ClinicianResponse toClinician(ClinicianProfile p) {
        UserAccount u = p.getUser();
        return new CoordinationDtos.ClinicianResponse(
                u.getId(),
                u.getFirstName(),
                u.getLastName(),
                u.getEmail(),
                p.getSpecialty(),
                p.getCredentials(),
                p.getBio(),
                p.getFacility() == null ? null : p.getFacility().getId(),
                p.getFacility() == null ? null : p.getFacility().getName()
        );
    }

    private CoordinationDtos.PatientSummaryResponse toPatient(UserAccount patient, boolean consentActive) {
        PatientProfile profile = patients.findByUserId(patient.getId()).orElse(null);
        return new CoordinationDtos.PatientSummaryResponse(
                patient.getId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getEmail(),
                profile == null ? null : profile.getCity(),
                profile == null ? null : profile.getPreferredLanguage(),
                profile == null ? null : profile.getDateOfBirth(),
                consentActive
        );
    }

    private CoordinationDtos.AuditEntryResponse toAudit(com.hyguettelabs.medibridge.domain.entity.AuditLog log) {
        return new CoordinationDtos.AuditEntryResponse(
                log.getId(),
                log.getActorEmail(),
                log.getActorRole(),
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getSubjectPatientId(),
                log.getDetail(),
                log.getCreatedAt()
        );
    }
}
