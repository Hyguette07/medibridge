package com.hyguettelabs.medibridge.service;

import com.hyguettelabs.medibridge.domain.entity.Appointment;
import com.hyguettelabs.medibridge.domain.entity.AvailabilitySlot;
import com.hyguettelabs.medibridge.domain.entity.UserAccount;
import com.hyguettelabs.medibridge.domain.enums.AppointmentStatus;
import com.hyguettelabs.medibridge.domain.enums.NotificationType;
import com.hyguettelabs.medibridge.domain.enums.Role;
import com.hyguettelabs.medibridge.repository.AppointmentRepository;
import com.hyguettelabs.medibridge.repository.AvailabilitySlotRepository;
import com.hyguettelabs.medibridge.web.dto.CoordinationDtos;
import com.hyguettelabs.medibridge.web.error.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AppointmentService {

    private final AppointmentRepository appointments;
    private final AvailabilitySlotRepository slots;
    private final PrivacyGuard privacyGuard;
    private final AccessAuditService audit;
    private final NotificationService notifications;

    public AppointmentService(AppointmentRepository appointments,
                              AvailabilitySlotRepository slots,
                              PrivacyGuard privacyGuard,
                              AccessAuditService audit,
                              NotificationService notifications) {
        this.appointments = appointments;
        this.slots = slots;
        this.privacyGuard = privacyGuard;
        this.audit = audit;
        this.notifications = notifications;
    }

    @Transactional
    public CoordinationDtos.AppointmentResponse book(UserAccount actor, UUID slotId, CoordinationDtos.BookRequest request) {
        privacyGuard.assertPatient(actor);
        AvailabilitySlot slot = slots.findById(slotId).orElseThrow(() -> ApiException.notFound("Slot not found"));
        if (slot.isBooked()) {
            throw ApiException.conflict("That slot is already booked");
        }
        if (slot.getStartsAt().isBefore(java.time.Instant.now())) {
            throw ApiException.badRequest("Cannot book a slot in the past");
        }
        slot.setBooked(true);
        slots.save(slot);

        Appointment appointment = new Appointment();
        appointment.setPatient(actor);
        appointment.setClinician(slot.getClinician());
        appointment.setFacility(slot.getFacility());
        appointment.setSlot(slot);
        appointment.setStatus(AppointmentStatus.BOOKED);
        appointment.setReason(request == null ? null : request.reason());
        appointments.save(appointment);

        audit.record(actor, actor.getId(), "BOOK_APPOINTMENT", "Appointment", appointment.getId().toString(),
                slot.getFacility().getName());
        notifications.push(slot.getClinician(), NotificationType.APPOINTMENT, "New visit booked",
                actor.displayName() + " booked a visit at " + slot.getFacility().getName() + ".");
        notifications.push(actor, NotificationType.APPOINTMENT, "Visit confirmed",
                "Your visit with " + slot.getClinician().displayName() + " is booked.");
        return toDto(appointment);
    }

    @Transactional
    public CoordinationDtos.AppointmentResponse cancel(UserAccount actor, UUID id) {
        Appointment appointment = appointments.findById(id)
                .orElseThrow(() -> ApiException.notFound("Appointment not found"));
        boolean party = appointment.getPatient().getId().equals(actor.getId())
                || appointment.getClinician().getId().equals(actor.getId());
        if (!party && actor.getRole() != Role.ADMIN) {
            throw ApiException.forbidden("You cannot cancel this appointment");
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw ApiException.conflict("Appointment is already cancelled");
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        AvailabilitySlot slot = appointment.getSlot();
        slot.setBooked(false);
        slots.save(slot);
        appointments.save(appointment);

        UUID subject = appointment.getPatient().getId();
        audit.record(actor, subject, "CANCEL_APPOINTMENT", "Appointment", id.toString(), null);
        UserAccount other = actor.getId().equals(appointment.getPatient().getId())
                ? appointment.getClinician() : appointment.getPatient();
        notifications.push(other, NotificationType.APPOINTMENT, "Visit cancelled",
                "A booked visit at " + appointment.getFacility().getName() + " was cancelled.");
        return toDto(appointment);
    }

    @Transactional(readOnly = true)
    public List<CoordinationDtos.AppointmentResponse> mine(UserAccount actor) {
        List<Appointment> found = switch (actor.getRole()) {
            case PATIENT -> appointments.findByPatientIdOrderByCreatedAtDesc(actor.getId());
            case CLINICIAN -> appointments.findByClinicianIdOrderByCreatedAtDesc(actor.getId());
            case ADMIN -> appointments.findAll();
        };
        return found.stream().map(this::toDto).toList();
    }

    CoordinationDtos.AppointmentResponse toDto(Appointment a) {
        return new CoordinationDtos.AppointmentResponse(
                a.getId(),
                a.getPatient().getId(),
                a.getPatient().displayName(),
                a.getClinician().getId(),
                a.getClinician().displayName(),
                a.getFacility().getId(),
                a.getFacility().getName(),
                a.getSlot().getId(),
                a.getSlot().getStartsAt(),
                a.getSlot().getEndsAt(),
                a.getStatus(),
                a.getReason(),
                a.getCreatedAt()
        );
    }
}
