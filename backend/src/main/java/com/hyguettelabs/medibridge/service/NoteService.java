package com.hyguettelabs.medibridge.service;

import com.hyguettelabs.medibridge.Disclaimer;
import com.hyguettelabs.medibridge.domain.entity.Appointment;
import com.hyguettelabs.medibridge.domain.entity.CareNote;
import com.hyguettelabs.medibridge.domain.entity.UserAccount;
import com.hyguettelabs.medibridge.domain.enums.NotificationType;
import com.hyguettelabs.medibridge.repository.AppointmentRepository;
import com.hyguettelabs.medibridge.repository.CareNoteRepository;
import com.hyguettelabs.medibridge.repository.UserAccountRepository;
import com.hyguettelabs.medibridge.web.dto.CoordinationDtos;
import com.hyguettelabs.medibridge.web.error.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class NoteService {

    private final CareNoteRepository notes;
    private final UserAccountRepository users;
    private final AppointmentRepository appointments;
    private final ConsentService consents;
    private final PrivacyGuard privacyGuard;
    private final AccessAuditService audit;
    private final NotificationService notifications;

    public NoteService(CareNoteRepository notes,
                       UserAccountRepository users,
                       AppointmentRepository appointments,
                       ConsentService consents,
                       PrivacyGuard privacyGuard,
                       AccessAuditService audit,
                       NotificationService notifications) {
        this.notes = notes;
        this.users = users;
        this.appointments = appointments;
        this.consents = consents;
        this.privacyGuard = privacyGuard;
        this.audit = audit;
        this.notifications = notifications;
    }

    @Transactional(readOnly = true)
    public List<CoordinationDtos.NoteResponse> list(UserAccount actor, UUID patientId) {
        privacyGuard.assertClinician(actor);
        UserAccount patient = users.findById(patientId).orElseThrow(() -> ApiException.notFound("Patient not found"));
        boolean consented = consents.hasActive(patientId, actor.getId());
        privacyGuard.assertClinicianMayReadNotes(actor, consented);
        audit.record(actor, patientId, "VIEW_NOTES", "CareNote", patientId.toString(), "listed care notes");
        notifications.push(patient, NotificationType.ACCESS, "Record accessed",
                actor.displayName() + " viewed care notes on your coordination record.");
        return notes.findByPatientIdOrderByCreatedAtDesc(patientId).stream().map(this::toDto).toList();
    }

    @Transactional
    public CoordinationDtos.NoteResponse create(UserAccount actor, CoordinationDtos.NoteRequest request) {
        privacyGuard.assertClinician(actor);
        UserAccount patient = users.findById(request.patientId())
                .orElseThrow(() -> ApiException.notFound("Patient not found"));
        boolean consented = consents.hasActive(patient.getId(), actor.getId());
        privacyGuard.assertClinicianMayReadNotes(actor, consented);

        CareNote note = new CareNote();
        note.setPatient(patient);
        note.setClinician(actor);
        note.setBody(request.body().trim());
        if (request.appointmentId() != null) {
            Appointment appointment = appointments.findById(request.appointmentId())
                    .orElseThrow(() -> ApiException.notFound("Appointment not found"));
            note.setAppointment(appointment);
        }
        notes.save(note);
        audit.record(actor, patient.getId(), "WRITE_NOTE", "CareNote", note.getId().toString(),
                "clinician wrote a care note (body not stored in audit)");
        return toDto(note);
    }

    private CoordinationDtos.NoteResponse toDto(CareNote n) {
        return new CoordinationDtos.NoteResponse(
                n.getId(),
                n.getPatient().getId(),
                n.getPatient().displayName(),
                n.getClinician().getId(),
                n.getClinician().displayName(),
                n.getAppointment() == null ? null : n.getAppointment().getId(),
                n.getBody(),
                n.getCreatedAt(),
                Disclaimer.TEXT
        );
    }
}
